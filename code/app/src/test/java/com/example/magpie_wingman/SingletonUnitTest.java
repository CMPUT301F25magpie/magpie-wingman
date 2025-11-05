package com.example.magpie_wingman;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.example.magpie_wingman.data.DbManager;

import org.junit.Test;

public class SingletonUnitTest {
    @Test
    public void singleton_returnsSameInstance() {
        DbManager m1 = DbManager.getInstance();
        DbManager m2 = DbManager.getInstance();

        assertNotNull(m1);
        assertNotNull(m2);
        assertSame(m1, m2);   // same object in memory, should only ever have one instance of dbManager created
    }

}
