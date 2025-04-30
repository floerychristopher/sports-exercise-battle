package com.seb.model;

import java.time.LocalDateTime;

public class PushupRecord {
    private int recordId;
    private int userId;
    private int count;
    private int durationInSeconds;
    private LocalDateTime recordDate;

    // Constructor for new records
    public PushupRecord(int userId, int count) {
        this.userId = userId;
        this.count = count;
        this.recordDate = LocalDateTime.now();
    }

    // Constructor for existing records
    public PushupRecord(int recordId, int userId, int count, LocalDateTime recordDate) {
        this.recordId = recordId;
        this.userId = userId;
        this.count = count;
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

    public LocalDateTime getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDateTime recordDate) {
        this.recordDate = recordDate;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }
}