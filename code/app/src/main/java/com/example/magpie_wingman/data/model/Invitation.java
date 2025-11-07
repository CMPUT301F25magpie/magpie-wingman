package com.example.magpie_wingman.data.model;

public class Invitation {
    private final String eventId;
    private final String eventName;
    private final String datetime;
    private final String location;
    private final String description;
    private final long invitedAt;

    public Invitation(String eventId, String eventName, String datetime,
                      String location, String description, long invitedAt) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.datetime = datetime;
        this.location = location;
        this.description = description;
        this.invitedAt = invitedAt;
    }

    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getDatetime() { return datetime; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public long getInvitedAt() { return invitedAt; }
}
