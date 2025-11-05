package com.example.magpie_wingman.data.model; // This matches its location

public class Entrant {
    private String name;
    private String profileImageUrl;
    private String status;

    public Entrant(String name, String status) {
        this.name = name;
        this.status = status;
        this.profileImageUrl = null;
    }

    // --- Getters ---
    public String getName() {
        return name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getStatus() {
        return status;
    }
}


/*
for running in android studios

@id/organizerNewEventFragment
@id/entrantEventsFragment
@id/adminProfilesFragment
@id/FirstFragment
 */