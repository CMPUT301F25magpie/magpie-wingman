package com.example.magpie_wingman.data.model;

public class Event {
    private String eventId;
    private String eventName;
    private String eventDate;
    private String eventLocation;
    private String description;
    private String posterImageUrl;

    public Event(String eventId, String eventName, String eventDate, String eventLocation, String description) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
        this.description = description;
        this.posterImageUrl = null;
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
        return description;
    }
    public String getPosterImageUrl() {
        return posterImageUrl;
    }
}