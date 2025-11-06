package com.example.magpie_wingman.data.model;

public class UserProfile {
    private String userId;
    private String name;
    private UserRole role;
    private String profileImageUrl;

    public UserProfile(String userId, String name, UserRole role) {
        this.userId = userId; this.name = name; this.role = role; this.profileImageUrl = null;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl( String url) { this.profileImageUrl = url; }
}