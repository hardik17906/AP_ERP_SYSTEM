package edu.univ.erp.service;

import edu.univ.erp.access.AccessService;
import edu.univ.erp.auth.UserSession;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.dto.RegistrationDetailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Student-specific actions.
 * Enforces all business rules related to registration, drops, and grade viewing.
 */
public class StudentService {

    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    // Dependencies
    private final EnrollmentDAO enrollmentDAO;
    private final SectionDAO sectionDAO;
    private final GradeDAO gradeDAO;
    private final SettingsDAO settingsDAO;
    private final AccessService accessService;
    private final CourseDAO courseDAO;
    private final UserSession session;
    private final StudentDAO studentDAO;

    // Constructor for Dependency Injection
    public StudentService(EnrollmentDAO enrollmentDAO, SectionDAO sectionDAO,
                          GradeDAO gradeDAO, SettingsDAO settingsDAO, AccessService accessService,
                          CourseDAO courseDAO, UserSession session,
                          StudentDAO studentDAO) {
        this.enrollmentDAO = enrollmentDAO;
        this.sectionDAO = sectionDAO;
        this.gradeDAO = gradeDAO;
        this.settingsDAO = settingsDAO;
        this.accessService = accessService;
        this.courseDAO = courseDAO;
        this.session = session;
        this.studentDAO = studentDAO;
    }

    /**
     * Attempts to register a student for a section.
     * Checks maintenance mode, deadline, capacity, and duplicates.
     */
    public String registerForSection(int studentId, int sectionId) {
        log.info("Attempting registration: studentId={}, sectionId={}", studentId, sectionId);

        // 1. Check Maintenance Mode
        if (accessService.isMaintenanceModeOn()) {
            return "Error: System is in maintenance mode. Please try again later.";
        }

        // 2. Check Registration Deadline
        if (isPastDeadline("registration_deadline")) {
            return "Error: The registration deadline has passed.";
        }

        // 3. Get Data
        Optional<Section> sectionOpt = sectionDAO.getSectionById(sectionId);
        if (sectionOpt.isEmpty()) {
            return "Error: Section not found.";
        }
        Section section = sectionOpt.get();

        int currentEnrollment = enrollmentDAO.getEnrollmentCount(sectionId);
        boolean alreadyEnrolled = enrollmentDAO.hasEnrollment(studentId, sectionId);

        // 4. Enforce Business Rules
        if (alreadyEnrolled) {
            return "Error: You are already registered for this section.";
        }

        if (currentEnrollment >= section.getCapacity()) {
            return "Error: Section is full.";
        }

        // 5. Execute
        boolean success = enrollmentDAO.createEnrollment(studentId, sectionId);

        if (success) {
            log.info("Successful registration: S_ID={}, SECT_ID={}", studentId, sectionId);
            return "Success: Registered for " + section.getCourseCode();
        } else {
            log.error("Failed registration (DB Error): S_ID={}, SECT_ID={}", studentId, sectionId);
            return "Error: A database error occurred.";
        }
    }

    /**
     * Attempts to drop a student from a section.
     * Checks the Drop Deadline and Ownership.
     */
    public String dropSection(int studentId, int enrollmentId) {
        log.info("Attempting drop: studentId={}, enrollmentId={}", studentId, enrollmentId);

        // 1. Check Maintenance
        if (accessService.isMaintenanceModeOn()) {
            return "Error: System is in maintenance mode.";
        }

        // 2. Check DROP Deadline
        if (isPastDeadline("drop_deadline")) {
            return "Error: The drop deadline has passed.";
        }

        // 3. Verify Ownership
        Optional<Enrollment> enrollOpt = enrollmentDAO.getEnrollmentById(enrollmentId);
        if (enrollOpt.isEmpty() || enrollOpt.get().getStudentId() != studentId) {
            return "Error: This is not your enrollment.";
        }

        // 4. Check Status
        if (!"REGISTERED".equals(enrollOpt.get().getStatus())) {
            return "Error: This section is not currently registered.";
        }

        // 5. Execute
        boolean success = enrollmentDAO.updateEnrollmentStatus(enrollmentId, "DROPPED");

        if (success) {
            log.info("Successful drop: S_ID={}, ENR_ID={}", studentId, enrollmentId);
            return "Success: Section dropped.";
        } else {
            return "Error: A database error occurred.";
        }
    }

    /**
     * Retrieves the profile for a single student.
     */
    public Optional<Student> getStudentDetails(int studentId) {
        if (!accessService.canViewStudentProfile(studentId)) {
            return Optional.empty();
        }
        return studentDAO.getStudentById(studentId);
    }

    /**
     * Retrieves all enrollments and their associated grades for a student.
     * FILTERS OUT DROPPED COURSES.
     */
    public Map<RegistrationDetailDTO, List<Grade>> getGrades(int studentId) {
        Map<RegistrationDetailDTO, List<Grade>> detailedGradesMap = new LinkedHashMap<>();

        // 1. Get all enrollments
        List<Enrollment> allEnrollments = enrollmentDAO.getEnrollmentsByStudent(studentId);
        if (allEnrollments.isEmpty()) {
            return detailedGradesMap;
        }

        // 2. Filter out DROPPED courses
        List<Enrollment> activeEnrollments = allEnrollments.stream()
                .filter(e -> !"DROPPED".equals(e.getStatus()))
                .collect(Collectors.toList());

        if (activeEnrollments.isEmpty()) {
            return detailedGradesMap;
        }

        // 3. Get unique section IDs
        List<Integer> sectionIds = activeEnrollments.stream()
                .map(Enrollment::getSectionId)
                .distinct()
                .collect(Collectors.toList());

        // 4. Bulk fetch Section and Course data
        Map<Integer, Section> sectionMap = sectionDAO.getSectionsByIds(sectionIds);
        Map<String, Course> courseMap = courseDAO.getAllCoursesAsMap();

        // 5. Build the DTO Map
        for (Enrollment enr : activeEnrollments) {
            Section sec = sectionMap.get(enr.getSectionId());
            if (sec == null) continue;

            Course cou = courseMap.get(sec.getCourseCode());
            if (cou == null) continue;

            RegistrationDetailDTO dto = new RegistrationDetailDTO();
            dto.setEnrollmentId(enr.getEnrollmentId());
            dto.setStatus(enr.getStatus());
            dto.setFinalGrade(enr.getFinalGrade());
            dto.setCourseCode(cou.getCourseCode());
            dto.setCourseTitle(cou.getTitle());
            dto.setCredits(cou.getCredits());
            dto.setDayTime(sec.getDayTime());
            dto.setRoom(sec.getRoom());
            dto.setSemester(sec.getSemester());
            dto.setYear(sec.getYear());

            // Get individual component grades
            List<Grade> grades = gradeDAO.getGradesForEnrollment(enr.getEnrollmentId());
            detailedGradesMap.put(dto, grades);
        }

        return detailedGradesMap;
    }

    /**
     * Gets a list of detailed DTOs for a student's *current* registrations.
     * Used for Timetable and Drop Panel.
     */
    public List<RegistrationDetailDTO> getMyCurrentRegistrations(int studentId, String semester, int year) {
        List<RegistrationDetailDTO> dtoList = new ArrayList<>();

        // Get enrollments for the specific term
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsByStudentAndTerm(studentId, semester, year);

        // Filter for only 'REGISTERED' status
        enrollments = enrollments.stream()
                .filter(e -> "REGISTERED".equals(e.getStatus()))
                .collect(Collectors.toList());

        if (enrollments.isEmpty()) {
            return dtoList;
        }

        // Bulk fetch data
        List<Integer> sectionIds = enrollments.stream().map(Enrollment::getSectionId).distinct().collect(Collectors.toList());
        Map<Integer, Section> sectionMap = sectionDAO.getSectionsByIds(sectionIds);
        Map<String, Course> courseMap = courseDAO.getAllCoursesAsMap();

        for (Enrollment enr : enrollments) {
            Section sec = sectionMap.get(enr.getSectionId());
            if (sec == null) continue;
            Course cou = courseMap.get(sec.getCourseCode());
            if (cou == null) continue;

            RegistrationDetailDTO dto = new RegistrationDetailDTO();
            dto.setEnrollmentId(enr.getEnrollmentId());
            dto.setStatus(enr.getStatus());
            dto.setCourseCode(cou.getCourseCode());
            dto.setCourseTitle(cou.getTitle());
            dto.setDayTime(sec.getDayTime());
            dto.setRoom(sec.getRoom());
            dto.setCredits(cou.getCredits()); // Added for Timetable
            
            dtoList.add(dto);
        }

        return dtoList;
    }
    private boolean isPastDeadline(String deadlineKey) {
        try {
            String deadlineStr = settingsDAO.getSettingValue(deadlineKey);
            if (deadlineStr == null) {
                return false; // Fail-open: if no deadline is set, allow the action
            }
            LocalDateTime deadline = LocalDateTime.parse(deadlineStr);
            return LocalDateTime.now().isAfter(deadline);
        } catch (DateTimeParseException e) {
            log.error("Could not parse deadline: {}. Allowing action.", deadlineKey, e);
            return false; 
        }
    }
}