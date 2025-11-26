package edu.univ.erp.service.dto;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Student;

/**
 * A DTO (Data Transfer Object) that combines data from Enrollment, Student,
 * and the student's name (from AuthDAO) for easy display in the Gradebook UI.
 */
public class EnrollmentDetailDTO {

    private final Enrollment enrollment;
    private final Student student;
    private final String studentName; // From AuthDAO

    public EnrollmentDetailDTO(Enrollment enrollment, Student student, String studentName) {
        this.enrollment = enrollment;
        this.student = student;
        this.studentName = studentName;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public Student getStudent() {
        return student;
    }

    public String getStudentName() {
        return studentName;
    }

    public int getEnrollmentId() {
        return enrollment.getEnrollmentId();
    }
    
    public String getStudentRollNumber() {
        return student.getRollNumber();
    }
    
    public String getFinalGrade() {
        return enrollment.getFinalGrade();
    }
}