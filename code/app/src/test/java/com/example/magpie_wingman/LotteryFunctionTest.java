package com.example.magpie_wingman;

import static org.junit.Assert.assertThrows;

import com.example.magpie_wingman.data.LotteryFunction;
import com.google.android.gms.tasks.Task;

import org.junit.Test;

/**
 * unit test for US 02.05.02 LotteryFunction
 */
public class LotteryFunctionTest {

    @Test
    public void testSampleEntrants_requiresDbManagerInitialization() {
        assertThrows(IllegalStateException.class, () -> {
            LotteryFunction.sampleEntrantsForEvent("event123", 5);
        });
    }
}
