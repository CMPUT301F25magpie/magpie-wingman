package com.example.magpie_wingman.data;

import com.google.android.gms.tasks.Task;

public class LotteryFunction {

    /**
     * calls existing DbManager method that randomly samples entrants and moves them from waitlist → registrable
     * @param eventId ID of the event
     * @param sampleCount number of entrants to select
     * @return Task<Void> to monitor completion
     */
    public static Task<Void> sampleEntrantsForEvent(String eventId, int sampleCount) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("Event ID cannot be null or empty");
        }
        if (sampleCount < 0) {
            throw new IllegalArgumentException("Sample count must be non-negative");
        }
        DbManager dbManager = DbManager.getInstance();
        return dbManager.drawInvitees(eventId, sampleCount);
    }
}