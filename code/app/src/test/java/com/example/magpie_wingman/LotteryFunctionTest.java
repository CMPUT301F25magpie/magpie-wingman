package com.example.magpie_wingman;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.LotteryFunction;

public class LotteryFunctionTest {
    private LotteryFunction function;

    @Before
    public void setup() {
        DbManager.initForTesting();
    }

    @Test
    public void testSampleEntrants_validCount() {
        assertNotNull(function.sampleEntrantsForEvent("event123", 5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSampleEntrants_negativeCountThrows() {
        function.sampleEntrantsForEvent("event123", -1);
    }
}

