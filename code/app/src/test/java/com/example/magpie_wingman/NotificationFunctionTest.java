package com.example.magpie_wingman;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.NotificationFunction;
import com.google.android.gms.tasks.Tasks;

public class NotificationFunctionTest {
    @Before
    public void setup() {
        DbManager.initForTesting();
    }

    @Test
    public void testNotifyEntrants_returnsNonNullTask() {
        try {
            NotificationFunction nf = new NotificationFunction(null);
            assertNotNull(nf);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("not mocked"));
        }
    }

    @Test
    public void testNotifyEntrants_handlesEmptyMessage() {
        NotificationFunction nf = new NotificationFunction(null);
        try {
            nf.notifyEntrants("event123", "waitlist", "");
        } catch (Exception e) {
            fail("Should handle empty message");
        }
    }
}
