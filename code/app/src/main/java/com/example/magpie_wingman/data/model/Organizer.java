package com.example.magpie_wingman.data.model;

import androidx.annotation.Nullable;

public class Organizer extends User {

    public Organizer(String userId,
                     String name,
                     @Nullable String profileImageUrl,
                     @Nullable String email,
                     @Nullable String phone,
                     @Nullable String deviceId) {
        super(userId, name, /*isOrganizer*/ true, profileImageUrl, email, phone, deviceId);
    }
}
