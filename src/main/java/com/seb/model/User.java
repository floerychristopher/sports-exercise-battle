package com.seb.model;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String username;
    private String passwordHash;
    private int elo;
    private LocalDateTime creationDate;

    // Constructor without userId (for new users)
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.elo = 1000; // Default ELO
        this.creationDate = LocalDateTime.now();
    }

    // Constructor with all fields (for database retrieval)
    public User(int userId, String username, String passwordHash, int elo, LocalDateTime creationDate) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.elo = elo;
        this.creationDate = creationDate;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", elo=" + elo +
                ", creationDate=" + creationDate +
                '}';
    }
}