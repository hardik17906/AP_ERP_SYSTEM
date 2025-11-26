package edu.univ.erp.auth;

/**
 * A simple, immutable POJO (Plain Old Java Object) that holds the
 * state of the currently authenticated user.
 *
 * This object is created by AuthService upon successful login and is
 * passed throughout the application (e.g., to the AccessService,
 * MainDashboard) to identify who the user is and what their role is.
 */
public class UserSession {

    private final int userId;
    private final String username;
    private final String role; // "STUDENT", "INSTRUCTOR", or "ADMIN"

    /**
     * Constructs a new UserSession.
     *
     * @param userId   The unique ID of the user (from auth_db.users_auth.user_id).
     * @param username The user's login name.
     * @param role     The user's role (e.g., "STUDENT").
     */
    public UserSession(int userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isInstructor() {
        return "INSTRUCTOR".equals(role);
    }

    public boolean isStudent() {
        return "STUDENT".equals(role);
    }

    @Override
    public String toString() {
        return "UserSession{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}