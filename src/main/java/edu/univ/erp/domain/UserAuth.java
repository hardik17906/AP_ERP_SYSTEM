package edu.univ.erp.domain;

import java.sql.Timestamp;

/**
 * POJO for the 'users_auth' table.
 * Represents a user's login credentials and core identity.
 */
public class UserAuth {

    private int userId;
    private String username;
    private String role; // "ADMIN", "INSTRUCTOR", "STUDENT"
    private String passwordHash; // The bcrypt hash
    private String status; // "ACTIVE", "LOCKED"
    private Timestamp lastLogin;
    private int failedAttempts;
    private Timestamp lockoutEnd; 
    public UserAuth() {
    }

    public UserAuth(int userId, String username, String role, String passwordHash, String status, Timestamp lastLogin, int failedAttempts, Timestamp lockoutEnd) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.passwordHash = passwordHash;
        this.status = status;
        this.lastLogin = lastLogin;
        this.failedAttempts = failedAttempts;
        this.lockoutEnd = lockoutEnd;
    }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public Timestamp getLockoutEnd() { return lockoutEnd; }
    public void setLockoutEnd(Timestamp lockoutEnd) { this.lockoutEnd = lockoutEnd; }

    @Override
    public String toString() {
        return "UserAuth{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}