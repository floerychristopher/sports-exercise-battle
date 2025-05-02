package com.seb.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testNewUserConstructor() {
        // Arrange
        String username = "testUser";
        String passwordHash = "hashedPassword";

        // Act
        User user = new User(username, passwordHash);

        // Assert
        assertEquals(username, user.getUsername(), "Username should match");
        assertEquals(passwordHash, user.getPasswordHash(), "Password hash should match");
        assertEquals(1000, user.getElo(), "New user should have 1000 ELO");
        assertNotNull(user.getCreationDate(), "Creation date should not be null");
    }

    @Test
    public void testExistingUserConstructor() {
        // Arrange
        int userId = 1;
        String username = "testUser";
        String passwordHash = "hashedPassword";
        int elo = 1200;
        LocalDateTime creationDate = LocalDateTime.now().minusDays(5);

        // Act
        User user = new User(userId, username, passwordHash, elo, creationDate);

        // Assert
        assertEquals(userId, user.getUserId(), "User ID should match");
        assertEquals(username, user.getUsername(), "Username should match");
        assertEquals(passwordHash, user.getPasswordHash(), "Password hash should match");
        assertEquals(elo, user.getElo(), "ELO should match");
        assertEquals(creationDate, user.getCreationDate(), "Creation date should match");
    }
}