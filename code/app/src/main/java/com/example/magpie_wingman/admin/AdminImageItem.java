package com.example.magpie_wingman.admin;

/**
 * Model representing a poster inside the admin image grid.
 */
public class AdminImageItem {

    private final String eventId;
    private final String eventName;
    private final String posterUrl;

    public AdminImageItem(String eventId, String eventName, String posterUrl) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.posterUrl = posterUrl;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getPosterUrl() {
        return posterUrl;
    }
}

