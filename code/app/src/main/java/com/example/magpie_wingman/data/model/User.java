package com.example.magpie_wingman.data.model;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model stored at Firestore: users/{userId}.
 * Source of truth for name/role/avatar/contact fields.
 *
 * Doc ID is the canonical userId; we don't duplicate it as a field in Firestore.
 */
public class User {

    // Firestore document id
    private final String userId;

    // Core fields
    private String name;
    private boolean isOrganizer;                 // true = ORGANIZER, false = ENTRANT
    @Nullable private String profileImageUrl;
    @Nullable private String email;
    @Nullable private String phone;
    @Nullable private String deviceId;

    public User(String userId,
                String name,
                boolean isOrganizer,
                @Nullable String profileImageUrl,
                @Nullable String email,
                @Nullable String phone,
                @Nullable String deviceId) {
        this.userId = userId;
        this.name = (name != null && !name.isEmpty()) ? name : userId;
        this.isOrganizer = isOrganizer;
        this.profileImageUrl = profileImageUrl;
        this.email = email;
        this.phone = phone;
        this.deviceId = deviceId;
    }

    /** Minimal constructor. don't need contact/device fields at creation time. */
    public User(String userId, String name, boolean isOrganizer, @Nullable String profileImageUrl, String email, String phone, String deviceId, Date birthday) {
        this(userId, name, isOrganizer, profileImageUrl, null, null, null);
    }

    /** Build from Firestore */
    public static User from(DocumentSnapshot d) {
        String id        = d.getId();
        String name      = d.getString("name");
        Boolean isOrg    = d.getBoolean("isOrganizer");
        String img       = d.getString("profileImageUrl");
        String email     = d.getString("email");
        String phone     = d.getString("phone");
        String deviceId  = d.getString("deviceId");
        return new User(id, name, isOrg != null && isOrg, img, email, phone, deviceId);
    }

    /** Map for Firestore writes to users/{userId} */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("isOrganizer", isOrganizer);
        m.put("profileImageUrl", profileImageUrl);
        if (email != null)    m.put("email", email);
        if (phone != null)    m.put("phone", phone);
        if (deviceId != null) m.put("deviceId", deviceId);
        return m;
    }

    /** Role as an enum  */
    public UserRole getRole() {
        // if (isAdmin) return UserRole.ADMIN;
        return isOrganizer ? UserRole.ORGANIZER : UserRole.ENTRANT;
    }

    /** Projection into a profile. */
    public UserProfile toProfile() {
        return new UserProfile(
                userId,
                name,
                getRole(),
                profileImageUrl
        );
    }

    // --- Getters/Setters ---

    public String getUserId() { return userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = (name != null && !name.isEmpty()) ? name : userId; }

    public boolean isOrganizer() { return isOrganizer; }
    public void setOrganizer(boolean organizer) { isOrganizer = organizer; }

    @Nullable public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(@Nullable String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    @Nullable public String getEmail() { return email; }
    public void setEmail(@Nullable String email) { this.email = email; }

    @Nullable public String getPhone() { return phone; }
    public void setPhone(@Nullable String phone) { this.phone = phone; }

    @Nullable public String getDeviceId() { return deviceId; }
    public void setDeviceId(@Nullable String deviceId) { this.deviceId = deviceId; }
}