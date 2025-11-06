package com.example.magpie_wingman.data.model;

public class UserProfile {
    private String name;
    private String role; // e.g., "Entrant", "Organizer", "Admin"
    private String profileImageUrl;

    public UserProfile(String name, String role) {
        this.name = name;
        this.role = role;
        this.profileImageUrl = null; // Placeholder for now
    }

    // --- Getters ---
    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
