package com.example.magpie_wingman.data;

import com.example.magpie_wingman.data.DbManager;
import com.google.android.gms.tasks.Task;

public class LotteryFunction {

    /**
     * Triggers random sampling of attendees for an event
     * @param eventId the event document ID
     * @param sampleCount number of attendees to move from waitlist to registrable
     * @return Task<Void> to monitor completion
     */
    public static Task<Void> sampleEntrantsForEvent(String eventId, int sampleCount) {
        DbManager dbManager = DbManager.getInstance();
        return dbManager.addUsersToRegistrable(eventId, sampleCount);
    }
}