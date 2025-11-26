package edu.univ.erp.service.dto;

/**
 * A DTO (Data Transfer Object) that combines data from Enrollment, Section,
 * and Course for easy display in the "My Grades" and "My Registrations" panels.
 */
public class RegistrationDetailDTO {

    private int enrollmentId;
    private String status;
    private String finalGrade;
    
    private String courseCode;
    private String courseTitle;
    private int credits;
    
    private String dayTime;
    private String room;
    private String semester;
    private int year;
    
    public RegistrationDetailDTO() {}
    
    public int getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(int enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFinalGrade() { return finalGrade; }
    public void setFinalGrade(String finalGrade) { this.finalGrade = finalGrade; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getDayTime() { return dayTime; }
    public void setDayTime(String dayTime) { this.dayTime = dayTime; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
}