package com.example.magpie_wingman.data.model;

import java.util.Date;

/**
 * Event an organizer creates/
 * Entrant membership is tracked on Entrant side as a list of event IDs
 */
public class Event {
    private String eventId;
    private String organizerId;
    private String eventName;
    private Date eventStartTime;
    private Date eventEndTime;
    private Date registrationStart;
    private Date registrationEnd;
    private String eventLocation;
    private String description;
    private String eventPosterURL;
    private int eventCapacity;


    private int waitlistCount;

    // required empty constructor for Firestore / deserialization
    public Event() {}

    public Event(String eventId,
                 String organizerId,
                 String eventName,
                 Date eventStartTime,
                 Date eventEndTime,
                 String eventLocation,
                 String description,
                 String eventPosterURL,
                 int eventCapacity) {

        this.eventId = eventId;
        this.organizerId = organizerId;
        this.eventName = eventName;

        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;

        this.eventLocation = eventLocation;

        // IMPORTANT: match the Firestore field "description"
        this.description = description;

        // Use the parameter, not null
        this.eventPosterURL = eventPosterURL;

        this.eventCapacity = eventCapacity;

        // New events start with an empty waitlist
        this.waitlistCount = 0;
    }

    // --- Getters ---
    public String getEventId() {
        return eventId;
    }
    public String getEventName() {
        return eventName;
    }
    public Date getEventStartTime() {
        return eventStartTime;
    }
    public Date getEventEndTime() { return eventEndTime;  }
    public Date getRegistrationStart() { return registrationStart; }
    public Date getRegistrationEnd() { return registrationEnd; }
    public String getEventLocation() {
        return eventLocation;
    }
    public String getDescription() {
        return description;
    }

    public String getOrganizerId() { return organizerId; }
    public String getEventPosterURL() { return eventPosterURL; }

    public int getEventCapacity() { return eventCapacity; }

    public int getWaitlistCount() { return waitlistCount; }

    // Setters

    public void setEventId(String eventId) { this.eventId = eventId; }

    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public void setEventName(String eventName) { this.eventName = eventName; }

    public void setEventStartTime(Date eventStartTime) { this.eventStartTime = eventStartTime; }
    public void setEventEndTime(Date eventEndTime) { this.eventEndTime = eventEndTime; }

    public void setRegistrationStart(Date registrationStart) { this.registrationStart = registrationStart; }
    public void setRegistrationEnd(Date registrationEnd) { this.registrationEnd = registrationEnd; }

    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

    public void setDescription(String description) { this.description = description; }

    public void setEventPosterURL(String eventPosterURL) { this.eventPosterURL = eventPosterURL; }

    public void setEventCapacity(int eventCapacity) { this.eventCapacity = eventCapacity; }

    public void setWaitlistCount(int waitlistCount) { this.waitlistCount = waitlistCount; }
}