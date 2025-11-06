package com.example.magpie_wingman.data.model;

import androidx.annotation.Nullable;

/**
 * Lightweight UI model for profile rows/cards.
 * Not used for Firestore reads/writes.
 */
public class UserProfile {

    private final String userId;                 // needed for actions (open/delete, etc.)
    private final String name;                   // display label
    private final UserRole role;                 // ENTRANT or ORGANIZER
    @Nullable private final String profileImageUrl;

    public UserProfile(String userId, String name, UserRole role, @Nullable String profileImageUrl) {
        this.userId = userId;
        this.name = (name != null && !name.isEmpty()) ? name : userId;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
    }

    /** Convenience for building UI from the domain model, keeping fields consistent. */
    public static UserProfile from(User u) {
        return new UserProfile(
                u.getUserId(),
                u.getName(),
                u.getRole(),
                u.getProfileImageUrl()
        );
    }

    // Simple overload for mocks/tests
    public UserProfile(String userId, String name, UserRole role) {
        this(userId, name, role, null);
    }

    // --- Getters ---

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
    @Nullable public String getProfileImageUrl() { return profileImageUrl; }
}
