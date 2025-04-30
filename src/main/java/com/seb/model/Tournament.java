package com.seb.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Tournament {
    private int tournamentId;
    private LocalDateTime startTime;
    private String status; // "ACTIVE" or "COMPLETED"
    private List<TournamentParticipant> participants;

    // Constructor for new tournaments
    public Tournament() {
        this.startTime = LocalDateTime.now();
        this.status = "ACTIVE";
        this.participants = new ArrayList<>();
    }

    // Constructor for existing tournaments
    public Tournament(int tournamentId, LocalDateTime startTime, String status) {
        this.tournamentId = tournamentId;
        this.startTime = startTime;
        this.status = status;
        this.participants = new ArrayList<>();
    }

    // Getters and setters
    public int getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(int tournamentId) {
        this.tournamentId = tournamentId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<TournamentParticipant> getParticipants() {
        return participants;
    }

    public void addParticipant(TournamentParticipant participant) {
        this.participants.add(participant);
    }

    public boolean isExpired() {
        // A tournament expires after 2 minutes
        return LocalDateTime.now().isAfter(startTime.plusMinutes(2));
    }
}