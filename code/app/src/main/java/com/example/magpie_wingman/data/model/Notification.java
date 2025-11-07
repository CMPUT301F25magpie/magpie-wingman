package com.example.magpie_wingman.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

public class Notification {
    private final String message;
    private final long timestamp;
    private final String eventId;
    private final boolean read;

    public Notification(String message, long timestamp, String eventId, boolean read) {
        this.message = message;
        this.timestamp = timestamp;
        this.eventId = eventId;
        this.read = read;
    }

    public String getMessageRaw() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getEventId() { return eventId; }
    public boolean isRead() { return read; }

    // Split whole message at first colon to get title + notification message
    public String getTitle() {
        if (message == null) return "Notification";
        int i = message.indexOf(':');
        return (i > 0) ? message.substring(0, i).trim() : "Notification";
    }

    public String getBody() {
        if (message == null) return "";
        int i = message.indexOf(':');
        return (i >= 0) ? message.substring(i + 1).trim() : message;
    }

    public static Notification from(DocumentSnapshot d) {
        String msg = d.getString("message");
        if (msg == null) msg = "";

        String eventId = d.getString("eventId");

        Boolean readObj = d.getBoolean("read");
        boolean read = readObj != null && readObj;

        long ts = 0L;
        Object raw = d.get("timestamp");
        if (raw instanceof Timestamp) {
            ts = ((Timestamp) raw).toDate().getTime();
        } else if (raw instanceof Long) {
            ts = (Long) raw;
        }

        return new Notification(msg, ts, eventId, read);
    }
}
