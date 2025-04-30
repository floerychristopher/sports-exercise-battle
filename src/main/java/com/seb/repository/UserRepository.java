package com.seb.repository;

import com.seb.config.DatabaseConfig;
import com.seb.model.User;
import com.seb.model.UserProfile;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class UserRepository {
    // Reference to DatabaseConfig
    private final DatabaseConfig dbConfig;

    public UserRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    // Create new user in database
    public User createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash, elo, creation_date) VALUES (?, ?, ?, ?) RETURNING user_id";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setInt(3, user.getElo());
            stmt.setTimestamp(4, Timestamp.valueOf(user.getCreationDate()));

            rs = stmt.executeQuery();

            if (rs.next()) {
                user.setUserId(rs.getInt("user_id"));
                return user;
            } else {
                throw new SQLException("Failed to create user, no ID returned");
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Find user by username
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, elo, creation_date FROM users WHERE username = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);

            rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getInt("elo"),
                        rs.getTimestamp("creation_date").toLocalDateTime()
                );
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    /**
     * Create a new authentication token in the format "username-sebToken"
     */
    public String createAuthToken(int userId, String username) throws SQLException {
        // Generate token in the format "username-sebToken"
        String token = username + "-sebToken";
        String sql = "INSERT INTO auth_tokens (user_id, token) VALUES (?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, token);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 1) {
                return token;
            } else {
                throw new SQLException("Failed to create auth token");
            }
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    /**
     * Validate a token
     */
    public Optional<Integer> validateToken(String token) throws SQLException {
        String sql = "SELECT user_id FROM auth_tokens WHERE token = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, token);

            rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getInt("user_id"));
            } else {
                return Optional.empty();
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    /**
     * Extract username from token
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

    // Generate random token
    private String generateToken() {
        return java.util.UUID.randomUUID().toString();
    }


    // Add these methods to the UserRepository class

    /**
     * Get user profile with all fields
     */
    public Optional<UserProfile> getUserProfile(int userId) throws SQLException {
        String sql = "SELECT user_id, display_name, bio, image FROM user_profiles WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                UserProfile profile = new UserProfile(
                        rs.getInt("user_id"),
                        rs.getString("display_name"),
                        rs.getString("bio"),
                        rs.getString("image")
                );
                return Optional.of(profile);
            } else {
                return Optional.empty();
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    /**
     * Create or update user profile with all fields
     */
    public void saveUserProfile(UserProfile profile) throws SQLException {
        // Check if profile exists
        Optional<UserProfile> existingProfile = getUserProfile(profile.getUserId());

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConfig.getConnection();

            if (existingProfile.isPresent()) {
                // Update existing profile
                String sql = "UPDATE user_profiles SET display_name = ?, bio = ?, image = ? WHERE user_id = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, profile.getDisplayName());
                stmt.setString(2, profile.getBio());
                stmt.setString(3, profile.getImage());
                stmt.setInt(4, profile.getUserId());
            } else {
                // Create new profile
                String sql = "INSERT INTO user_profiles (user_id, display_name, bio, image) VALUES (?, ?, ?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setInt(1, profile.getUserId());
                stmt.setString(2, profile.getDisplayName());
                stmt.setString(3, profile.getBio());
                stmt.setString(4, profile.getImage());
            }

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException("Failed to save user profile");
            }
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }


    /**
     * Get user by ID
     */
    public Optional<User> findById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, password_hash, elo, creation_date FROM users WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getInt("elo"),
                        rs.getTimestamp("creation_date").toLocalDateTime()
                );
                return Optional.of(user);
            } else {
                return Optional.empty();
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    /**
     * Update user's ELO
     */
    public void updateElo(int userId, int newElo) throws SQLException {
        String sql = "UPDATE users SET elo = ? WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, newElo);
            stmt.setInt(2, userId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException("Failed to update user ELO");
            }
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    /**
     * Get all users for scoreboard
     */
    public List<Map<String, Object>> getScoreboard() throws SQLException {
        String sql = "SELECT u.user_id, u.username, u.elo, " +
                "(SELECT COUNT(*) FROM pushup_records pr WHERE pr.user_id = u.user_id) as total_entries, " +
                "(SELECT COALESCE(SUM(count), 0) FROM pushup_records pr WHERE pr.user_id = u.user_id) as total_pushups " +
                "FROM users u ORDER BY u.elo DESC";

        List<Map<String, Object>> scoreboard = new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("userId", rs.getInt("user_id"));
                entry.put("username", rs.getString("username"));
                entry.put("elo", rs.getInt("elo"));
                entry.put("totalEntries", rs.getInt("total_entries"));
                entry.put("totalPushups", rs.getInt("total_pushups"));

                scoreboard.add(entry);
            }

            return scoreboard;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }
}