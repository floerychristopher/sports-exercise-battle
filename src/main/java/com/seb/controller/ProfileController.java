package com.seb.controller;

import com.seb.model.UserProfile;
import com.seb.repository.UserRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProfileController {
    private final UserRepository userRepository;

    public ProfileController() {
        this.userRepository = new UserRepository();
    }

    /**
     * Get user profile
     */
    public Map<String, Object> getProfile(int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Get user info
            Optional<com.seb.model.User> userOpt = userRepository.findById(userId);

            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            com.seb.model.User user = userOpt.get();

            // Get profile info (might not exist yet)
            Optional<UserProfile> profileOpt = userRepository.getUserProfile(userId);

            response.put("success", true);
            response.put("userId", user.getUserId());
            response.put("username", user.getUsername());
            response.put("elo", user.getElo());
            response.put("creationDate", user.getCreationDate());

            // Add display name if profile exists
            if (profileOpt.isPresent()) {
                response.put("displayName", profileOpt.get().getDisplayName());
            } else {
                response.put("displayName", user.getUsername()); // Default to username
            }

        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Update user profile with Name, Bio, Image
     */
    public Map<String, Object> updateProfile(int userId, String name, String bio, String image) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if user exists
            Optional<com.seb.model.User> userOpt = userRepository.findById(userId);

            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            // Update or create profile with new fields
            UserProfile profile = new UserProfile(userId, name);
            profile.setBio(bio);
            profile.setImage(image);

            userRepository.saveUserProfile(profile);

            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("name", name);
            response.put("bio", bio);
            response.put("image", image);

        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get scoreboard
     */
    public Map<String, Object> getScoreboard() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("success", true);
            response.put("scoreboard", userRepository.getScoreboard());
        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }
}