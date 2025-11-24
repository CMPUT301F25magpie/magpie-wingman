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

public class EntrantNotificationsFragment extends Fragment {

    private LinearLayout notificationsList;
    private final List<Notification> notifications = new ArrayList<>();
    private ListenerRegistration registration;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_notifications, container, false);
    }

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detachListener();
    }

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

    private void applyNotifications(List<Notification> newItems) {
        notifications.clear();
        notifications.addAll(newItems);
        renderNotifications();
    }

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
            TextView messageTv = card.findViewById(R.id.text_notification_message);

            titleTv.setText(n.getTitle());
            messageTv.setText(n.getBody());

            notificationsList.addView(card);
        }
    }

    private void detachListener() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }
}
