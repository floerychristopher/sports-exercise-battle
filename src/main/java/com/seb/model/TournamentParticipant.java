package com.seb.model;

public class TournamentParticipant {
    private int tournamentId;
    private int userId;
    private int totalPushups;
    private String username; // For display purposes

    public TournamentParticipant(int tournamentId, int userId, int totalPushups) {
        this.tournamentId = tournamentId;
        this.userId = userId;
        this.totalPushups = totalPushups;
    }

    public TournamentParticipant(int tournamentId, int userId, int totalPushups, String username) {
        this.tournamentId = tournamentId;
        this.userId = userId;
        this.totalPushups = totalPushups;
        this.username = username;
    }

    // Getters and setters
    public int getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(int tournamentId) {
        this.tournamentId = tournamentId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTotalPushups() {
        return totalPushups;
    }

    public void setTotalPushups(int totalPushups) {
        this.totalPushups = totalPushups;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void addPushups(int count) {
        this.totalPushups += count;
    }
}