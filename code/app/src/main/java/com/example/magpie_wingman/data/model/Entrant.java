package com.example.magpie_wingman.data.model; // This matches its location

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Entrant extends User{
    private List<String> waitlistedEvents = new ArrayList<>();
    private List<String> enrolledEvents = new ArrayList<>();
    private String profileImageUrl;


    public Entrant(String userId,
                   String name,
                   @Nullable String profileImageUrl,
                   @Nullable String email,
                   @Nullable String phone,
                   @Nullable String deviceId) {
        super(userId, name, /*isOrganizer*/ false, profileImageUrl, email, phone, deviceId);
    }

    public List<String> getWaitlistedEvents() { return waitlistedEvents; }
    public List<String> getEnrolledEvents()   { return enrolledEvents; }

}


/*
for running in android studios

@id/organizerNewEventFragment
@id/entrantEventsFragment
@id/adminProfilesFragment
@id/FirstFragment
 */