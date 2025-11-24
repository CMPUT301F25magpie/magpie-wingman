package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Notification;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntrantNotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ListenerRegistration registration;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_notifications, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
//        super.onViewCreated(v, savedInstanceState);
//
//        // Toolbar back navigation
//        MaterialToolbar tb = v.findViewById(R.id.toolbar_notifications);
//        if (tb != null) {
//            tb.setNavigationOnClickListener(_v ->
//                    requireActivity().getOnBackPressedDispatcher().onBackPressed());
//        }
//
//        recyclerView = v.findViewById(R.id.recycler_notifications);
//        adapter = new NotificationAdapter();
//        recyclerView.setAdapter(adapter);
//
//        String deviceId = Settings.Secure.getString(
//                requireContext().getContentResolver(),
//                Settings.Secure.ANDROID_ID
//        );
//
//        DbManager.getInstance().findUserByDeviceId(deviceId)
//                .addOnSuccessListener(id -> {
//                    if (!isAdded() || id == null || id.isEmpty()) return;
//                    userId = id;
//                    attachListenerFor(userId);
//                });
    }

    @Override
    public void onDestroyView() {
//        super.onDestroyView();
//        detachListener();
    }

    private void attachListenerFor(String uid) {
//        FirebaseFirestore db = DbManager.getInstance().getDb();
//        CollectionReference ref = db.collection("users")
//                .document(uid)
//                .collection("notifications");
//
//        Query q = ref.orderBy("timestamp", Query.Direction.DESCENDING);
//
//        registration = q.addSnapshotListener((QuerySnapshot snaps, com.google.firebase.firestore.FirebaseFirestoreException e) -> {
//            if (!isAdded()) return;
//            if (e != null || snaps == null) {
//                adapter.submitList(new ArrayList<>());
//                return;
//            }
//            List<Notification> list = new ArrayList<>();
//            snaps.getDocuments().forEach(d -> list.add(Notification.from(d)));
//            adapter.submitList(list);
//        });
    }

    private void detachListener() {
//        if (registration != null) {
//            registration.remove();
//            registration = null;
//        }
    }
}
