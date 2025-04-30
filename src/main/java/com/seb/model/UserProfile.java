package com.seb.model;

public class UserProfile {
    private int userId;
    private String displayName;
    private String bio;
    private String image;

    // Basic constructor
    public UserProfile(int userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    // Full constructor
    public UserProfile(int userId, String displayName, String bio, String image) {
        this.userId = userId;
        this.displayName = displayName;
        this.bio = bio;
        this.image = image;
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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}