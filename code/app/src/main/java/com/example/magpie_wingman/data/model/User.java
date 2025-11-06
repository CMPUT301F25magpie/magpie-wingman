package com.example.magpie_wingman.data.model;

import java.io.Serializable;

public class User implements Serializable {
     protected String userId;
     protected String userName;
     protected String userEmail;
     protected String userPhone;
     protected String userDeviceId;
     protected UserRole userRole;
     public User() {}

    public User(String userId, String userName, String userEmail, String userPhone, String userDeviceId, UserRole role) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhone = userPhone;
        this.userDeviceId = userDeviceId;
        this.userRole = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserDeviceId() {
        return userDeviceId;
    }

    public void setUserDeviceId(String userDeviceId) {
        this.userDeviceId = userDeviceId;
    }
}
