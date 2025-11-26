package edu.univ.erp;

import edu.univ.erp.auth.AuthService;
import edu.univ.erp.auth.hash.PasswordUtil;
import edu.univ.erp.auth.store.AuthDAO;
import edu.univ.erp.domain.UserAuth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthDAO authDAO;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(authDAO);
    }

    @Test
    void testLoginSuccess() {
        String username = "student1";
        String rawPassword = "password123";
        String hashed = PasswordUtil.hashPassword(rawPassword);

        UserAuth mockUser = new UserAuth();
        mockUser.setUserId(1);
        mockUser.setUsername(username);
        mockUser.setRole("STUDENT");
        mockUser.setPasswordHash(hashed);
        mockUser.setStatus("ACTIVE");

        when(authDAO.findUserByUsername(username)).thenReturn(Optional.of(mockUser));

        AuthService.AuthResult result = authService.login(username, rawPassword);

        assertTrue(result.isSuccess());
        assertNotNull(result.getSession());
        assertEquals("STUDENT", result.getSession().getRole());
    }

    @Test
    void testLoginWrongPassword() {
        String username = "student1";
        String correctPass = "password123";
        String wrongPass = "wrongpass";

        UserAuth mockUser = new UserAuth();
        mockUser.setUsername(username);
        mockUser.setPasswordHash(PasswordUtil.hashPassword(correctPass));
        mockUser.setStatus("ACTIVE");
        mockUser.setFailedAttempts(0);

        when(authDAO.findUserByUsername(username)).thenReturn(Optional.of(mockUser));

        AuthService.AuthResult result = authService.login(username, wrongPass);

        assertFalse(result.isSuccess());
        // The message might vary, just check it failed
        assertNotNull(result.getErrorMessage());

        verify(authDAO, times(1)).incrementFailedAttempts(anyInt());
    }

    @Test
    void testAccountLocked() {
        String username = "student1";
        UserAuth mockUser = new UserAuth();
        mockUser.setStatus("ACTIVE");

        // --- UPDATED LOGIC: Set a future timestamp to simulate a lock ---
        // Set lock to expire 5 minutes in the future
        Timestamp futureTime = Timestamp.from(Instant.now().plusSeconds(300));
        mockUser.setLockoutEnd(futureTime);

        when(authDAO.findUserByUsername(username)).thenReturn(Optional.of(mockUser));

        AuthService.AuthResult result = authService.login(username, "anyPassword");

        assertFalse(result.isSuccess());
        // Check for keywords from your actual error message
        String msg = result.getErrorMessage();
        assertTrue(msg.contains("locked") || msg.contains("Try again"), "Error message should mention lock or time.");
    }
}