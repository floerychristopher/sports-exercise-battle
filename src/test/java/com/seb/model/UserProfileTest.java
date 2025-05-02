package com.seb.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserProfileTest {

    @Test
    public void testBasicConstructor() {
        // Arrange
        int userId = 1;
        String displayName = "Test User";

        // Act
        UserProfile profile = new UserProfile(userId, displayName);

        // Assert
        assertEquals(userId, profile.getUserId(), "User ID should match");
        assertEquals(displayName, profile.getDisplayName(), "Display name should match");
        assertNull(profile.getBio(), "Bio should be null");
        assertNull(profile.getImage(), "Image should be null");
    }

    @Test
    public void testFullConstructor() {
        // Arrange
        int userId = 1;
        String displayName = "Test User";
        String bio = "This is my bio";
        String image = ":-)";

        // Act
        UserProfile profile = new UserProfile(userId, displayName, bio, image);

        // Assert
        assertEquals(userId, profile.getUserId(), "User ID should match");
        assertEquals(displayName, profile.getDisplayName(), "Display name should match");
        assertEquals(bio, profile.getBio(), "Bio should match");
        assertEquals(image, profile.getImage(), "Image should match");
    }
}