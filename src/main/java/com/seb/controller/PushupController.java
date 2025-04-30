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
     * Record pushups and participate in tournament
     */
    public Map<String, Object> recordPushups(int userId, int count) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate count
            if (count <= 0) {
                response.put("success", false);
                response.put("message", "Pushup count must be greater than zero");
                return response;
            }

            // Create pushup record
            PushupRecord record = new PushupRecord(userId, count);
            PushupRecord savedRecord = pushupRepository.addRecord(record);

            // Get or create active tournament
            Tournament tournament = tournamentRepository.getOrCreateActiveTournament();

            // Add user to tournament
            TournamentParticipant participant = tournamentRepository.addParticipant(
                    tournament.getTournamentId(), userId, count);

            // Update user streak (unique feature)
            UserStreak streak = streakRepository.updateStreak(userId);

            // Check if tournament is expired after adding participant
            if (tournament.isExpired()) {
                tournamentRepository.completeTournament(tournament.getTournamentId());
                response.put("tournamentCompleted", true);
            } else {
                response.put("tournamentCompleted", false);

                // Calculate remaining time in seconds
                long remainingSeconds = java.time.Duration.between(
                        java.time.LocalDateTime.now(),
                        tournament.getStartTime().plusMinutes(2)
                ).getSeconds();

                response.put("remainingTime", Math.max(0, remainingSeconds));
            }

            response.put("success", true);
            response.put("message", "Pushups recorded successfully");
            response.put("recordId", savedRecord.getRecordId());
            response.put("tournamentId", tournament.getTournamentId());
            response.put("yourTotal", participant.getTotalPushups());

            // Add streak information
            response.put("streak", Map.of(
                    "currentStreak", streak.getCurrentStreak(),
                    "longestStreak", streak.getLongestStreak()
            ));

            // Add streak achievement messages
            if (streak.getCurrentStreak() == 3) {
                response.put("streakAchievement", "3-Day Streak! Keep going!");
            } else if (streak.getCurrentStreak() == 7) {
                response.put("streakAchievement", "One Week Streak! You're on fire!");
            } else if (streak.getCurrentStreak() == 30) {
                response.put("streakAchievement", "30-Day Streak! Amazing commitment!");
            }

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