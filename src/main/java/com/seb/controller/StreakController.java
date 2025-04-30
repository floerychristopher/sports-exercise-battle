package com.seb.controller;

import com.seb.model.UserStreak;
import com.seb.repository.StreakRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StreakController {
    private final StreakRepository streakRepository;

    public StreakController() {
        this.streakRepository = new StreakRepository();
    }

    /**
     * Get a user's streak information
     */
    public Map<String, Object> getUserStreak(int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<UserStreak> streakOpt = streakRepository.getUserStreak(userId);

            response.put("success", true);

            if (streakOpt.isPresent()) {
                UserStreak streak = streakOpt.get();
                response.put("currentStreak", streak.getCurrentStreak());
                response.put("longestStreak", streak.getLongestStreak());
                response.put("lastActive", streak.getLastActive());
            } else {
                response.put("currentStreak", 0);
                response.put("longestStreak", 0);
                response.put("lastActive", null);
            }

        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get streak leaderboard
     */
    public Map<String, Object> getStreakLeaderboard() {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("success", true);
            response.putAll(streakRepository.getStreakLeaderboard());
        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }
}