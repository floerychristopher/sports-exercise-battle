package com.seb.integration;

import com.seb.controller.UserController;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class UserRegistrationIntegrationTest {

    @Test
    public void testUserRegistrationAndLogin() {
        // Arrange
        UserController userController = new UserController();
        String username = "integrationTest" + System.currentTimeMillis(); // Unique username
        String password = "testPassword123";

        // Act - Register user
        Map<String, Object> registrationResult = userController.register(username, password);

        // Assert - Registration successful
        assertTrue((Boolean)registrationResult.get("success"), "Registration should succeed");
        assertNotNull(registrationResult.get("userId"), "User ID should be returned");
        assertNotNull(registrationResult.get("token"), "Token should be returned");

        // Act - Login with created user
        Map<String, Object> loginResult = userController.login(username, password);

        // Assert - Login successful
        assertTrue((Boolean)loginResult.get("success"), "Login should succeed");
        assertEquals(registrationResult.get("userId"), loginResult.get("userId"), "User ID should match");
        assertEquals(1000, loginResult.get("elo"), "Initial ELO should be 1000");
    }

    @Test
    public void testDuplicateRegistration() {
        // Arrange
        UserController userController = new UserController();
        String username = "duplicateTest" + System.currentTimeMillis(); // Unique username
        String password = "testPassword123";

        // Act - Register first time
        userController.register(username, password);

        // Act - Try to register again with same username
        Map<String, Object> result = userController.register(username, password);

        // Assert
        assertFalse((Boolean)result.get("success"), "Duplicate registration should fail");
        assertEquals("Username already exists", result.get("message"), "Error message should indicate duplicate username");
    }
}