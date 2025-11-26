package edu.univ.erp.domain;

/**
 * POJO for the 'students' table.
 * Represents a student's profile.
 */
public class Student {

    private int userId; // This is the foreign key to the auth_db.users_auth table
    private String rollNumber;
    private String program; // e.g., "B.Tech. CSE"
    private int year;
    private String name;
    /**
     * Default constructor
     */
    public Student() {
    }
    public Student(int userId, String rollNumber, String program, int year) {
        this.userId = userId;
        this.rollNumber = rollNumber;
        this.program = program;
        this.year = year;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Student{" +
                "userId=" + userId +
                ", rollNumber='" + rollNumber + '\'' +
                ", program='" + program + '\'' +
                '}';
    }
}