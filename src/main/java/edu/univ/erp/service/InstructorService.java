package edu.univ.erp.service;

import edu.univ.erp.access.AccessService;
import edu.univ.erp.auth.store.AuthDAO;
import edu.univ.erp.data.*;
import edu.univ.erp.domain.*;
import edu.univ.erp.service.dto.EnrollmentDetailDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class InstructorService {

    private static final Logger log = LoggerFactory.getLogger(InstructorService.class);

    private final SectionDAO sectionDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final GradeDAO gradeDAO;
    private final AccessService accessService;
    private final StudentDAO studentDAO;
    private final AuthDAO authDAO;
    private final AssessmentDAO assessmentDAO;

    public InstructorService(SectionDAO sectionDAO, EnrollmentDAO enrollmentDAO,
                             GradeDAO gradeDAO, AccessService accessService,
                             StudentDAO studentDAO, AuthDAO authDAO,
                             AssessmentDAO assessmentDAO) {
        this.sectionDAO = sectionDAO;
        this.enrollmentDAO = enrollmentDAO;
        this.gradeDAO = gradeDAO;
        this.accessService = accessService;
        this.studentDAO = studentDAO;
        this.authDAO = authDAO;
        this.assessmentDAO = assessmentDAO;
    }

    public List<Section> getMySections(int instructorId, String semester, int year) {
        return sectionDAO.getSectionsByInstructor(instructorId, semester, year);
    }

    public List<EnrollmentDetailDTO> getEnrollmentsForSection(int instructorId, int sectionId) {
        if (!accessService.canInstructorManageSection(sectionId)) return List.of();
        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsBySection(sectionId);
        if (enrollments.isEmpty()) return List.of();
        List<Integer> studentIds = enrollments.stream().map(Enrollment::getStudentId).collect(Collectors.toList());
        Map<Integer, Student> studentMap = studentDAO.getStudentsByIds(studentIds);
        Map<Integer, String> nameMap = authDAO.getAllUsernamesAsMap(studentIds);

        List<EnrollmentDetailDTO> dtoList = new ArrayList<>();
        for (Enrollment enr : enrollments) {
            Student stu = studentMap.get(enr.getStudentId());
            String name = nameMap.getOrDefault(enr.getStudentId(), "N/A");
            if (stu != null) dtoList.add(new EnrollmentDetailDTO(enr, stu, name));
        }
        return dtoList;
    }

    public List<Grade> getGradesForEnrollment(int instructorId, int enrollmentId) {
        if (!accessService.canAccessEnrollmentData(enrollmentId)) return List.of();
        return gradeDAO.getGradesForEnrollment(enrollmentId);
    }

    public List<AssessmentComponent> getAssessmentSchema(int sectionId) {
        return assessmentDAO.getSchemaForSection(sectionId);
    }

    public String saveAssessmentSchema(int instructorId, int sectionId, List<AssessmentComponent> schema) {
        if (!accessService.canInstructorManageSection(sectionId)) return "Error: Not authorized.";
        if (accessService.isMaintenanceModeOn()) return "Error: Maintenance Mode.";

        BigDecimal totalWeight = BigDecimal.ZERO;
        for(AssessmentComponent c : schema) totalWeight = totalWeight.add(c.getWeightage());

        if (totalWeight.compareTo(new BigDecimal("100.0")) != 0) {
            return "Error: Total weightage must be exactly 100%. Current: " + totalWeight + "%";
        }

        assessmentDAO.clearSchema(sectionId);

        for(AssessmentComponent c : schema) {
            boolean saved = assessmentDAO.createComponent(c);
            if (!saved) return "Error: Database failed to save component.";
        }
        return "Success: Grading schema saved.";
    }

    public String computeAndSaveAllFinalGrades(int instructorId, int sectionId) {
        if (!accessService.canInstructorManageSection(sectionId)) return "Error: Not authorized.";
        if (accessService.isMaintenanceModeOn()) return "Error: Maintenance Mode.";

        List<AssessmentComponent> schema = assessmentDAO.getSchemaForSection(sectionId);
        if (schema.isEmpty()) return "Error: No grading schema defined for this section.";

        List<EnrollmentDetailDTO> enrollments = getEnrollmentsForSection(instructorId, sectionId);

        int processed = 0;
        for (EnrollmentDetailDTO dto : enrollments) {
            List<Grade> studentGrades = gradeDAO.getGradesForEnrollment(dto.getEnrollmentId());
            BigDecimal finalPercentage = calculatePercentage(studentGrades, schema);
            String letter = determineLetterGrade(finalPercentage);
            enrollmentDAO.setFinalGrade(dto.getEnrollmentId(), letter);
            processed++;
        }
        return "Success: Calculated grades for " + processed + " students.";
    }

    // --- REVISED: STANDARD TEXT FORMATTING (NO HTML) ---
    public String getClassStatistics(int sectionId) {
        List<AssessmentComponent> schema = assessmentDAO.getSchemaForSection(sectionId);
        if (schema.isEmpty()) return "No grading schema defined yet.";

        List<Enrollment> enrollments = enrollmentDAO.getEnrollmentsBySection(sectionId);
        if (enrollments.isEmpty()) return "No students enrolled.";

        List<BigDecimal> percentages = new ArrayList<>();
        Map<String, Integer> gradeCounts = new HashMap<>();

        for (Enrollment enr : enrollments) {
            List<Grade> grades = gradeDAO.getGradesForEnrollment(enr.getEnrollmentId());
            BigDecimal p = calculatePercentage(grades, schema);
            percentages.add(p);

            String letter = determineLetterGrade(p);
            gradeCounts.put(letter, gradeCounts.getOrDefault(letter, 0) + 1);
        }

        if (percentages.isEmpty()) return "No data available.";

        // Summary Math
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;
        BigDecimal min = new BigDecimal("100.0");

        for (BigDecimal p : percentages) {
            sum = sum.add(p);
            if (p.compareTo(max) > 0) max = p;
            if (p.compareTo(min) < 0) min = p;
        }
        BigDecimal average = sum.divide(new BigDecimal(percentages.size()), 2, RoundingMode.HALF_UP);

        // Build Standard Text String
        StringBuilder sb = new StringBuilder();
        sb.append("CLASS STATISTICS\n\n");
        sb.append(String.format("%-15s %5d\n", "Total Students:", percentages.size()));
        sb.append(String.format("%-15s %5s%%\n", "Average Score:", average.toString()));
        sb.append(String.format("%-15s %5s%%\n", "Highest Score:", max.toString()));
        sb.append(String.format("%-15s %5s%%\n", "Lowest Score:", min.toString()));
        sb.append("\nGRADE DISTRIBUTION\n");

        String[] gradeOrder = {"A+", "A", "B+", "B", "C+", "C", "D+", "D", "E", "F"};
        for (String grade : gradeOrder) {
            int count = gradeCounts.getOrDefault(grade, 0) ;
            sb.append(String.format("%-3s : %-3d\n", grade, count));
        }

        return sb.toString();
    }

    private BigDecimal calculatePercentage(List<Grade> grades, List<AssessmentComponent> schema) {
        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        
        for (AssessmentComponent comp : schema) {
            // Find the grade for this component
            BigDecimal scoreObtained = grades.stream()
                .filter(g -> g.getComponentName().equalsIgnoreCase(comp.getName()))
                .map(Grade::getScore)
                .findFirst()
                .orElse(BigDecimal.ZERO);

            if (comp.getMaxMarks().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = scoreObtained.divide(comp.getMaxMarks(), 4, RoundingMode.HALF_UP);
                totalWeightedScore = totalWeightedScore.add(ratio.multiply(comp.getWeightage()));
            }
        }
        return totalWeightedScore;
    }

    private String determineLetterGrade(BigDecimal score) {
        double s = score.doubleValue();
        if (s >= 95) return "A+";
        if (s >= 90) return "A";
        if (s >= 85) return "B+";
        if (s >= 80) return "B";
        if (s >= 70) return "C+";
        if (s >= 60) return "C";
        if (s >= 50) return "D+";
        if (s >= 40) return "D";
        if (s >= 30) return "E";
        return "F";
    }

    public String saveAllGrades(int userId, int sectionId, int enrollmentId, List<Grade> gradesToSave) {
        if (accessService.isReadOnly()) return "Error: Read only mode.";

        for(Grade g : gradesToSave) {
            gradeDAO.saveOrUpdateGrade(g.getEnrollmentId(), g.getComponentName(), g.getScore());
        }
        return "Success: Grades saved.";
    }

    public String saveFinalGrade(int instructorId, int enrollmentId, String finalGrade) {
        if (accessService.isMaintenanceModeOn()) return "Error: Maintenance Mode.";
        enrollmentDAO.setFinalGrade(enrollmentId, finalGrade);
        return "Success: Final grade saved.";
    }
}