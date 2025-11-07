package com.example.magpie_wingman;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.magpie_wingman.data.DbManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class DbManagerInstrumentedTest {

    @Test
    public void dbManager_initializes() {
        Context ctx = ApplicationProvider.getApplicationContext();
        assertNotNull(ctx);

        DbManager dbm = DbManager.getInstance();
        assertNotNull(dbm);

        FirebaseFirestore db = dbm.getDb();
        assertNotNull(db);
    }

    @Test
    public void createUser_writesAllFields() throws Exception {
        // arrange
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String name = "Test User";

        String email = "test_user_" + System.currentTimeMillis() + "@example.com";
        String phone = "555-0000";


        Task<Void> writeTask = dbm.createUser(name, email, phone);

        Tasks.await(writeTask, 10, TimeUnit.SECONDS);


        Task<QuerySnapshot> queryTask = db.collection("users")
                .whereEqualTo("email", email)
                .get();

        QuerySnapshot qs = Tasks.await(queryTask, 10, TimeUnit.SECONDS);
        assertNotNull(qs);
        List<DocumentSnapshot> docs = qs.getDocuments();
        assertTrue("Expected at least 1 user doc with this email", docs.size() >= 1);


        DocumentSnapshot doc = docs.get(0);
        assertNotNull(doc);
        assertTrue(doc.exists());


        assertEquals(name, doc.getString("name"));
        assertEquals(email, doc.getString("email"));
        assertEquals(phone, doc.getString("phone"));

        Boolean isOrganizer = doc.getBoolean("isOrganizer");
        assertNotNull(isOrganizer);
        assertTrue(isOrganizer);


        String userId = doc.getString("userId");
        assertNotNull("userId should be written in the doc", userId);

        assertTrue("userId should contain '#'", userId.contains("#"));
    }
    @Test
    public void deleteUser_removesDocument() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String name = "Delete Me";
        String email = "delete_me_" + System.currentTimeMillis() + "@example.com";
        String phone = "555-9999";
        Tasks.await(dbm.createUser(name, email, phone), 10, TimeUnit.SECONDS);
        QuerySnapshot qs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        assertNotNull(qs);
        assertTrue(qs.getDocuments().size() >= 1);
        DocumentSnapshot doc = qs.getDocuments().get(0);
        String userId = doc.getString("userId");
        assertNotNull(userId);
        Tasks.await(dbm.deleteUser(userId), 10, TimeUnit.SECONDS);
        DocumentSnapshot afterDelete = Tasks.await(
                db.collection("users").document(userId).get(),
                10, TimeUnit.SECONDS
        );

        assertNotNull(afterDelete);
        assertTrue("user doc should be deleted", !afterDelete.exists());
    }


    @Test
    public void createEvent_writesAllFields() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String eventName = "Magpie Meetup " + System.currentTimeMillis();
        String description = "Test description for magpie meetup";
        String organizerId = "stuart#1234";  // can be any existing/placeholder user id
        Date regStart = "2025-11-01";
        String regEnd = "2025-11-05";

        Tasks.await(
                dbm.createEvent(
                        eventName,
                        description,
                        organizerId,
                        regStart,
                        regEnd
                ),
                10, TimeUnit.SECONDS
        );

        QuerySnapshot qs = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get(),
                10, TimeUnit.SECONDS
        );

        assertNotNull(qs);
        assertTrue("Expected at least 1 event with this name", qs.getDocuments().size() >= 1);

        DocumentSnapshot doc = qs.getDocuments().get(0);
        assertNotNull(doc);
        assertTrue(doc.exists());

        assertEquals(eventName, doc.getString("eventName"));
        assertEquals(description, doc.getString("description"));
        assertEquals(organizerId, doc.getString("organizerId"));
        assertEquals(regStart, doc.get("registrationStart"));
        assertEquals(regEnd, doc.get("registrationEnd"));

        String eventId = doc.getString("eventId");
        assertNotNull("eventId should be stored", eventId);
        assertTrue("eventId should contain '#'", eventId.contains("#"));
    }
    @Test
    public void addUserToWaitlist_createsSubdoc() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        //create a user
        String userEmail = "waitlist_user_" + System.currentTimeMillis() + "@example.com";
        String userName = "Waitlist User";
        String userPhone = "555-2222";

        Tasks.await(
                dbm.createUser(userName, userEmail, userPhone),
                10, TimeUnit.SECONDS
        );

        // find the userId we just created
        QuerySnapshot userQs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", userEmail)
                        .get(),
                10, TimeUnit.SECONDS
        );

        assertNotNull(userQs);
        assertTrue(userQs.getDocuments().size() >= 1);
        DocumentSnapshot userDoc = userQs.getDocuments().get(0);
        String userId = userDoc.getString("userId");
        assertNotNull(userId);

        // 2) create an event
        String eventName = "Waitlist Test Event " + System.currentTimeMillis();
        String description = "Event to test waitlist";
        String organizerId = "organizer#0001";
        String regStart = "2025-11-01";
        String regEnd = "2025-11-02";

        Tasks.await(
                dbm.createEvent(eventName, description, organizerId, regStart, regEnd),
                10, TimeUnit.SECONDS
        );

        // find the eventId we just created
        QuerySnapshot eventQs = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get(),
                10, TimeUnit.SECONDS
        );

        assertNotNull(eventQs);
        assertTrue(eventQs.getDocuments().size() >= 1);
        DocumentSnapshot eventDoc = eventQs.getDocuments().get(0);
        String eventId = eventDoc.getString("eventId");
        assertNotNull(eventId);

        // 3) add user to waitlist
        Tasks.await(
                dbm.addUserToWaitlist(eventId, userId),
                10, TimeUnit.SECONDS
        );

        // 4) assert the subdoc exists
        DocumentSnapshot wlDoc = Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("waitlist")
                        .document(userId)
                        .get(),
                10, TimeUnit.SECONDS
        );

        assertNotNull(wlDoc);
        assertTrue("waitlist doc should exist", wlDoc.exists());
        assertEquals(userId, wlDoc.getString("userId"));
    }

    @Test
    public void addUsersToRegistrable_movesRequestedNumber() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // 1) create event
        String eventName = "Sampling Event " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(
                        eventName,
                        "sampling test",
                        "organizer#0001",
                        "2025-11-01",
                        "2025-11-02"
                ),
                10, TimeUnit.SECONDS
        );

        // get eventId
        QuerySnapshot eventQs = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot eventDoc = eventQs.getDocuments().get(0);
        String eventId = eventDoc.getString("eventId");
        assertNotNull(eventId);

        // 2) make 3 users and add them to waitlist
        List<String> userIds = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            String email = "wluser_" + i + "_" + System.currentTimeMillis() + "@example.com";
            Tasks.await(dbm.createUser("WL User " + i, email, "555-000" + i), 10, TimeUnit.SECONDS);

            QuerySnapshot userQs = Tasks.await(
                    db.collection("users")
                            .whereEqualTo("email", email)
                            .get(),
                    10, TimeUnit.SECONDS
            );
            DocumentSnapshot userDoc = userQs.getDocuments().get(0);
            String userId = userDoc.getString("userId");
            assertNotNull(userId);
            userIds.add(userId);

            // add to waitlist
            Tasks.await(dbm.addUserToWaitlist(eventId, userId), 10, TimeUnit.SECONDS);
        }

        // 3) promote 2 users from waitlist → registrable
        Tasks.await(dbm.addUsersToRegistrable(eventId, 2), 10, TimeUnit.SECONDS);

        // 4) assert registrable has 2
        QuerySnapshot registrableSnap = Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("registrable")
                        .get(),
                10, TimeUnit.SECONDS
        );
        assertNotNull(registrableSnap);
        assertEquals(2, registrableSnap.size());

        // 5) assert waitlist has 1 left
        QuerySnapshot waitlistSnap = Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("waitlist")
                        .get(),
                10, TimeUnit.SECONDS
        );
        assertNotNull(waitlistSnap);
        assertEquals(1, waitlistSnap.size());
    }

    @Test
    public void addUserToRegistered_movesFromRegistrable() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // 1) create event
        String eventName = "Register Flow " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(
                        eventName,
                        "flow test",
                        "organizer#0001",
                        "2025-11-01",
                        "2025-11-02"
                ),
                10, TimeUnit.SECONDS
        );

        QuerySnapshot eventQs = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot eventDoc = eventQs.getDocuments().get(0);
        String eventId = eventDoc.getString("eventId");
        assertNotNull(eventId);

        // 2) create 1 user
        String email = "reg_user_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Reg User", email, "555-8888"), 10, TimeUnit.SECONDS);

        QuerySnapshot userQs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = userQs.getDocuments().get(0);
        String userId = userDoc.getString("userId");
        assertNotNull(userId);

        // 3) manually add to registrable (this simulates the user being invited)
        Map<String, Object> regData = new HashMap<>();
        regData.put("userId", userId);
        regData.put("invitedAt", System.currentTimeMillis());

        Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("registrable")
                        .document(userId)
                        .set(regData),
                10, TimeUnit.SECONDS
        );

        // 4) now move to registered using our helper
        Tasks.await(dbm.addUserToRegistered(eventId, userId), 10, TimeUnit.SECONDS);

        // 5) assert user is in registered
        DocumentSnapshot registeredDoc = Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("registered")
                        .document(userId)
                        .get(),
                10, TimeUnit.SECONDS
        );
        assertNotNull(registeredDoc);
        assertTrue(registeredDoc.exists());
        assertEquals(userId, registeredDoc.getString("userId"));

        // 6) assert user is NOT in registrable anymore
        DocumentSnapshot registrableDoc = Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("registrable")
                        .document(userId)
                        .get(),
                10, TimeUnit.SECONDS
        );
        assertNotNull(registrableDoc);
        assertFalse("user should have been removed from registrable", registrableDoc.exists());
    }
    @Test
    public void deleteEvent_removesEventAndSubcollections() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // 1) create event
        String eventName = "Delete Me " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(
                        eventName,
                        "to be deleted",
                        "organizer#0001",
                        "2025-11-01",
                        "2025-11-02"
                ),
                10, TimeUnit.SECONDS
        );

        // fetch eventId
        QuerySnapshot eventQs = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot eventDoc = eventQs.getDocuments().get(0);
        String eventId = eventDoc.getString("eventId");
        assertNotNull(eventId);

        // 2) create a user and put them in all 3 subcollections
        String email = "deleteme_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Delete User", email, "555-9999"), 10, TimeUnit.SECONDS);

        QuerySnapshot userQs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = userQs.getDocuments().get(0);
        String userId = userDoc.getString("userId");
        assertNotNull(userId);

        // add to waitlist
        Tasks.await(dbm.addUserToWaitlist(eventId, userId), 10, TimeUnit.SECONDS);

        // add to registrable (direct write)
        Map<String, Object> regData = new HashMap<>();
        regData.put("userId", userId);
        Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("registrable")
                        .document(userId)
                        .set(regData),
                10, TimeUnit.SECONDS
        );

        // add to registered (direct write)
        Map<String, Object> regdData = new HashMap<>();
        regdData.put("userId", userId);
        Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("registered")
                        .document(userId)
                        .set(regdData),
                10, TimeUnit.SECONDS
        );

        // 3) delete the event
        Tasks.await(dbm.deleteEvent(eventId), 10, TimeUnit.SECONDS);

        // 4) assert event gone
        DocumentSnapshot afterDelete = Tasks.await(
                db.collection("events").document(eventId).get(),
                10, TimeUnit.SECONDS
        );
        assertFalse("event doc should be deleted", afterDelete.exists());

        // 5) assert subcollections empty (reads should return 0 docs)
        QuerySnapshot wlAfter = Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("waitlist")
                        .get(),
                10, TimeUnit.SECONDS
        );
        assertEquals(0, wlAfter.size());

        QuerySnapshot regableAfter = Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("registrable")
                        .get(),
                10, TimeUnit.SECONDS
        );
        assertEquals(0, regableAfter.size());

        QuerySnapshot regdAfter = Tasks.await(
                db.collection("events")
                        .document(eventId)
                        .collection("registered")
                        .get(),
                10, TimeUnit.SECONDS
        );
        assertEquals(0, regdAfter.size());
    }
    @Test
    public void cancelWaitlist_removesUserFromWaitlist() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // create event
        String eventName = "Cancel WL " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(eventName, "desc", "organizer#0001", "2025-11-01", "2025-11-02"),
                10, TimeUnit.SECONDS
        );
        QuerySnapshot evQs = Tasks.await(
                db.collection("events").whereEqualTo("eventName", eventName).get(),
                10, TimeUnit.SECONDS
        );
        String eventId = evQs.getDocuments().get(0).getString("eventId");

        // create user
        String email = "wl_cancel_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("WL Cancel", email, "555-0000"), 10, TimeUnit.SECONDS);
        QuerySnapshot uQs = Tasks.await(
                db.collection("users").whereEqualTo("email", email).get(),
                10, TimeUnit.SECONDS
        );
        String userId = uQs.getDocuments().get(0).getString("userId");

        // add to waitlist
        Tasks.await(dbm.addUserToWaitlist(eventId, userId), 10, TimeUnit.SECONDS);

        // cancel
        Tasks.await(dbm.cancelWaitlist(eventId, userId), 10, TimeUnit.SECONDS);

        // assert gone
        DocumentSnapshot after = Tasks.await(
                db.collection("events").document(eventId)
                        .collection("waitlist").document(userId).get(),
                10, TimeUnit.SECONDS
        );
        assertFalse(after.exists());
    }

    @Test
    public void cancelRegistrable_removesUserFromRegistrable() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String eventName = "Cancel Regable " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(eventName, "desc", "organizer#0001", "2025-11-01", "2025-11-02"),
                10, TimeUnit.SECONDS
        );
        QuerySnapshot evQs = Tasks.await(
                db.collection("events").whereEqualTo("eventName", eventName).get(),
                10, TimeUnit.SECONDS
        );
        String eventId = evQs.getDocuments().get(0).getString("eventId");

        String email = "regable_cancel_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Regable Cancel", email, "555-1111"), 10, TimeUnit.SECONDS);
        QuerySnapshot uQs = Tasks.await(
                db.collection("users").whereEqualTo("email", email).get(),
                10, TimeUnit.SECONDS
        );
        String userId = uQs.getDocuments().get(0).getString("userId");

        // simulate "registrable"
        Map<String, Object> regData = new HashMap<>();
        regData.put("userId", userId);
        Tasks.await(
                db.collection("events").document(eventId)
                        .collection("registrable").document(userId)
                        .set(regData),
                10, TimeUnit.SECONDS
        );

        // cancel
        Tasks.await(dbm.cancelRegistrable(eventId, userId), 10, TimeUnit.SECONDS);

        // assert gone
        DocumentSnapshot after = Tasks.await(
                db.collection("events").document(eventId)
                        .collection("registrable").document(userId).get(),
                10, TimeUnit.SECONDS
        );
        assertFalse(after.exists());
    }

    @Test
    public void cancelRegistered_removesUserFromRegistered() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String eventName = "Cancel Registered " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(eventName, "desc", "organizer#0001", "2025-11-01", "2025-11-02"),
                10, TimeUnit.SECONDS
        );
        QuerySnapshot evQs = Tasks.await(
                db.collection("events").whereEqualTo("eventName", eventName).get(),
                10, TimeUnit.SECONDS
        );
        String eventId = evQs.getDocuments().get(0).getString("eventId");

        String email = "regd_cancel_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Regd Cancel", email, "555-2222"), 10, TimeUnit.SECONDS);
        QuerySnapshot uQs = Tasks.await(
                db.collection("users").whereEqualTo("email", email).get(),
                10, TimeUnit.SECONDS
        );
        String userId = uQs.getDocuments().get(0).getString("userId");

        // simulate "registered"
        Map<String, Object> regdData = new HashMap<>();
        regdData.put("userId", userId);
        Tasks.await(
                db.collection("events").document(eventId)
                        .collection("registered").document(userId)
                        .set(regdData),
                10, TimeUnit.SECONDS
        );

        // cancel
        Tasks.await(dbm.cancelRegistered(eventId, userId), 10, TimeUnit.SECONDS);

        // assert gone
        DocumentSnapshot after = Tasks.await(
                db.collection("events").document(eventId)
                        .collection("registered").document(userId).get(),
                10, TimeUnit.SECONDS
        );
        assertFalse(after.exists());
    }
    @Test
    public void updateName_updatesUserNameField() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // 1) create user
        String email = "upname_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Original Name", email, "555-0000"), 10, TimeUnit.SECONDS);

        // 2) fetch userId
        QuerySnapshot qs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = qs.getDocuments().get(0);
        String userId = userDoc.getString("userId");

        // 3) update
        Tasks.await(dbm.updateName(userId, "New Name"), 10, TimeUnit.SECONDS);

        // 4) read back
        DocumentSnapshot after = Tasks.await(
                db.collection("users").document(userId).get(),
                10, TimeUnit.SECONDS
        );
        assertEquals("New Name", after.getString("name"));
    }

    @Test
    public void updateEmail_updatesUserEmailField() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String email = "upemail_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Email User", email, "555-1111"), 10, TimeUnit.SECONDS);

        QuerySnapshot qs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = qs.getDocuments().get(0);
        String userId = userDoc.getString("userId");

        String newEmail = "updated_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.updateEmail(userId, newEmail), 10, TimeUnit.SECONDS);

        DocumentSnapshot after = Tasks.await(
                db.collection("users").document(userId).get(),
                10, TimeUnit.SECONDS
        );
        assertEquals(newEmail, after.getString("email"));
    }

    @Test
    public void updatePhoneNumber_updatesUserPhoneField() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String email = "upphone_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Phone User", email, "555-2222"), 10, TimeUnit.SECONDS);

        QuerySnapshot qs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = qs.getDocuments().get(0);
        String userId = userDoc.getString("userId");

        String newPhone = "780-555-4321";
        Tasks.await(dbm.updatePhoneNumber(userId, newPhone), 10, TimeUnit.SECONDS);

        DocumentSnapshot after = Tasks.await(
                db.collection("users").document(userId).get(),
                10, TimeUnit.SECONDS
        );
        assertEquals(newPhone, after.getString("phone"));
    }
    @Test
    public void changeOrgPerms_updatesIsOrganizerField() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // create user
        String email = "orgperm_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Org Perm", email, "555-0000"), 10, TimeUnit.SECONDS);

        // get userId
        QuerySnapshot qs = Tasks.await(
                db.collection("users").whereEqualTo("email", email).get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = qs.getDocuments().get(0);
        String userId = userDoc.getString("userId");

        // change to false
        Tasks.await(dbm.changeOrgPerms(userId, false), 10, TimeUnit.SECONDS);

        // assert field updated
        DocumentSnapshot after = Tasks.await(
                db.collection("users").document(userId).get(),
                10, TimeUnit.SECONDS
        );
        assertEquals(false, after.getBoolean("isOrganizer"));
    }

    @Test
    public void getUserName_returnsCorrectName() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String email = "getter_name_" + System.currentTimeMillis() + "@example.com";

        // create user
        Tasks.await(
                dbm.createUser("Test Name", email, "555-0000"),
                10, TimeUnit.SECONDS
        );

        // find the userId we just created
        QuerySnapshot qs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = qs.getDocuments().get(0);
        String userId = userDoc.getString("userId");

        // call the getter
        String name = Tasks.await(
                dbm.getUserName(userId),
                10, TimeUnit.SECONDS
        );

        assertEquals("Test Name", name);
    }
    @Test
    public void getUserEmail_returnsCorrectEmail() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String email = "getter_email_" + System.currentTimeMillis() + "@example.com";

        Tasks.await(
                dbm.createUser("Email User", email, "555-1111"),
                10, TimeUnit.SECONDS
        );

        QuerySnapshot qs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = qs.getDocuments().get(0);
        String userId = userDoc.getString("userId");

        String fetchedEmail = Tasks.await(
                dbm.getUserEmail(userId),
                10, TimeUnit.SECONDS
        );

        assertEquals(email, fetchedEmail);
    }

    @Test
    public void getUserPhone_returnsCorrectPhone() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String email = "getter_phone_" + System.currentTimeMillis() + "@example.com";
        String phone = "780-555-1234";

        Tasks.await(
                dbm.createUser("Phone User", email, phone),
                10, TimeUnit.SECONDS
        );

        QuerySnapshot qs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = qs.getDocuments().get(0);
        String userId = userDoc.getString("userId");

        String fetchedPhone = Tasks.await(
                dbm.getUserPhone(userId),
                10, TimeUnit.SECONDS
        );

        assertEquals(phone, fetchedPhone);
    }

    @Test
    public void getIsOrganizer_updatesAfterChangeOrgPerms() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        String email = "getter_org_change_" + System.currentTimeMillis() + "@example.com";

        // create user
        Tasks.await(
                dbm.createUser("Org Change User", email, "555-4444"),
                10, TimeUnit.SECONDS
        );

        // retrieve the userId of the created user
        QuerySnapshot qs = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", email)
                        .get(),
                10, TimeUnit.SECONDS
        );
        DocumentSnapshot userDoc = qs.getDocuments().get(0);
        String userId = userDoc.getString("userId");

        // check initial isOrganizer value (should be true)
        Boolean initial = Tasks.await(
                dbm.getIsOrganizer(userId),
                10, TimeUnit.SECONDS
        );
        assertNotNull("isOrganizer should not be null", initial);
        assertTrue("isOrganizer should be true by default", initial);

        // change permission to false
        Tasks.await(
                dbm.changeOrgPerms(userId, false),
                10, TimeUnit.SECONDS
        );

        // verify that it changed
        Boolean afterChange = Tasks.await(
                dbm.getIsOrganizer(userId),
                10, TimeUnit.SECONDS
        );

        assertNotNull("isOrganizer after change should not be null", afterChange);
        assertFalse("isOrganizer should now be false after calling changeOrgPerms", afterChange);
    }
    @Test
    public void getEventDescription_returnsCorrectDescription() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // make an organizer first
        String orgEmail = "event_desc_org_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Event Org", orgEmail, "555-0000"));
        // get organizerId
        DocumentSnapshot orgDoc = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", orgEmail)
                        .get()
        ).getDocuments().get(0);
        String organizerId = orgDoc.getString("userId");

        String description = "This is a test event description.";
        String eventName = "Test Event " + System.currentTimeMillis();

        // create event
        Tasks.await(
                dbm.createEvent(
                        eventName,
                        description,
                        organizerId,
                        null,
                        null
                )
        );

        // find the event we just created by name
        DocumentSnapshot eventDoc = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get()
        ).getDocuments().get(0);
        String eventId = eventDoc.getString("eventId");

        // now call our getter
        String fetchedDesc = Tasks.await(
                dbm.getEventDescription(eventId)
        );

        assertEquals(description, fetchedDesc);
    }

    @Test
    public void getEventOrganizer_returnsOrganizerId() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // create organizer
        String orgEmail = "event_org_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Organizer Person", orgEmail, "555-1111"));
        DocumentSnapshot orgDoc = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", orgEmail)
                        .get()
        ).getDocuments().get(0);
        String organizerId = orgDoc.getString("userId");

        // create event
        String eventName = "Org Event " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(
                        eventName,
                        "org test",
                        organizerId,
                        null,
                        null
                )
        );

        // fetch back the event to get its id
        DocumentSnapshot eventDoc = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get()
        ).getDocuments().get(0);
        String eventId = eventDoc.getString("eventId");

        // call getter
        String fetchedOrganizerId = Tasks.await(
                dbm.getEventOrganizer(eventId)
        );

        assertEquals(organizerId, fetchedOrganizerId);
    }

    @Test
    public void getEventRegistrationDates_returnWhatWeSaved() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // organizer
        String orgEmail = "event_dates_org_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Dates Org", orgEmail, "555-2222"));
        DocumentSnapshot orgDoc = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", orgEmail)
                        .get()
        ).getDocuments().get(0);
        String organizerId = orgDoc.getString("userId");

        Date start = Timestamp.now();
        // arbitrary end a minute later
        Date end = new Timestamp(start.getSeconds() + 60, 0);

        String eventName = "Dates Event " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(
                        eventName,
                        "event with dates",
                        organizerId,
                        start,
                        end
                )
        );

        DocumentSnapshot eventDoc = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get()
        ).getDocuments().get(0);
        String eventId = eventDoc.getString("eventId");

        Object regStart = Tasks.await(dbm.getEventRegistrationStart(eventId));
        Object regEnd = Tasks.await(dbm.getEventRegistrationEnd(eventId));

        assertTrue("registrationStart should be a Timestamp", regStart instanceof Timestamp);
        assertTrue("registrationEnd should be a Timestamp", regEnd instanceof Timestamp);

        assertEquals(start, regStart);
        assertEquals(end, regEnd);
    }

    @Test
    public void getEventWaitlist_returnsAllUsers() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // organizer
        String orgEmail = "waitlist_org_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Waitlist Org", orgEmail, "555-3333"));
        DocumentSnapshot orgDoc = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", orgEmail)
                        .get()
        ).getDocuments().get(0);
        String organizerId = orgDoc.getString("userId");

        // event
        String eventName = "Waitlist Event " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(eventName, "wl test", organizerId, null, null)
        );
        DocumentSnapshot eventDoc = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get()
        ).getDocuments().get(0);
        String eventId = eventDoc.getString("eventId");

        // make two entrants
        String email1 = "wl_user1_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Wl User 1", email1, "555-4001"));
        DocumentSnapshot u1 = Tasks.await(
                db.collection("users").whereEqualTo("email", email1).get()
        ).getDocuments().get(0);
        String userId1 = u1.getString("userId");

        String email2 = "wl_user2_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Wl User 2", email2, "555-4002"));
        DocumentSnapshot u2 = Tasks.await(
                db.collection("users").whereEqualTo("email", email2).get()
        ).getDocuments().get(0);
        String userId2 = u2.getString("userId");

        // add both to waitlist
        Tasks.await(dbm.addUserToWaitlist(eventId, userId1));
        Tasks.await(dbm.addUserToWaitlist(eventId, userId2));

        // now get waitlist
        List<String> waitlist = Tasks.await(
                dbm.getEventWaitlist(eventId)
        );

        assertTrue(waitlist.contains(userId1));
        assertTrue(waitlist.contains(userId2));
    }

    @Test
    public void getEventRegistered_returnsAllUsers() throws Exception {
        DbManager dbm = DbManager.getInstance();
        FirebaseFirestore db = dbm.getDb();

        // organizer
        String orgEmail = "reg_org_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Reg Org", orgEmail, "555-5555"));
        DocumentSnapshot orgDoc = Tasks.await(
                db.collection("users")
                        .whereEqualTo("email", orgEmail)
                        .get()
        ).getDocuments().get(0);
        String organizerId = orgDoc.getString("userId");

        // event
        String eventName = "Reg Event " + System.currentTimeMillis();
        Tasks.await(
                dbm.createEvent(eventName, "reg test", organizerId, null, null)
        );
        DocumentSnapshot eventDoc = Tasks.await(
                db.collection("events")
                        .whereEqualTo("eventName", eventName)
                        .get()
        ).getDocuments().get(0);
        String eventId = eventDoc.getString("eventId");

        // make two entrants
        String email1 = "reg_user1_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Reg User 1", email1, "555-6001"));
        DocumentSnapshot u1 = Tasks.await(
                db.collection("users").whereEqualTo("email", email1).get()
        ).getDocuments().get(0);
        String userId1 = u1.getString("userId");

        String email2 = "reg_user2_" + System.currentTimeMillis() + "@example.com";
        Tasks.await(dbm.createUser("Reg User 2", email2, "555-6002"));
        DocumentSnapshot u2 = Tasks.await(
                db.collection("users").whereEqualTo("email", email2).get()
        ).getDocuments().get(0);
        String userId2 = u2.getString("userId");

        // add both to registered
        Tasks.await(dbm.addUserToRegistered(eventId, userId1));
        Tasks.await(dbm.addUserToRegistered(eventId, userId2));

        // now get registered list
        List<String> registered = Tasks.await(
                dbm.getEventRegistered(eventId)
        );

        assertTrue(registered.contains(userId1));
        assertTrue(registered.contains(userId2));
    }

    }