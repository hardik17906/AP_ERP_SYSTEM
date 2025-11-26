package edu.univ.erp.auth.store;

import edu.univ.erp.data.db.DatabaseManager;
import edu.univ.erp.domain.UserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthDAO {

    private static final Logger log = LoggerFactory.getLogger(AuthDAO.class);
    private final DatabaseManager dbManager;

    public AuthDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public Optional<UserAuth> findUserByUsername(String username) {
        String sql = "SELECT user_id, username, role, password_hash, status, last_login, failed_attempts, lockout_end FROM users_auth WHERE username = ?";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserAuth user = new UserAuth();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(rs.getString("role"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setStatus(rs.getString("status"));
                    user.setLastLogin(rs.getTimestamp("last_login"));
                    // Read new fields
                    user.setFailedAttempts(rs.getInt("failed_attempts"));
                    user.setLockoutEnd(rs.getTimestamp("lockout_end"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            log.error("Database error finding user: {}", username, e);
        }
        return Optional.empty();
    }

    public void incrementFailedAttempts(int userId) {
        String sql = "UPDATE users_auth SET failed_attempts = failed_attempts + 1 WHERE user_id = ?";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error incrementing failed attempts", e);
        }
    }

    public void tempLockAccount(int userId, int minutes) {
        String sql = "UPDATE users_auth SET lockout_end = ? WHERE user_id = ?";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Calculate future time
            LocalDateTime futureTime = LocalDateTime.now().plusMinutes(minutes);
            pstmt.setTimestamp(1, Timestamp.valueOf(futureTime));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error setting temp lock", e);
        }
    }

    public void resetLockAndAttempts(int userId) {
        // Clear both the counter AND the timer
        String sql = "UPDATE users_auth SET failed_attempts = 0, lockout_end = NULL WHERE user_id = ?";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("Error resetting lock", e);
        }
    }

    public Optional<UserAuth> findUserById(int userId) {
        String sql = "SELECT user_id, username, role, password_hash, status FROM users_auth WHERE user_id = ?";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserAuth user = new UserAuth();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setRole(rs.getString("role"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setStatus(rs.getString("status"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            log.error("Error finding user by id: {}", userId, e);
        }
        return Optional.empty();
    }

    public int createUserAuth(String username, String role, String passwordHash) {
        String sql = "INSERT INTO users_auth (username, role, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, username);
            pstmt.setString(2, role);
            pstmt.setString(3, passwordHash);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) return -1;
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) return generatedKeys.getInt(1);
            }
        } catch (SQLException e) { log.error("Error creating user: {}", username, e); }
        return -1;
    }

    public boolean updatePasswordHash(int userId, String newPasswordHash) {
        String sql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { log.error("Error updating password", e); return false; }
    }

    public boolean deleteUserAuth(int userId) {
        String sql = "DELETE FROM users_auth WHERE user_id = ?";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public void updateLastLogin(int userId) {
        String sql = "UPDATE users_auth SET last_login = ? WHERE user_id = ?";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.from(java.time.Instant.now()));
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) { log.warn("Could not update last_login", e); }
    }

    public Map<Integer, String> getAllUsernamesAsMap() { return getAllUsernamesAsMap(null); }

    public Map<Integer, String> getAllUsernamesAsMap(List<Integer> userIds) {
        Map<Integer, String> map = new HashMap<>();
        String sql = "SELECT user_id, username FROM users_auth";
        if (userIds != null && !userIds.isEmpty()) {
            String placeholders = userIds.stream().map(id -> "?").collect(Collectors.joining(","));
            sql += " WHERE user_id IN (" + placeholders + ")";
        }
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (userIds != null && !userIds.isEmpty()) {
                for (int i = 0; i < userIds.size(); i++) pstmt.setInt(i + 1, userIds.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) map.put(rs.getInt("user_id"), rs.getString("username"));
            }
        } catch (SQLException e) { log.error("Error getting usernames map", e); }
        return map;
    }

    public List<UserAuth> getUsersByRole(String role) {
        List<UserAuth> users = new ArrayList<>();
        String sql = "SELECT user_id, username, role, status FROM users_auth WHERE role = ?";
        try (Connection conn = dbManager.getAuthConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, role);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    UserAuth u = new UserAuth();
                    u.setUserId(rs.getInt("user_id"));
                    u.setUsername(rs.getString("username"));
                    u.setRole(rs.getString("role"));
                    u.setStatus(rs.getString("status"));
                    users.add(u);
                }
            }
        } catch (SQLException e) { log.error("Error getting users by role", e); }
        return users;
    }
}