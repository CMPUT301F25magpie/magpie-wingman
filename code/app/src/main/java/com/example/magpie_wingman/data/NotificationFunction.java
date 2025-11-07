package com.example.magpie_wingman.data;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationFunction {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /**
     * sends a notification to all users in a given event subcollection e.g. waitlist / registrable / cancelled (not done yet)
     *
     * @param eventId event ID
     * @param subcollectionName waitlist / registrable / cancelled
     * @param message  message to send
     * @return Task<Void>
     */
    public Task<Void> notifyEntrants(String eventId, String subcollectionName, String message) {
        TaskCompletionSource<Void> tcs = new TaskCompletionSource<>();

        executor.execute(() -> {
            try {
                QuerySnapshot query = Tasks.await(
                        db.collection("events")
                                .document(eventId)
                                .collection(subcollectionName)
                                .get()
                );
                List<DocumentSnapshot> docs = query.getDocuments();
                WriteBatch batch = db.batch();
                long now = System.currentTimeMillis();

                for (DocumentSnapshot doc : docs) {
                    String userId = doc.getId();

                    Map<String, Object> notif = new HashMap<>();
                    notif.put("message", message);
                    notif.put("timestamp", now);
                    notif.put("eventId", eventId);
                    notif.put("read", false);

                    batch.set(
                            db.collection("users")
                                    .document(userId)
                                    .collection("notifications")
                                    .document(),
                            notif
                    );
                }
                Tasks.await(batch.commit());
                tcs.setResult(null);
            } catch (Exception e) {
                tcs.setException(e);
            }
        });
        return tcs.getTask();
    }
}