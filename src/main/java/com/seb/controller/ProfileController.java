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

    // Get user profile
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
            response.put("UserId", user.getUserId());
            response.put("Username", user.getUsername());
            response.put("ELO", user.getElo());

            // Add profile fields if profile exists
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                response.put("Name", profile.getDisplayName());
                response.put("Bio", profile.getBio());
                response.put("Image", profile.getImage());
            } else {
                // Default values
                response.put("Name", user.getUsername());
                response.put("Bio", "");
                response.put("Image", "");
            }

        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    // Update user profile
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

            // Update or create profile
            UserProfile profile = new UserProfile(userId, name, bio, image);
            userRepository.saveUserProfile(profile);

            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("Name", name);
            response.put("Bio", bio);
            response.put("Image", image);

        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    // Get scoreboard
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