package com.example.magpie_wingman.data;

import android.content.Context;
import android.provider.Settings;

import androidx.annotation.Nullable;

import com.example.magpie_wingman.data.model.User;
import com.example.magpie_wingman.data.model.UserProfile;
import com.example.magpie_wingman.data.model.UserRole;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * This utility class contains all of the "Helper Methods" that read from and write to the database
 * Helper method functionalities include:
 * -  Creating and deleting users/events
 * -  Moving users to waitlist -> registrable -> registered subcollections in an event's doc
 * -  Removing users from the above subcollections
 * -  Changing user's organizer perms
 * -  Getters and setters for user/events' info
 */
public class DbManager {

    private static DbManager instance;

    private final FirebaseFirestore db;
    private final Context appContext;
    private final SecureRandom random = new SecureRandom();

    private DbManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
    }

    public static synchronized void init(Context context) {
        if (instance == null) {
            instance = new DbManager(context);
        }
    }

    public static synchronized DbManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("DbManager not initialized. Call DbManager.init(context) first.");
        }
        return instance;
    }

    public FirebaseFirestore getDb() {
        return db;
    }

    /**
     * Generates a unique internal ID for user documents by using the first name and a randomized 4 digit integer
     * @param name - the inputted name for the user.
     * @return userID the document ID for the user's firebase document
     */
    public String generateUserId(String name) {
        String cleanName = name.trim().replaceAll("\\s+", "").toLowerCase(Locale.ROOT);

        while (true) {
            int suffix = 1000 + random.nextInt(9000);
            String userId = cleanName + "#" + suffix;

            try {
                DocumentSnapshot doc = Tasks.await(
                        db.collection("users").document(userId).get()
                );

                if (!doc.exists()) {
                    return userId;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return userId;
            }
        }
    }

    /**
     * Creates a new user document.
     * The userId will be generated using generateUserID and will also act as the document ID
     */
    public Task<Void> createUser(String name, String email, String phone) {
        String userId = generateUserId(name);

        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("name", name);
        user.put("email", email);
        user.put("phone", phone);
        user.put("isOrganizer", true);
        user.put("deviceId", Settings.Secure.getString(
                appContext.getContentResolver(),
                Settings.Secure.ANDROID_ID
        ));

        return db.collection("users")
                .document(userId)
                .set(user, SetOptions.merge());

    }

    /**
     * Creates a new event document in Firestore.
     * The event ID is generated from the first word in the event name + random 4-digit number.
     *
     * @param eventName        the name of the event
     * @param description      the event description
     * @param organizerId      the userId of the organizer (to store reference)
     * @param regStart         registration start date (String or Timestamp)
     * @param regEnd           registration end date (String or Timestamp)
     */
    public Task<Void> createEvent(String eventName,
                                  String description,
                                  String organizerId,
                                  Object regStart,
                                  Object regEnd) {

        String eventId;
        DocumentSnapshot doc;

        while (true) {
            String firstWord = eventName.trim().split("\\s+")[0].toLowerCase(Locale.ROOT);
            int suffix = 1000 + random.nextInt(9000);
            eventId = firstWord + "#" + suffix;

            try {
                doc = Tasks.await(db.collection("events").document(eventId).get());
                if (!doc.exists()) break; // no collision, proceed
            } catch (Exception e) {
                e.printStackTrace(); //if there's a collision it will show in the stack trace and retry a new number
                continue;
            }
        }

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", eventId);
        event.put("eventName", eventName);
        event.put("description", description);
        event.put("organizerId", organizerId);
        event.put("registrationStart", regStart);
        event.put("registrationEnd", regEnd);

        return db.collection("events")
                .document(eventId)
                .set(event, SetOptions.merge());
    }
    public Task<Void> deleteEntrant(String userId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Get all events
                QuerySnapshot eventsSnap = Tasks.await(db.collection("events").get());

                // Single batch (no chunking)
                WriteBatch batch = db.batch();

                for (DocumentSnapshot ev : eventsSnap.getDocuments()) {
                    com.google.firebase.firestore.DocumentReference evRef = ev.getReference();

                    // waitlist
                    QuerySnapshot wl = Tasks.await(
                            evRef.collection("waitlist").whereEqualTo("userId", userId).get()
                    );
                    for (DocumentSnapshot d : wl.getDocuments()) {
                        batch.delete(d.getReference());
                    }

                    // registrable
                    QuerySnapshot rg = Tasks.await(
                            evRef.collection("registrable").whereEqualTo("userId", userId).get()
                    );
                    for (DocumentSnapshot d : rg.getDocuments()) {
                        batch.delete(d.getReference());
                    }

                    // registered
                    QuerySnapshot rd = Tasks.await(
                            evRef.collection("registered").whereEqualTo("userId", userId).get()
                    );
                    for (DocumentSnapshot d : rd.getDocuments()) {
                        batch.delete(d.getReference());
                    }
                }

                // Finally delete the user doc
                batch.delete(db.collection("users").document(userId));

                // Commit once
                Tasks.await(batch.commit());
                tcs.setResult(null);
            } catch (Exception e) {
                tcs.setException(e);
            }
        });

        return tcs.getTask();
    }

    public Task<Void> deleteOrganizer(String organizerId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                QuerySnapshot organizerEvents = Tasks.await(
                        db.collection("events")
                                .whereEqualTo("organizerId", organizerId)
                                .get()
                );

                List<Task<Void>> deletes = new ArrayList<>();
                for (DocumentSnapshot d : organizerEvents.getDocuments()) {
                    deletes.add(deleteEvent(d.getId()));
                }

                Tasks.await(Tasks.whenAll(deletes));
                Tasks.await(deleteEntrant(organizerId));

                tcs.setResult(null);
            } catch (Exception e) {
                tcs.setException(e);
            }
        });

        return tcs.getTask();
    }

    public Task<List<UserProfile>> fetchProfiles(@Nullable UserRole roleFilter) {
        TaskCompletionSource<List<UserProfile>> tcs = new TaskCompletionSource<>();

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Query q = db.collection("users");
                if (roleFilter == UserRole.ORGANIZER) {
                    q = q.whereEqualTo("isOrganizer", true);
                } else if (roleFilter == UserRole.ENTRANT) {
                    q = q.whereEqualTo("isOrganizer", false);
                }

                QuerySnapshot snap = Tasks.await(q.get());

                List<UserProfile> out = new ArrayList<>();
                for (DocumentSnapshot d : snap.getDocuments()) {
                    String userId = d.getId();
                    String name = d.getString("name");
                    String image = d.getString("profileImageUrl");
                    Boolean isOrg = d.getBoolean("isOrganizer");

                    UserRole role = (isOrg != null && isOrg)
                            ? UserRole.ORGANIZER
                            : UserRole.ENTRANT;

                    if (roleFilter == null || role == roleFilter) {out.add(new UserProfile(userId,
                            name, role));
                    }
                }

                tcs.setResult(out);
            } catch (Exception e) {
                tcs.setException(e);
            }
        });
        return tcs.getTask();
    }
    /**
     * Deletes a user profile based on their role.
     *
     * @param userId the Firestore document id of the user
     * @param role   the user's current role
     * @return a Task that completes when all writes are finished or fails with the underlying exception
     */
    public Task<Void> deleteProfile(String userId, com.example.magpie_wingman.data.model.UserRole role) {
        if (role == com.example.magpie_wingman.data.model.UserRole.ORGANIZER) {
            return deleteOrganizer(userId);
        }
        return deleteEntrant(userId);
    }

    /**
     * Deletes an event document and its subcollections
     * ("waitlist", "registrable", "registered") from Firestore.
     *
     * @param eventId The ID of the event to delete.
     * @return A Task that completes when the delete finishes. Result is {@code null} if success.)
     */
    public Task<Void> deleteEvent(String eventId) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>(); //I create a custom task for methods that require multiple firestore tasks.

        Executors.newSingleThreadExecutor().execute(() -> { //since we await here we want to create a background thread to avoid freezing UI
            try {
                // reference to the event doc to delete
                DocumentReference eventRef = db.collection("events").document(eventId);

                // get subcollections' docs
                QuerySnapshot waitlistSnap = Tasks.await( //we await to make sure we have all of the references to the subcollection before deleting everything
                        eventRef.collection("waitlist").get()
                );
                QuerySnapshot registrableSnap = Tasks.await(
                        eventRef.collection("registrable").get()
                );
                QuerySnapshot registeredSnap = Tasks.await(
                        eventRef.collection("registered").get()
                );

                WriteBatch batch = db.batch();

                // delete all user docs
                for (DocumentSnapshot doc : waitlistSnap.getDocuments()) {
                    batch.delete(doc.getReference());
                }

                for (DocumentSnapshot doc : registrableSnap.getDocuments()) {
                    batch.delete(doc.getReference());
                }

                for (DocumentSnapshot doc : registeredSnap.getDocuments()) {
                    batch.delete(doc.getReference());
                }

                // now we can delete the actual event doc
                batch.delete(eventRef);

                // commit and wait until it's done
                Tasks.await(batch.commit());

                //if success set result to null (success)
                tcs.setResult(null);
            } catch (Exception e) {
                tcs.setException(e); //exception handling
            }
        });

        return tcs.getTask();
    }

    /**
     * Adds a userID document to the event document's waitlist subcollection (creates one if it doesn't exist yet)
     * @param eventId - ID of event doc in question
     * @param userId - ID of the user
     * @return
     */
    public Task<Void> addUserToWaitlist(String eventId, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("addedAt", System.currentTimeMillis());  // simple timestamp

        return db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(userId)
                .set(data, SetOptions.merge());
    }

    /**
     * Randomly selects count users from an event's waitlist and moves them
     * to the "registrable" subcollection in Firestore.
     *
     * @param eventId The ID of the event to update.
     * @param count   The number of users to switch from waitlist to registrable.
     * @return A google.android.gms.tasks.Task that completes when all Firestore
     *         reads and writes are finished (result is null on success).
     */
    public Task<Void> addUsersToRegistrable(String eventId, int count) {
        //create an empty task for this as its more complex. Will tell callers if and when it succeeds.
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        // create a background thread so this doesn't freeze the UI (has an await).
        // Actual method operation code is wrapped in this
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                //query the waitlist subcollection
                QuerySnapshot waitlistSnap = Tasks.await(
                        db.collection("events")
                                .document(eventId)
                                .collection("waitlist")
                                .get()
                );

                List<DocumentSnapshot> waitlistDocs = waitlistSnap.getDocuments(); //list of waitlist user documents

                if (waitlistDocs.isEmpty() || count <= 0) {
                    tcs.setResult(null);
                    return;
                }

                // randomize order of the waitlist users
                Collections.shuffle(waitlistDocs, random);

                // select first x users
                int actualCount = Math.min(count, waitlistDocs.size());

                //Use batch to reduce number of read/writes
                WriteBatch batch = db.batch();

                for (int i = 0; i < actualCount; i++) {
                    DocumentSnapshot userDoc = waitlistDocs.get(i);
                    String userId = userDoc.getId();

                    DocumentReference waitlistRef = db.collection("events")
                            .document(eventId)
                            .collection("waitlist")
                            .document(userId);

                    DocumentReference registrableRef = db.collection("events")
                            .document(eventId)
                            .collection("registrable")
                            .document(userId);

                    Map<String, Object> regData = new HashMap<>();
                    regData.put("userId", userId);
                    regData.put("invitedAt", System.currentTimeMillis());

                    batch.set(registrableRef, regData, SetOptions.merge());
                    batch.delete(waitlistRef);
                }

                // commit the changes in a batch. We await before returning the task
                Tasks.await(batch.commit());

                // If no exceptions, set the task result to success (null)
                tcs.setResult(null);

                // If not, set the result to the exception.
            } catch (Exception e) {
                tcs.setException(e);
            }
        });
        //return the task result to the caller
        return tcs.getTask();
    }
    public Task<Void> addUserToRegistered(String eventId, String userId) {

        com.google.firebase.firestore.WriteBatch batch = db.batch();

        // source doc (registrable)
        com.google.firebase.firestore.DocumentReference registrableRef =
                db.collection("events")
                        .document(eventId)
                        .collection("registrable")
                        .document(userId);

        // target doc (registered)
        com.google.firebase.firestore.DocumentReference registeredRef =
                db.collection("events")
                        .document(eventId)
                        .collection("registered")
                        .document(userId);

        // copy minimal data
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("movedAt", System.currentTimeMillis());

        // write to registered
        batch.set(registeredRef, data, SetOptions.merge());
        // delete from registrable
        batch.delete(registrableRef);

        return batch.commit();
    }

    /**
     * Removes the given user from the waitlist of the given event.
     *
     * @param eventId The event to update.
     * @param userId  The user to remove.
     * @return A Task that completes when the delete finishes.
     */
    public Task<Void> cancelWaitlist(String eventId, String userId) {
        return db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(userId)
                .delete();
    }

    /**
     * Removes the given user from the registrable of the given event.
     *
     * @param eventId The event to update.
     * @param userId  The user to remove.
     * @return A Task that completes when the delete finishes.
     */
    public Task<Void> cancelRegistrable(String eventId, String userId) {
        return db.collection("events")
                .document(eventId)
                .collection("registrable")
                .document(userId)
                .delete();
    }

    /**
     * Removes the given user from the registered subcollection of the given event.
     *
     * @param eventId The event to update.
     * @param userId  The user to remove.
     * @return A Task that completes when the delete finishes.
     */
    public Task<Void> cancelRegistered(String eventId, String userId) {
        return db.collection("events")
                .document(eventId)
                .collection("registered")
                .document(userId)
                .delete();
    }
    /**
     * Updates the user's display name.
     *
     * @param userId  ID of the user document to update.
     * @param newName New name to set.
     * @return Task that completes when the update finishes.
     */
    public Task<Void> updateName(String userId, String newName) {
        return db.collection("users")
                .document(userId)
                .update("name", newName);
    }

    /**
     * Updates the user's email address.
     *
     * @param userId   ID of the user document to update.
     * @param newEmail New email to set.
     * @return Task that completes when the update finishes.
     */
    public Task<Void> updateEmail(String userId, String newEmail) {
        return db.collection("users")
                .document(userId)
                .update("email", newEmail);
    }

    /**
     * Updates the user's phone number.
     *
     * @param userId     ID of the user document to update.
     * @param newPhone   New phone number to set.
     * @return Task that completes when the update finishes.
     */
    public Task<Void> updatePhoneNumber(String userId, String newPhone) {
        return db.collection("users")
                .document(userId)
                .update("phone", newPhone);
    }

    /**
     * Changes a user's organizer permissions.
     *
     * @param userId      The ID of the user to update.
     * @param isOrganizer True to grant organizer permissions; false to revoke them.
     * @return A Task that completes when the update finishes.
     */
    public Task<Void> changeOrgPerms(String userId, boolean isOrganizer) {
        return db.collection("users")
                .document(userId)
                .update("isOrganizer", isOrganizer);
    }

    /**
     * Gets the user's name from Firestore.
     *
     * @param userId ID of the user document.
     * @return Task resolving to the user's name.
     */
    public Task<String> getUserName(String userId) {
        return db.collection("users").document(userId)
                .get()
                .continueWith(task -> task.getResult().getString("name"));
    }

    /**
     * Gets the user's email from Firestore.
     *
     * @param userId ID of the user document.
     * @return Task resolving to the user's email.
     */
    public Task<String> getUserEmail(String userId) {
        return db.collection("users").document(userId)
                .get()
                .continueWith(task -> task.getResult().getString("email"));
    }

    /**
     * Gets whether the user is an organizer.
     *
     * @param userId ID of the user document.
     * @return Task resolving to true if organizer, false otherwise.
     */
    public Task<Boolean> getIsOrganizer(String userId) {
        return db.collection("users").document(userId)
                .get()
                .continueWith(task -> task.getResult().getBoolean("isOrganizer"));
    }

    /**
     * Gets the user's phone number.
     *
     * @param userId ID of the user document.
     * @return Task resolving to the user's phone number.
     */
    public Task<String> getUserPhone(String userId) {
        return db.collection("users").document(userId)
                .get()
                .continueWith(task -> task.getResult().getString("phone"));
    }

    /**
     * Gets the device ID stored for the user.
     *
     * @param userId ID of the user document.
     * @return Task resolving to the user's device ID.
     */
    public Task<String> getUserDeviceId(String userId) {
        return db.collection("users").document(userId)
                .get()
                .continueWith(task -> task.getResult().getString("deviceId"));
    }
    /**
     * Gets the organizer ID associated with a given event.
     *
     * @param eventId The unique identifier of the event.
     * @return A Task that resolves to the organizer's user ID as a String,
     *         or null if the event does not exist or the field is missing.
     */
    public Task<String> getEventOrganizer(String eventId) {
        return db.collection("events")
                .document(eventId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return null;
                    }
                    return task.getResult().getString("organizerId");
                });
    }

    /**
     * Gets the description of a given event.
     *
     * @param eventId The unique identifier of the event.
     * @return A Task that resolves to the event's description as a String,
     *         or null if the event does not exist or the field is missing.
     */
    public Task<String> getEventDescription(String eventId) {
        return db.collection("events")
                .document(eventId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return null;
                    }
                    return task.getResult().getString("description");
                });
    }

    /**
     * Retrieves the registration period start value for a given event.
     *
     * @param eventId The unique identifier of the event.
     * @return A Task that resolves to the registration start Object,
     *         or null if the event does not exist or the field is missing.
     */
    public Task<Object> getEventRegistrationStart(String eventId) {
        return db.collection("events")
                .document(eventId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return null;
                    }
                    // you saved regStart as Object, so return as-is
                    return task.getResult().get("registrationStart");
                });
    }

    /**
     * Retrieves the registration period end value for a given event.

     * @param eventId The unique identifier of the event.
     * @return A Task that resolves to the registration end Object,
     *         or null if the event does not exist or the field is missing.
     */
    public Task<Object> getEventRegistrationEnd(String eventId) {
        return db.collection("events")
                .document(eventId)
                .get()
                .continueWith(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return null;
                    }
                    return task.getResult().get("registrationEnd");
                });
    }

    /**
     * Retrieves all user IDs currently on the waitlist for a given event.
     *
     * @param eventId The unique identifier of the event.
     * @return A Task that resolves to a List of user IDs (Strings)
     *         representing users in the "waitlist" subcollection.
     *         Returns an empty list if the event or subcollection is missing.
     */
    public Task<List<String>> getEventWaitlist(String eventId) {
        return db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .get()
                .continueWith(task -> {
                    List<String> users = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return users;
                    }
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        users.add(doc.getId());
                    }
                    return users;
                });
    }

    /**
     * Retrieves all user IDs currently marked as registrable for a given event.
     *
     * @param eventId The unique identifier of the event.
     * @return A Task that resolves to a List of user IDs (Strings)
     *         representing users in the "registrable" subcollection.
     *         Returns an empty list if the event or subcollection is missing.
     */
    public Task<List<String>> getEventRegistrable(String eventId) {
        return db.collection("events")
                .document(eventId)
                .collection("registrable")
                .get()
                .continueWith(task -> {
                    List<String> users = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return users;
                    }
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        users.add(doc.getId());
                    }
                    return users;
                });
    }

    /**
     * Retrieves all user IDs currently registered for a given event.
     *
     * @param eventId The unique identifier of the event.
     * @return A Task that resolves to a List of user IDs (Strings)
     *         representing users in the "registered" subcollection.
     *         Returns an empty list if the event or subcollection is missing.
     */
    public Task<List<String>> getEventRegistered(String eventId) {
        return db.collection("events")
                .document(eventId)
                .collection("registered")
                .get()
                .continueWith(task -> {
                    List<String> users = new ArrayList<>();
                    if (!task.isSuccessful() || task.getResult() == null) {
                        return users;
                    }
                    for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                        users.add(doc.getId());
                    }
                    return users;
                });
    }

}

