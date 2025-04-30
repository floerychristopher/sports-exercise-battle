package com.seb.controller;

import com.seb.model.User;
import com.seb.repository.UserRepository;
import com.seb.security.PasswordUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserController {
    private final UserRepository userRepository;

    public UserController() {
        this.userRepository = new UserRepository();
    }

    // Register
    public Map<String, Object> register(String username, String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Check if username exists
            Optional<User> existingUser = userRepository.findByUsername(username);
            if (existingUser.isPresent()) {
                response.put("success", false);
                response.put("message", "Username already exists");
                return response;
            }

            // Hash password
            String passwordHash = PasswordUtil.hashPassword(password);

            // Create new user
            User newUser = new User(username, passwordHash);
            User createdUser = userRepository.createUser(newUser);

            // Generate auth token
            String token = userRepository.createAuthToken(createdUser.getUserId(), username);

            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("userId", createdUser.getUserId());
            response.put("token", token);

        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find user by username
            Optional<User> userOpt = userRepository.findByUsername(username);

            if (!userOpt.isPresent()) {
                response.put("success", false);
                response.put("message", "Invalid username or password");
                return response;
            }

            User user = userOpt.get();

            // Verify password
            if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                response.put("success", false);
                response.put("message", "Invalid username or password");
                return response;
            }

            // Generate auth token with username for the new format
            String token = userRepository.createAuthToken(user.getUserId(), username);

            response.put("success", true);
            response.put("message", "Login successful");
            response.put("userId", user.getUserId());
            response.put("token", token);
            response.put("elo", user.getElo());

        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    // Validate auth token
    public boolean validateToken(String token) {
        try {
            return userRepository.validateToken(token).isPresent();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Get user ID from token
     */
    public Optional<Integer> getUserIdFromToken(String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                String token = authHeader.substring("Basic ".length());
                return userRepository.validateToken(token);
            }
            return Optional.empty();
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    /**
     * Extract username from auth header
     */
    public String getUsernameFromToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String token = authHeader.substring("Basic ".length());
            if (token.contains("-sebToken")) {
                return token.split("-sebToken")[0];
            }
        }
        return null;
    }

    /**
     * Check if user can access profile
     */
    public boolean canAccessProfile(String authHeader, String profileUsername) {
        String tokenUsername = getUsernameFromToken(authHeader);
        return tokenUsername != null && tokenUsername.equals(profileUsername);
    }
}