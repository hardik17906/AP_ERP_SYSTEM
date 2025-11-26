package edu.univ.erp.domain;

/**
 * POJO for the 'instructors' table.
 * Represents an instructor's profile.
 */
public class Instructor {

    private int userId; // This is the foreign key to the auth_db.users_auth table
    private String department;
    private String title;

    private String usernameForDisplay;

    public Instructor() {
    }
    public Instructor(int userId, String department, String title) {
        this.userId = userId;
        this.department = department;
        this.title = title;
    }
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsernameForDisplay() {
        return usernameForDisplay;
    }

    public void setUsernameForDisplay(String usernameForDisplay) {
        this.usernameForDisplay = usernameForDisplay;
    }
    @Override
    public String toString() {
        String name = (usernameForDisplay != null) ? usernameForDisplay : "Unknown";
        // Formats ID as 3 digits (e.g., 5 becomes 005)
        return String.format("FAC-%03d : %s (%s)", userId, name, department);
    }
}