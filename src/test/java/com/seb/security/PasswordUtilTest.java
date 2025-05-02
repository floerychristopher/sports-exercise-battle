package com.seb.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordUtilTest {

    @Test
    public void testPasswordHashing() {
        // Arrange
        String password = "secretPassword123";

        // Act
        String hashedPassword = PasswordUtil.hashPassword(password);

        // Assert
        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(password, hashedPassword, "Hashed password should not be the same as the original");
        assertTrue(hashedPassword.contains(":"), "Hashed password should contain salt separator");
    }

    @Test
    public void testPasswordVerification_ValidPassword() {
        // Arrange
        String password = "secretPassword123";
        String hashedPassword = PasswordUtil.hashPassword(password);

        // Act
        boolean result = PasswordUtil.verifyPassword(password, hashedPassword);

        // Assert
        assertTrue(result, "Password verification should pass with correct password");
    }

    @Test
    public void testPasswordVerification_InvalidPassword() {
        // Arrange
        String password = "secretPassword123";
        String wrongPassword = "wrongPassword";
        String hashedPassword = PasswordUtil.hashPassword(password);

        // Act
        boolean result = PasswordUtil.verifyPassword(wrongPassword, hashedPassword);

        // Assert
        assertFalse(result, "Password verification should fail with wrong password");
    }
}