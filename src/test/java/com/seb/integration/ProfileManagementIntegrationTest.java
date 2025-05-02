package com.seb.integration;

import com.seb.controller.ProfileController;
import com.seb.controller.UserController;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class ProfileManagementIntegrationTest {

    @Test
    public void testProfileUpdateAndRetrieval() {
        // Arrange
        UserController userController = new UserController();
        ProfileController profileController = new ProfileController();

        // Create test user
        String username = "profile" + System.currentTimeMillis();
        Map<String, Object> registerResult = userController.register(username, "password");
        int userId = (Integer)registerResult.get("userId");

        // Act - Get initial profile
        Map<String, Object> initialProfile = profileController.getProfile(userId);

        // Assert - Initial profile
        assertTrue((Boolean)initialProfile.get("success"), "Profile retrieval should succeed");
        assertEquals(username, initialProfile.get("Name"), "Default name should match username");

        // Act - Update profile
        String displayName = "Updated Name";
        String bio = "This is my bio text";
        String image = ":-D";
        Map<String, Object> updateResult = profileController.updateProfile(userId, displayName, bio, image);

        // Assert - Update succeeded
        assertTrue((Boolean)updateResult.get("success"), "Profile update should succeed");

        // Act - Get updated profile
        Map<String, Object> updatedProfile = profileController.getProfile(userId);

        // Assert - Profile updated correctly
        assertEquals(displayName, updatedProfile.get("Name"), "Name should be updated");
        assertEquals(bio, updatedProfile.get("Bio"), "Bio should be updated");
        assertEquals(image, updatedProfile.get("Image"), "Image should be updated");
    }
}