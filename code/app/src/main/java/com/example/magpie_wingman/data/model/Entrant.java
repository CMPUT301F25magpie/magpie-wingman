package com.example.magpie_wingman.data.model; // This matches its location

import java.util.ArrayList;
import java.util.List;

public class Entrant extends User{
    private List<String> waitlistedEvents = new ArrayList<>();
    private List<String> enrolledEvents = new ArrayList<>();
    private String profileImageUrl;

    public Entrant(String userId, String userName, String userEmail, String userPhone, String userDeviceId) {
        super(userId, userName, userEmail, userPhone, userDeviceId);
        this.waitlistedEvents = new ArrayList<>();
        this.enrolledEvents = new ArrayList<>();
    }


    public List<String> getWaitlistedEvents() {
        return waitlistedEvents;
    }

    public void setWaitlistedEvents(List<String> waitlistedEvents) {
        this.waitlistedEvents = waitlistedEvents;
    }

    public List<String> getEnrolledEvents() {
        return enrolledEvents;
    }

    public void setEnrolledEvents(List<String> enrolledEvents) {
        this.enrolledEvents = enrolledEvents;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}


/*
for running in android studios

@id/organizerNewEventFragment
@id/entrantEventsFragment
@id/adminProfilesFragment
@id/FirstFragment
 */