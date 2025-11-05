package com.example.magpie_wingman.data;

import com.google.firebase.Timestamp;

public class Event {
    private String eventId;
    private String eventName;
    private String description;
    private String organizerId;
    private Object registrationStart; // keep it Object to match current Firestore
    private Object registrationEnd;

    // required empty constructor for Firestore / deserialization
    public Event() {}

    public Event(String eventId,
                 String eventName,
                 String description,
                 String organizerId,
                 Object registrationStart,
                 Object registrationEnd) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.organizerId = organizerId;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDescription() {
        return description;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public Object getRegistrationStart() {
        return registrationStart;
    }

    public Object getRegistrationEnd() {
        return registrationEnd;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void setRegistrationStart(Object registrationStart) {
        this.registrationStart = registrationStart;
    }

    public void setRegistrationEnd(Object registrationEnd) {
        this.registrationEnd = registrationEnd;
    }
}
