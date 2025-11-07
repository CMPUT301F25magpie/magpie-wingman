package com.example.magpie_wingman.data;

import com.example.magpie_wingman.data.DbManager;
import com.google.android.gms.tasks.Task;

public class LotteryFunction {
    /**
     * calls existing DbManager method that randomly samples entrants and moves them from waitlist → registrable
     * @param eventId ID of the event
     * @param sampleCount number of entrants to select
     * @return Task<Void> to monitor completion
     */
    public static Task<Void> sampleEntrantsForEvent(String eventId, int sampleCount) {
        DbManager dbManager = DbManager.getInstance();
        return dbManager.addUsersToRegistrable(eventId, sampleCount);
    }
}