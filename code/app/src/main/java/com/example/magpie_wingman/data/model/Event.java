package com.example.magpie_wingman.data.model;

/**
 * Event an organizer creates/
 * Entrant membership is tracked on Entrant side as a list of event IDs
 */
public class Event {
    private String eventId;
    private String organizerId;
    private String eventName;
    private String eventDate;
    private String eventLocation;
    private String eventDescription;
    private String eventPosterURL;
    private int eventCapacity;

    public Event(String eventId, String organizerId, String eventName, String eventDate, String eventLocation, String eventDescription, String eventPosterURL, int eventCapacity) {
        this.eventId = eventId;
        this.organizerId = organizerId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.eventDescription = eventDescription;
        this.eventPosterURL = null;
        this.eventCapacity = eventCapacity;
    }

    // --- Getters ---
    public String getEventId() {
        return eventId;
    }
    public String getEventName() {
        return eventName;
    }
    public String getEventDate() {
        return eventDate;
    }
    public String getEventLocation() {
        return eventLocation;
    }
    public String getDescription() {
        return eventDescription;
    }
    public String getPosterImageUrl() {
        return eventPosterURL;
    }

    public String getOrganizerId() { return organizerId; }

    public String getEventDescription() { return eventDescription; }

    public String getEventPosterURL() { return eventPosterURL; }

    public int getEventCapacity() { return eventCapacity; }

    // Setters

    public void setEventId(String eventId) { this.eventId = eventId; }

    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }

    public void setEventName(String eventName) { this.eventName = eventName; }

    public void setEventDate(String eventDate) { this.eventDate = eventDate; }

    public void setEventLocation(String eventLocation) { this.eventLocation = eventLocation; }

    public void setEventDescription(String eventDescription) { this.eventDescription = eventDescription; }

    public void setEventPosterURL(String eventPosterURL) { this.eventPosterURL = eventPosterURL; }

    public void setEventCapacity(int eventCapacity) { this.eventCapacity = eventCapacity; }
}