package com.seb.model;

import java.time.LocalDate;

public class UserStreak {
    private int userId;
    private int currentStreak;
    private int longestStreak;
    private LocalDate lastActive;

    public UserStreak(int userId, int currentStreak, int longestStreak, LocalDate lastActive) {
        this.userId = userId;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.lastActive = lastActive;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastActive() {
        return lastActive;
    }

    public void setLastActive(LocalDate lastActive) {
        this.lastActive = lastActive;
    }
}