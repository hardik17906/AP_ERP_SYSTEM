package edu.univ.erp.auth;

import edu.univ.erp.auth.hash.PasswordUtil;
import edu.univ.erp.auth.store.AuthDAO;
import edu.univ.erp.domain.UserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 2; // Bonus: Temporary Lock Duration

    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public AuthResult login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            return AuthResult.failure("Username and password cannot be empty.");
        }

        try {
            Optional<UserAuth> userOpt = authDAO.findUserByUsername(username);

            if (userOpt.isEmpty()) {
                return AuthResult.failure("Incorrect username or password.");
            }

            UserAuth user = userOpt.get();

            // 1. CHECK FOR PERMANENT ADMIN LOCK
            if (!"ACTIVE".equals(user.getStatus())) {
                return AuthResult.failure("Account is disabled. Please contact admin.");
            }

            // 2. CHECK FOR TEMPORARY LOCKOUT
            if (user.getLockoutEnd() != null) {
                if (user.getLockoutEnd().after(Timestamp.from(Instant.now()))) {
                    // Lock is active
                    long millisLeft = user.getLockoutEnd().getTime() - System.currentTimeMillis();
                    long secondsLeft = millisLeft / 1000;
                    return AuthResult.failure("Account locked due to failed attempts. Try again in " + secondsLeft + " seconds.");
                } else {
                    // Lock expired -> Auto Unlock logic
                    authDAO.resetLockAndAttempts(user.getUserId());
                    user.setFailedAttempts(0); // Update local object state
                }
            }

            // 3. VERIFY PASSWORD
            if (PasswordUtil.checkPassword(password, user.getPasswordHash())) {
                // SUCCESS
                log.info("User logged in successfully: {}", username);

                // Clear any previous failures on success
                authDAO.resetLockAndAttempts(user.getUserId());
                authDAO.updateLastLogin(user.getUserId());

                UserSession session = new UserSession(user.getUserId(), user.getUsername(), user.getRole());
                return AuthResult.success(session);
            } else {
                // FAILURE
                int currentFails = user.getFailedAttempts() + 1;
                authDAO.incrementFailedAttempts(user.getUserId());

                log.warn("Failed login for {}. Attempt {}/{}", username, currentFails, MAX_ATTEMPTS);

                if (currentFails >= MAX_ATTEMPTS) {
                    // Trigger Temporary Lock
                    authDAO.tempLockAccount(user.getUserId(), LOCK_DURATION_MINUTES);
                    return AuthResult.failure("Too many failed attempts. Account locked for " + LOCK_DURATION_MINUTES + " minutes.");
                }

                return AuthResult.failure("Incorrect password. Attempts remaining: " + (MAX_ATTEMPTS - currentFails));
            }

        } catch (Exception e) {
            log.error("An unexpected database error occurred during login for user: " + username, e);
            return AuthResult.failure("A system error occurred. Please try again later.");
        }
    }

    public String changePassword(int userId, String oldPassword, String newPassword) {
        try {
            Optional<UserAuth> userOpt = authDAO.findUserById(userId);
            if (userOpt.isEmpty()) return "Error: User not found.";

            UserAuth user = userOpt.get();

            if (!PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) {
                return "Error: Your old password was incorrect.";
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                return "Error: New password cannot be empty.";
            }

            String newPasswordHash = PasswordUtil.hashPassword(newPassword);
            boolean success = authDAO.updatePasswordHash(userId, newPasswordHash);

            return success ? "Success: Your password has been changed." : "Error: Database update failed.";

        } catch (Exception e) {
            log.error("Error during password change for userId: " + userId, e);
            return "Error: A system error occurred.";
        }
    }

    public static class AuthResult {
        private final boolean success;
        private final UserSession session;
        private final String errorMessage;

        private AuthResult(boolean success, UserSession session, String errorMessage) {
            this.success = success;
            this.session = session;
            this.errorMessage = errorMessage;
        }

        public static AuthResult success(UserSession session) { return new AuthResult(true, session, null); }
        public static AuthResult failure(String errorMessage) { return new AuthResult(false, null, errorMessage); }

        public boolean isSuccess() { return success; }
        public UserSession getSession() { return session; }
        public String getErrorMessage() { return errorMessage; }
    }
}