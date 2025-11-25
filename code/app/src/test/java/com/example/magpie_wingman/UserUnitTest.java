package com.example.magpie_wingman;

import com.example.magpie_wingman.data.model.User;
import com.example.magpie_wingman.data.model.UserRole;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the core User model class.
 */
public class UserUnitTest {

    @Test
    public void organizerRoleAssignmentIsCorrect() {
        // Arrange (Test for Organizer)
        String userId = "ORG123";
        String name = "Organizer Max";
        boolean isOrganizer = true;

        User organizer = new User(userId, name, isOrganizer, null, "max@org.com", "555-1234", "device1", null);

        // Assert
        assertTrue(organizer.isOrganizer());
        assertEquals(UserRole.ORGANIZER, organizer.getRole());
        assertEquals(name, organizer.getName());
    }

    @Test
    public void entrantRoleAssignmentIsCorrect() {
        // Arrange (Test for Entrant)
        String userId = "ENT456";
        String name = "Entrant Sam";
        boolean isOrganizer = false;
        User entrant = new User(userId, name, isOrganizer, null, null, null, null, null);


        assertFalse(entrant.isOrganizer());
        assertEquals(UserRole.ENTRANT, entrant.getRole());
        assertEquals(userId, entrant.getUserId());
    }
}