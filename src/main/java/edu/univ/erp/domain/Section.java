package edu.univ.erp.domain;

/**
 * POJO for the 'sections' table.
 * Represents a specific instance of a Course offered in a term.
 */
public class Section {

    private int sectionId;
    private String courseCode; // Foreign key to Course
    private int instructorId; // Foreign key to Instructor (can be 0 or null)
    private String dayTime; // e.g., "MWF 10:00-11:00"
    private String room;
    private int capacity;
    private String semester; // e.g., "FALL"
    private int year;

    public Section() {
    }

    public Section(int sectionId, String courseCode, int instructorId, String dayTime, String room, int capacity, String semester, int year) {
        this.sectionId = sectionId;
        this.courseCode = courseCode;
        this.instructorId = instructorId;
        this.dayTime = dayTime;
        this.room = room;
        this.capacity = capacity;
        this.semester = semester;
        this.year = year;
    }
    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public int getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(int instructorId) {
        this.instructorId = instructorId;
    }

    public String getDayTime() {
        return dayTime;
    }

    public void setDayTime(String dayTime) {
        this.dayTime = dayTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Section{" +
                "sectionId=" + sectionId +
                ", courseCode='" + courseCode + '\'' +
                ", instructorId=" + instructorId +
                ", capacity=" + capacity +
                '}';
    }
}