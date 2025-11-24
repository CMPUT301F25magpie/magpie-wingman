package com.example.magpie_wingman;

import org.junit.Test;
import static org.junit.Assert.assertThrows;

import com.example.magpie_wingman.data.NotificationFunction;

/**
 * unit tests for US 02.05.01, 02.07.01, 02.07.02, 02.07.03 NotificationFunction
 */
public class NotificationFunctionTest {

    @Test
    public void testNotifyEntrants_throwsExpectedError() {
        assertThrows(RuntimeException.class, () -> {
            NotificationFunction nf = new NotificationFunction();
            nf.notifyEntrants("event123", "waitlist", "Test message");
        });
    }
}