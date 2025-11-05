package com.example.magpie_wingman.data.policy;
/**
 * Business rules for events: creation validation and join/registration checks.
 *
 */
public class EventPolicy {
    public void validateEventCreation(long currentTime, long eventStartTime, long eventEndTime, int eventCapacity){
        if (eventCapacity < 0)  throw new IllegalArgumentException("Capacity must be > 0");
        if (eventStartTime < currentTime) throw new IllegalArgumentException("Start time must be in the future.");
        if (eventEndTime <= eventStartTime) throw new IllegalArgumentException("End time must be after start time");
    }

    public boolean validateRegistrationPeriod(long currentTime, long registrationStartTime, long registrationEndTime) {
        return currentTime >= registrationStartTime && currentTime <= registrationEndTime;
    }

    public boolean validateCapacity(int capacity, int attendeeCount) {
        return attendeeCount < capacity;
    }

}
