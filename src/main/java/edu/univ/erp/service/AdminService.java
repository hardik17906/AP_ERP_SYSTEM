package edu.univ.erp.service;

import edu.univ.erp.access.AccessService;
import edu.univ.erp.auth.hash.PasswordUtil;
import edu.univ.erp.auth.store.AuthDAO;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.Course;
import edu.univ.erp.domain.Instructor;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.UserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final AuthDAO authDAO;
    private final StudentDAO studentDAO;
    private final InstructorDAO instructorDAO;
    private final CourseDAO courseDAO;
    private final SectionDAO sectionDAO;
    private final SettingsDAO settingsDAO;
    private final AccessService accessService;

    public AdminService(AuthDAO authDAO, StudentDAO studentDAO, InstructorDAO instructorDAO,
                        CourseDAO courseDAO, SectionDAO sectionDAO, SettingsDAO settingsDAO,
                        AccessService accessService) {
        this.authDAO = authDAO;
        this.studentDAO = studentDAO;
        this.instructorDAO = instructorDAO;
        this.courseDAO = courseDAO;
        this.sectionDAO = sectionDAO;
        this.settingsDAO = settingsDAO;
        this.accessService = accessService;
    }

    public String toggleMaintenanceMode(boolean isEnabled) {
        String value = isEnabled ? "true" : "false";
        boolean success = settingsDAO.setSettingValue("maintenance_mode", value);
        return success ? "Success: Maintenance mode set to " + value : "Error: Could not update setting.";
    }


    public List<Student> getAllStudents() { return studentDAO.getAllStudents(); }

    public List<Instructor> getAllInstructors() {
        // 1. Get the raw list
        List<Instructor> instructors = instructorDAO.getAllInstructors();
        // 2. Get usernames
        Map<Integer, String> usernames = authDAO.getAllUsernamesAsMap();
        // 3. Stitch
        for(Instructor i : instructors) {
            i.setUsernameForDisplay(usernames.getOrDefault(i.getUserId(), "Unknown"));
        }
        return instructors;
    }

    public List<UserAuth> getAdmins() { return authDAO.getUsersByRole("ADMIN"); }
    public Map<Integer, String> getAllUsernames() { return authDAO.getAllUsernamesAsMap(); }

    public String createStudent(String username, String password, Student student) {
        if (authDAO.findUserByUsername(username).isPresent()) return "Error: Username taken.";
        int newUserId = authDAO.createUserAuth(username, "STUDENT", PasswordUtil.hashPassword(password));
        if (newUserId == -1) return "Error: Auth creation failed.";
        student.setUserId(newUserId);
        if (studentDAO.createStudent(student)) return "Success: Student created.";
        else { authDAO.deleteUserAuth(newUserId); return "Error: Profile creation failed."; }
    }

    public String createInstructor(String username, String password, Instructor instructor) {
        if (authDAO.findUserByUsername(username).isPresent()) return "Error: Username taken.";
        int newUserId = authDAO.createUserAuth(username, "INSTRUCTOR", PasswordUtil.hashPassword(password));
        if (newUserId == -1) return "Error: Auth creation failed.";
        instructor.setUserId(newUserId);
        if (instructorDAO.createInstructor(instructor)) return "Success: Instructor created.";
        else { authDAO.deleteUserAuth(newUserId); return "Error: Profile creation failed."; }
    }

    public String createAdmin(String username, String password) {
        if (authDAO.findUserByUsername(username).isPresent()) return "Error: Username taken.";
        int newUserId = authDAO.createUserAuth(username, "ADMIN", PasswordUtil.hashPassword(password));
        return (newUserId != -1) ? "Success: Admin created." : "Error: Failed to create admin.";
    }

    // COURSE & SECTION LOGIC

    public List<Course> getAllCourses() { return courseDAO.getAllCourses(); }
    public List<Section> getAllSections() { return sectionDAO.getAllSections(); }

    public String createCourse(Course course) {
        if (accessService.isMaintenanceModeOn()) return "Error: Maintenance is ON.";
        return courseDAO.createCourse(course) ? "Success: Course created." : "Error: Failed.";
    }

    public String updateCourse(Course course) {
        if (accessService.isMaintenanceModeOn()) return "Error: Maintenance is ON.";
        if (course.getTitle() == null || course.getTitle().isBlank()) return "Error: Title cannot be empty.";
        return courseDAO.updateCourse(course) ? "Success: Course updated." : "Error: Failed.";
    }

    public String createSection(Section section) {
        if (accessService.isMaintenanceModeOn()) return "Error: Maintenance is ON.";

        List<Section> termSections = sectionDAO.getSectionsByTerm(section.getSemester(), section.getYear());

        for (Section existing : termSections) {
            if (section.getInstructorId() > 0 &&
                    existing.getInstructorId() == section.getInstructorId() &&
                    existing.getDayTime().equalsIgnoreCase(section.getDayTime())) {

                return "Error: Instructor Conflict! They are already teaching at " + existing.getDayTime();
            }

            if (existing.getRoom().equalsIgnoreCase(section.getRoom()) &&
                    existing.getDayTime().equalsIgnoreCase(section.getDayTime())) {

                return "Error: Room Conflict! " + existing.getRoom() + " is booked at " + existing.getDayTime();
            }
        }

        return sectionDAO.createSection(section) ? "Success: Section created." : "Error: Database failure.";
    }

    public String updateSection(Section section) {
        if (accessService.isMaintenanceModeOn()) return "Error: Maintenance is ON.";

        List<Section> termSections = sectionDAO.getSectionsByTerm(section.getSemester(), section.getYear());

        for (Section existing : termSections) {
            if (existing.getSectionId() == section.getSectionId()) continue;

            if (section.getInstructorId() > 0 &&
                    existing.getInstructorId() == section.getInstructorId() &&
                    existing.getDayTime().equalsIgnoreCase(section.getDayTime())) {
                return "Error: Instructor Conflict! Time overlap.";
            }

            if (existing.getRoom().equalsIgnoreCase(section.getRoom()) &&
                    existing.getDayTime().equalsIgnoreCase(section.getDayTime())) {
                return "Error: Room Conflict! Room is booked.";
            }
        }

        return sectionDAO.updateSection(section) ? "Success: Section updated." : "Error: Database failure.";
    }

    public String assignInstructor(int sectionId, int instructorId) {
        if (accessService.isMaintenanceModeOn()) return "Error: Maintenance is ON.";
        return sectionDAO.assignInstructor(sectionId, instructorId) ? "Success" : "Error";
    }

    // --- DEADLINES ---
    public String updateRegistrationDeadline(String deadlineStr) {
        try { java.time.LocalDateTime.parse(deadlineStr); }
        catch (Exception e) { return "Error: Invalid format. Use yyyy-MM-ddTHH:mm"; }
        return settingsDAO.setSettingValue("registration_deadline", deadlineStr) ? "Success: Deadline updated." : "Error: Failed.";
    }

    public String getRegistrationDeadline() { return settingsDAO.getSettingValue("registration_deadline"); }

    public String updateDropDeadline(String deadlineStr) {
        try { java.time.LocalDateTime.parse(deadlineStr); }
        catch (Exception e) { return "Error: Invalid format. Use yyyy-MM-ddTHH:mm"; }
        return settingsDAO.setSettingValue("drop_deadline", deadlineStr) ? "Success: Drop deadline updated." : "Error: Failed.";
    }

    public String getDropDeadline() { return settingsDAO.getSettingValue("drop_deadline"); }
}