package com.example.magpie_wingman.data.model;

import androidx.annotation.Nullable;

import java.util.Date;

public class Administrator extends User {

    public Administrator(String userId,
                         String name,
                         @Nullable String profileImageUrl,
                         @Nullable String email,
                         @Nullable String phone,
                         @Nullable String deviceId,
                         @Nullable Date birthday) {
        // Admins are not organizers; pass false
        super(userId, name, /*isOrganizer*/ false, profileImageUrl, email, phone, deviceId, birthday);
    }

    @Override
    public UserRole getRole() {
        return UserRole.ADMIN;
    }
}