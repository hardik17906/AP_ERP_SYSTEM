package edu.univ.erp.domain;

import java.math.BigDecimal;

/**
 * POJO for the 'grades' table.
 * Represents a single assessment component (e.g., Quiz 1)
 * for a specific enrollment.
 */
public class Grade {

    private int gradeId;
    private int enrollmentId;
    private String componentName; // e.g., "Midterm", "Quiz 1"
    
    // Use BigDecimal for scores to avoid floating-point errors
    private BigDecimal score;

    public Grade() {
    }
    public Grade(int gradeId, int enrollmentId, String componentName, BigDecimal score) {
        this.gradeId = gradeId;
        this.enrollmentId = enrollmentId;
        this.componentName = componentName;
        this.score = score;
    }
    public Grade(int enrollmentId, String componentName, BigDecimal score) {
        this.enrollmentId = enrollmentId;
        this.componentName = componentName;
        this.score = score;
    }
    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Grade{" +
                "gradeId=" + gradeId +
                ", enrollmentId=" + enrollmentId +
                ", componentName='" + componentName + '\'' +
                ", score=" + score +
                '}';
    }
}