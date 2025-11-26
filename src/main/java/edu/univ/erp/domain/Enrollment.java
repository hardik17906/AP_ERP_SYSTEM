package edu.univ.erp.domain;

/**
 * POJO for the 'enrollments' table.
 * Represents the link between a Student and a Section.
 */
public class Enrollment {

    private int enrollmentId;
    private int studentId;
    private int sectionId;
    private String status; // e.g., "REGISTERED", "DROPPED", "COMPLETED"
    private String finalGrade; // e.g., "A", "B+", NULL

    public Enrollment() {
    }

    public Enrollment(int enrollmentId, int studentId, int sectionId, String status, String finalGrade) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.status = status;
        this.finalGrade = finalGrade;
    }
    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(String finalGrade) {
        this.finalGrade = finalGrade;
    }

    
    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", studentId=" + studentId +
                ", sectionId=" + sectionId +
                ", status='" + status + '\'' +
                '}';
    }
}