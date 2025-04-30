package com.seb.model;

public class UserProfile {
    private int userId;
    private String displayName;

    public UserProfile(int userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    // Getters and setters
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}