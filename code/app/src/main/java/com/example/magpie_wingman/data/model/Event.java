package com.example.magpie_wingman.data.model;

import java.util.Date;

/**
 * Represents an Event created by an Organizer.
 * Stores all event details including registration dates, capacity, and location.
 * Entrant membership is tracked via subcollections in Firestore, not in this class directly.
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

    private int waitingListLimit;
    private String qrCodeHash;


    public Event() {}

    /**
     * Constructs a new Event with specific details.
     * @param eventId Unique identifier for the event.
     * @param organizerId The device ID/User ID of the creator.
     * @param eventName Title of the event.
     * @param eventStartTime When the event begins.
     * @param eventEndTime When the event ends.
     * @param eventLocation Physical address or location string.
     * @param description Detailed info about the event.
     * @param eventPosterURL URL to the poster image in storage.
     * @param eventCapacity Maximum number of attendees (0 for unlimited).
     */
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
        this.description = description;
        this.eventPosterURL = eventPosterURL;
        this.eventCapacity = eventCapacity;
        this.waitlistCount = 0;

        // Default values for new fields
        this.waitingListLimit = 0;
        this.qrCodeHash = eventId;
    }


    public String getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public Date getEventStartTime() { return eventStartTime; }
    public Date getEventEndTime() { return eventEndTime; }
    public Date getRegistrationStart() { return registrationStart; }
    public Date getRegistrationEnd() { return registrationEnd; }
    public String getEventLocation() { return eventLocation; }
    public String getDescription() { return description; }
    public String getOrganizerId() { return organizerId; }
    public String getEventPosterURL() { return eventPosterURL; }
    public int getEventCapacity() { return eventCapacity; }
    public int getWaitlistCount() { return waitlistCount; }


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

    //for part4
    public int getWaitingListLimit() { return waitingListLimit; }
    public void setWaitingListLimit(int waitingListLimit) { this.waitingListLimit = waitingListLimit; }

    public String getQrCodeHash() { return qrCodeHash; }
    public void setQrCodeHash(String qrCodeHash) { this.qrCodeHash = qrCodeHash; }
}