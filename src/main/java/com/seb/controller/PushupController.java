package com.seb.controller;

import com.seb.repository.StreakRepository;
import com.seb.model.PushupRecord;
import com.seb.model.Tournament;
import com.seb.model.TournamentParticipant;
import com.seb.model.UserStreak;
import com.seb.repository.PushupRepository;
import com.seb.repository.TournamentRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class PushupController {
    private final PushupRepository pushupRepository;
    private final TournamentRepository tournamentRepository;
    private final StreakRepository streakRepository;

    public PushupController() {
        this.pushupRepository = new PushupRepository();
        this.tournamentRepository = new TournamentRepository();
        this.streakRepository = new StreakRepository();
    }

    /**
     * Record pushups with duration
     */
    public Map<String, Object> recordPushups(int userId, int count, Integer durationInSeconds) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate count
            if (count <= 0) {
                response.put("success", false);
                response.put("message", "Count must be greater than zero");
                return response;
            }

            // Use default duration if not provided
            int duration = (durationInSeconds != null) ? durationInSeconds : 120; // Default 2 minutes

            // Create pushup record with duration
            PushupRecord record = new PushupRecord(userId, count, duration);
            PushupRecord savedRecord = pushupRepository.addRecord(record);

            // Rest of your implementation
            // ...

        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get user's pushup history
     */
    public Map<String, Object> getUserHistory(int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("success", true);
            response.put("history", pushupRepository.getUserHistory(userId));
        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    /**
     * Get user's stats
     */
    public Map<String, Object> getUserStats(int userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> stats = pushupRepository.getUserStats(userId);

            response.put("success", true);
            response.putAll(stats);
        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }
}