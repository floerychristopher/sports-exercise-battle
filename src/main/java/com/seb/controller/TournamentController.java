package com.seb.controller;

import com.seb.model.Tournament;
import com.seb.repository.TournamentRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TournamentController {
    private final TournamentRepository tournamentRepository;

    public TournamentController() {
        this.tournamentRepository = new TournamentRepository();
    }

    // Get active tournament/create new tournament
    public Map<String, Object> getActiveTournament() {
        Map<String, Object> response = new HashMap<>();

        try {
            Tournament tournament = tournamentRepository.getOrCreateActiveTournament();

            response.put("success", true);
            response.put("tournamentId", tournament.getTournamentId());
            response.put("startTime", tournament.getStartTime());
            response.put("status", tournament.getStatus());
            response.put("participants", tournament.getParticipants());

            // Calculate remaining time in seconds
            long remainingSeconds = java.time.Duration.between(
                    java.time.LocalDateTime.now(),
                    tournament.getStartTime().plusMinutes(2)
            ).getSeconds();

            response.put("remainingTime", Math.max(0, remainingSeconds));

        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    // Get recent tournaments
    public Map<String, Object> getRecentTournaments(int limit) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("success", true);
            response.put("tournaments", tournamentRepository.getRecentTournaments(limit));
        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }

    // Get tournament logs
    public Map<String, Object> getTournamentLogs(int tournamentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            response.put("success", true);
            response.put("logs", tournamentRepository.getTournamentLogs(tournamentId));
        } catch (SQLException e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }

        return response;
    }
}