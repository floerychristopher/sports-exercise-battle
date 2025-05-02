package com.seb.repository;

import com.seb.config.DatabaseConfig;
import com.seb.model.UserStreak;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class StreakRepository {
    private final DatabaseConfig dbConfig;

    public StreakRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    // Get user streak info
    public Optional<UserStreak> getUserStreak(int userId) throws SQLException {
        String sql = "SELECT user_id, current_streak, longest_streak, last_active FROM user_streaks WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                UserStreak streak = new UserStreak(
                        rs.getInt("user_id"),
                        rs.getInt("current_streak"),
                        rs.getInt("longest_streak"),
                        rs.getDate("last_active") != null ? rs.getDate("last_active").toLocalDate() : null
                );
                return Optional.of(streak);
            } else {
                return Optional.empty();
            }
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Update user streak when they record a pushup session
    public UserStreak updateStreak(int userId) throws SQLException {

        LocalDate today = LocalDate.now();

        // Get or create user streak
        Optional<UserStreak> streakOpt = getUserStreak(userId);
        UserStreak streak;

        if (streakOpt.isPresent()) {
            streak = streakOpt.get();

            // Calculate new streak based on last active date
            if (streak.getLastActive() == null) {
                // First time recording, start streak at 1
                streak.setCurrentStreak(1);
                streak.setLongestStreak(1);
            } else if (streak.getLastActive().equals(today)) {
                // Already recorded today (streak stays the same)
                // Do nothing
            } else if (streak.getLastActive().equals(today.minusDays(1))) {
                // Recorded yesterday (increment streak)
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);

                // Update longest streak if needed
                if (streak.getCurrentStreak() > streak.getLongestStreak()) {
                    streak.setLongestStreak(streak.getCurrentStreak());
                }
            } else {
                // Streak broken, start new streak
                streak.setCurrentStreak(1);
            }

            // Update last active date
            streak.setLastActive(today);

            // Update in database
            updateStreakInDb(streak);
        } else {
            // Create new streak entry
            streak = new UserStreak(userId, 1, 1, today);
            createStreakInDb(streak);
        }

        return streak;
    }

    // Create new streak entry in DB
    private void createStreakInDb(UserStreak streak) throws SQLException {
        String sql = "INSERT INTO user_streaks (user_id, current_streak, longest_streak, last_active) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, streak.getUserId());
            stmt.setInt(2, streak.getCurrentStreak());
            stmt.setInt(3, streak.getLongestStreak());
            stmt.setDate(4, streak.getLastActive() != null ? Date.valueOf(streak.getLastActive()) : null);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException("Failed to create user streak");
            }
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Update existing streak entry in DB
    private void updateStreakInDb(UserStreak streak) throws SQLException {
        String sql = "UPDATE user_streaks SET current_streak = ?, longest_streak = ?, last_active = ? WHERE user_id = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, streak.getCurrentStreak());
            stmt.setInt(2, streak.getLongestStreak());
            stmt.setDate(3, streak.getLastActive() != null ? Date.valueOf(streak.getLastActive()) : null);
            stmt.setInt(4, streak.getUserId());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new SQLException("Failed to update user streak");
            }
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }

    // Get streaks of all users (for scoreboard)
    public Map<String, Object> getStreakLeaderboard() throws SQLException {
        String sql = "SELECT us.user_id, u.username, us.current_streak, us.longest_streak, us.last_active " +
                "FROM user_streaks us " +
                "JOIN users u ON us.user_id = u.user_id " +
                "ORDER BY us.current_streak DESC, us.longest_streak DESC";

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> currentStreaks = new ArrayList<>();
        List<Map<String, Object>> longestStreaks = new ArrayList<>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbConfig.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> streakInfo = new HashMap<>();
                streakInfo.put("userId", rs.getInt("user_id"));
                streakInfo.put("username", rs.getString("username"));
                streakInfo.put("currentStreak", rs.getInt("current_streak"));
                streakInfo.put("longestStreak", rs.getInt("longest_streak"));
                streakInfo.put("lastActive", rs.getDate("last_active") != null ?
                        rs.getDate("last_active").toLocalDate() : null);

                currentStreaks.add(streakInfo);
            }

            // Sort for longest streaks
            currentStreaks.sort((a, b) ->
                    Integer.compare((Integer)b.get("longestStreak"), (Integer)a.get("longestStreak")));

            // Create copy for longest streaks leaderboard
            for (Map<String, Object> streak : currentStreaks) {
                longestStreaks.add(new HashMap<>(streak));
            }

            result.put("currentStreaks", currentStreaks);
            result.put("longestStreaks", longestStreaks);

            return result;
        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) dbConfig.closeConnection(conn);
        }
    }
}