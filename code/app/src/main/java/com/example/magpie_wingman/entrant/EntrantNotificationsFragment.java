package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Notification;
import com.example.magpie_wingman.data.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays all notifications for the currently logged-in entrant.
 *
 * <p>This screen:
 * <ul>
 *     <li>Subscribes to real-time updates from the user's {@code notifications} subcollection</li>
 *     <li>Renders each notification as a card with title and body</li>
 *     <li>Automatically refreshes when new notifications arrive or existing ones change</li>
 * </ul>
 *
 * <p>The fragment attaches a Firestore snapshot listener tied to the view lifecycle and
 * removes it in {@link #onDestroyView()}.</p>
 */
public class EntrantNotificationsFragment extends Fragment {

    private LinearLayout notificationsList;
    private final List<Notification> notifications = new ArrayList<>();
    private ListenerRegistration registration;
    private String userId;

    /**
     * Inflates the notifications fragment layout which contains the container
     * container for rendered notification cards.
     *
     * @param inflater  layout inflater used to inflate views
     * @param container optional parent view
     * @param savedInstanceState previously saved state, if any
     * @return the inflated root view for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_notifications, container, false);
    }

    /**
     * Initializes UI views, wires up the back button, resolves the current user,
     * and subscribes for real-time notification updates.
     *
     * @param v root view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Header back button
        ImageButton backBtn = v.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(_v ->
                    requireActivity().getOnBackPressedDispatcher().onBackPressed()
            );
        }

        notificationsList = v.findViewById(R.id.notification_list);

        // Retrieve current logged-in user
        User current = MyApp.getInstance().getCurrentUser();
        if (!isAdded() || current == null) {
            return;
        }

        userId = current.getUserId();
        attachListenerFor(userId);
    }

    /**
     * Cleans up any active Firestore listener when the view is destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachListener();
    }

    /**
     * Attaches a real-time Firestore listener for the given user's notifications.
     *
     * <p>Implementation details:
     * <ul>
     *     <li>Listens to {@code users/{uid}/notifications} ordered by {@code timestamp} descending</li>
     *     <li>Maps each document to a {@link Notification} model</li>
     *     <li>Applies the new list to the UI on each snapshot</li>
     * </ul>
     *
     * @param uid target user ID notifications
     */
    private void attachListenerFor(String uid) {
        FirebaseFirestore db = DbManager.getInstance().getDb();
        CollectionReference ref = db.collection("users")
                .document(uid)
                .collection("notifications");

        Query q = ref.orderBy("timestamp", Query.Direction.DESCENDING);

        registration = q.addSnapshotListener((QuerySnapshot snaps,
                                              com.google.firebase.firestore.FirebaseFirestoreException e) -> {
            if (!isAdded()) return;

            List<Notification> list = new ArrayList<>();
            if (e != null || snaps == null) {
                applyNotifications(list);
                return;
            }

            snaps.getDocuments().forEach(d -> list.add(Notification.from(d)));
            applyNotifications(list);
        });
    }

    /**
     * Replaces the current in-memory notifications list with the given items
     * and re-renders the UI.
     *
     * @param newItems notifications to display
     */
    private void applyNotifications(List<Notification> newItems) {
        notifications.clear();
        notifications.addAll(newItems);
        renderNotifications();
    }

    /**
     * Renders all notifications into the {@link #notificationsList} container.
     *
     * <p>Each notification is inflated from {@code item_notification.xml} and
     * shows its title and message body.</p>
     */
    private void renderNotifications() {
        if (!isAdded() || notificationsList == null) return;

        notificationsList.removeAllViews();

        if (notifications.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (Notification n : notifications) {
            View card = inflater.inflate(R.layout.item_notification, notificationsList, false);

            TextView titleTv = card.findViewById(R.id.text_notification_title);
            TextView messageTv = card.findViewById(R.id.text_message);

            titleTv.setText(n.getTitle());
            messageTv.setText(n.getBody());

            notificationsList.addView(card);
        }
    }

    /**
     * Detaches the active Firestore snapshot listener.
     */
    private void detachListener() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
