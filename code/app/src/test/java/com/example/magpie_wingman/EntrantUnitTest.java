package com.example.magpie_wingman; // Adjust package path if necessary

import com.example.magpie_wingman.data.model.Entrant;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Entrant subclass model.
 */
public class EntrantUnitTest {

    @Test
    public void newEntrantHasEmptyLists() {
        // Arrange
        String userId = "ENT456";
        String name = "Test Entrant";
        Entrant entrant = new Entrant(userId, name, null, null, null, null, null);


        assertNotNull(entrant.getWaitlistedEvents());
        assertNotNull(entrant.getEnrolledEvents());
        assertTrue(entrant.getWaitlistedEvents().isEmpty());
        assertTrue(entrant.getEnrolledEvents().isEmpty());
    }

    @Test
    public void enrollmentStatusUpdateWorks() {

        Entrant entrant = new Entrant("E1", "Test", null, null, null, null, null);


        entrant.getWaitlistedEvents().add("EVENT1");
        entrant.getWaitlistedEvents().add("EVENT2");
        entrant.getEnrolledEvents().add("EVENT3");

        assertEquals(2, entrant.getWaitlistedEvents().size());
        assertEquals(1, entrant.getEnrolledEvents().size());
        assertTrue(entrant.getWaitlistedEvents().contains("EVENT1"));
    }
}