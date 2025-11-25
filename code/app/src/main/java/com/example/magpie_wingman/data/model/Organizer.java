package com.example.magpie_wingman.data.model;

import androidx.annotation.Nullable;

import java.util.Date;

public class Organizer extends User {

    public Organizer(String userId,
                     String name,
                     @Nullable String profileImageUrl,
                     @Nullable String email,
                     @Nullable String phone,
                     @Nullable String deviceId,
                     @Nullable Date birthday) {
        //Removed 'birthday' from super() call
        super(userId, name, /*isOrganizer*/ true, profileImageUrl, email, phone, deviceId);
    }
}