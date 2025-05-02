package com.seb.model;

import java.time.LocalDateTime;

public class PushupRecord {
    private int recordId;
    private int userId;
    private int count;
    private int durationInSeconds;
    private LocalDateTime recordDate;

    // Constructor for new records with count only (default duration)
    public PushupRecord(int userId, int count) {
        this.userId = userId;
        this.count = count;
        this.durationInSeconds = 120; // Default
        this.recordDate = LocalDateTime.now();
    }

    // Constructor for new records with count and duration
    public PushupRecord(int userId, int count, int durationInSeconds) {
        this.userId = userId;
        this.count = count;
        this.durationInSeconds = durationInSeconds;
        this.recordDate = LocalDateTime.now();
    }

    // Constructor for existing records
    public PushupRecord(int recordId, int userId, int count, int durationInSeconds, LocalDateTime recordDate) {
        this.recordId = recordId;
        this.userId = userId;
        this.count = count;
        this.durationInSeconds = durationInSeconds;
        this.recordDate = recordDate;
    }

    // Getters and setters
    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public LocalDateTime getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDateTime recordDate) {
        this.recordDate = recordDate;
    }
}