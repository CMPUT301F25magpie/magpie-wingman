package com.example.magpie_wingman.data.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

/**
 * Represents a notification sent to a user.
 */
public class Notification {
    private final String message;
    private final long timestamp;
    private final String eventId;
    private final boolean read;

    /**
     * Constructs a notification with all fields.
     *
     * @param message   raw notification message string
     * @param timestamp timestamp in epoch milliseconds
     * @param eventId   related event ID, or {@code null} if not event-specific
     * @param read      whether this notification has already been read
     */
    public Notification(String message, long timestamp, String eventId, boolean read) {
        this.message = message;
        this.timestamp = timestamp;
        this.eventId = eventId;
        this.read = read;
    }

    // Getters
    public String getMessageRaw() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getEventId() { return eventId; }
    public boolean isRead() { return read; }

    /**
     * Derives a notification title from the raw message.
     *
     * <p>Convention:
     * <ul>
     *     <li>If the message contains a colon ({@code Title: body}), the substring
     *         before the first colon is used as the title.</li>
     *     <li>If no colon is found or the message is {@code null}, a generic
     *         {@code "Notification"} title is returned.</li>
     * </ul>
     *
     * @return title for UI headings
     */
    public String getTitle() {
        if (message == null) return "Notification";
        int i = message.indexOf(':');
        return (i > 0) ? message.substring(0, i).trim() : "Notification";
    }

    /**
     * Derives the body portion of the notification from the raw message.
     *
     * <p>Convention:
     * <ul>
     *     <li>If the message contains a colon ({@code Title: body}), the substring
     *         after the first colon is used as the body.</li>
     *     <li>If no colon is found, the entire message is treated as the body.</li>
     *     <li>If the message is {@code null}, an empty string is returned.</li>
     * </ul>
     *
     * @return body text of the notification
     */
    public String getBody() {
        if (message == null) return "";
        int i = message.indexOf(':');
        return (i >= 0) ? message.substring(i + 1).trim() : message;
    }

    /**
     * Creates a {@link Notification} instance from a Firestore document snapshot.
     *
     * <p>Expected document fields:
     * <ul>
     *     <li>{@code message}: {@link String}</li>
     *     <li>{@code eventId}: {@link String} (optional)</li>
     *     <li>{@code read}: {@link Boolean} (optional, defaults to {@code false})</li>
     *     <li>{@code timestamp}: {@link Timestamp} or {@link Long}</li>
     * </ul>
     *
     * @param d Firestore document snapshot representing a notification
     * @return a populated {@link Notification} model
     */
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
