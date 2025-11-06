package com.example.magpie_wingman.data.model;

public class Organizer extends User {
    public Organizer() {
        super();
        this.userRole = UserRole.ORGANIZER;
    }
    public Organizer(String userId, String userName, String userEmail, String userPhone, String userDeviceId) {
        super(userId, userName, userEmail, userPhone, userDeviceId, UserRole.ORGANIZER);
    }
}
