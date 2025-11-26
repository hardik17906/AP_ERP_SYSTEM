package edu.univ.erp.service.dto;

import java.math.BigDecimal;

/**
 * A DTO (Data Transfer Object) to represent a single row in the course catalog.
 * This class combines data from multiple tables (Course, Section, Instructor, Enrollment)
 * for easy display in the UI.
 */
public class CatalogEntry {

    private int sectionId;
    private String courseCode;
    private String courseTitle;
    private int credits;
    private String instructorName;
    private String dayTime;
    private String room;
    private int capacity;
    private int currentEnrollment;

    // Full constructor
    public CatalogEntry(int sectionId, String courseCode, String courseTitle, int credits,
                        String instructorName, String dayTime, String room,
                        int capacity, int currentEnrollment) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credits = credits;
        this.instructorName = instructorName;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.currentEnrollment = currentEnrollment;
    }
    
    // Empty constructor
    public CatalogEntry() {}

    public int getSectionId() { return sectionId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public int getCredits() { return credits; }
    public String getInstructorName() { return instructorName; }
    public String getDayTime() { return dayTime; }
    public String getRoom() { return room; }
    public int getCapacity() { return capacity; }
    public int getCurrentEnrollment() { return currentEnrollment; }
}