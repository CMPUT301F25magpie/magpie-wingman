package com.example.magpie_wingman.data.model;

public class Administrator extends User {
    public Administrator() {
        super();
        this.userRole = UserRole.ADMIN;
    }
    public Administrator(String userId, String userName, String userEmail, String userPhone, String userDeviceId) {
        super(userId, userName, userEmail, userPhone, userDeviceId, UserRole.ADMIN);
    }
}
