package com.example.magpie_wingman.data.model;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper for sending notifications from organizers to entrants.
 *
 * Writes per-user notification documents under:
 *   users/{userId}/notifications/{notificationId}
 *
 * Fields:
 *   - "message"   (String)
 *   - "timestamp" (long, millis since epoch)
 *   - "eventId"   (String)
 *   - "read"      (Boolean)
 */
public class NotificationFunction {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Sends a notification to all entrants in the given subcollection of an event.
     *
     * @param eventId       ID of the event.
     * @param subcollection One of "waitlist", "registrable", or "cancelled".
     * @param fullMessage   Full message string, e.g., "Title: Body text".
     * @return Task that completes when all writes are committed.
     */
    public Task<Void> notifyEntrants(String eventId,
                                     String subcollection,
                                     String fullMessage) {

        final long now = System.currentTimeMillis();

        CollectionReference groupRef = db.collection("events")
                .document(eventId)
                .collection(subcollection);

        return groupRef.get().continueWithTask(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                throw task.getException() != null
                        ? task.getException()
                        : new Exception("Failed to fetch recipients from " + subcollection);
            }

            List<DocumentSnapshot> docs = task.getResult().getDocuments();
            if (docs.isEmpty()) {
                // No recipients in this group, nothing to send
                return Tasks.forResult(null);
            }

            WriteBatch batch = db.batch();

            for (DocumentSnapshot d : docs) {
                String userId = d.getId(); // assuming doc id is userId

                DocumentReference notifRef = db.collection("users")
                        .document(userId)
                        .collection("notifications")
                        .document(); // auto-id

                Map<String, Object> notifData = new HashMap<>();
                notifData.put("message", fullMessage);
                notifData.put("timestamp", now);
                notifData.put("eventId", eventId);
                notifData.put("read", false);

                batch.set(notifRef, notifData);
            }

            return batch.commit();
        });
    }
}

