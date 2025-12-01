package com.example.magpie_wingman.data.model;

/**
 * Represents an invitation for an entrant to participate in an event.
 */
public class Invitation {
    private final String eventId;
    private final String eventName;
    private final String datetime;
    private final String location;
    private final String description;
    private final long invitedAt;

    /**
     * Constructs an Invitation with all displayable fields.
     *
     * @param eventId      unique ID of the related event
     * @param eventName    display name of the event
     * @param datetime     formatted registration or event time range
     * @param location     location where the event takes place
     * @param description  short description or summary of the event
     * @param invitedAt    epoch milliseconds when this invitation was created
     */
    public Invitation(String eventId, String eventName, String datetime,
                      String location, String description, long invitedAt) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.datetime = datetime;
        this.location = location;
        this.description = description;
        this.invitedAt = invitedAt;
    }

    // Getters
    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getDatetime() { return datetime; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public long getInvitedAt() { return invitedAt; }
}
