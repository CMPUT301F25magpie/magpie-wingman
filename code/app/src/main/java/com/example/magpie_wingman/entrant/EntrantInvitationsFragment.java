package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Invitation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntrantInvitationsFragment extends Fragment implements InvitationAdapter.OnActionListener {

    private RecyclerView recycler;
    private InvitationAdapter adapter;
    private String userId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_invitations, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        MaterialToolbar tb = v.findViewById(R.id.toolbar_invitations);
        if (tb != null) {
            tb.setNavigationOnClickListener(_v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }

        recycler = v.findViewById(R.id.recycler_invitations);
        adapter = new InvitationAdapter(this);
        recycler.setAdapter(adapter);

        // Load current user data (Pls pass userID when navigating)
        userId = getArguments() != null ? getArguments().getString("userId") : null;
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(requireContext(), "Missing userId.", Toast.LENGTH_SHORT).show();
            return;
        }

        loadInvitations();
    }

    // Load all events where this user is present in events/{eventId}/registrable/{userId}
    private void loadInvitations() {
        FirebaseFirestore db = DbManager.getInstance().getDb();

        db.collectionGroup("registrable")
                .whereEqualTo(FieldPath.documentId(), userId)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Invitation> out = new ArrayList<>();
                    if (snaps.isEmpty()) {
                        adapter.setItems(out);
                        return;
                    }

                    final int[] pending = {snaps.size()};
                    for (DocumentSnapshot registrableDoc : snaps) {
                        DocumentReference eventDoc = registrableDoc.getReference().getParent().getParent();
                        if (eventDoc == null) {
                            if (--pending[0] == 0) adapter.setItems(out);
                            continue;
                        }

                        eventDoc.get().addOnSuccessListener(ev -> {
                            String eventId = ev.getId();
                            String name = getStringSafe(ev, "eventName");
                            String desc = getStringSafe(ev, "description");
                            String location = getStringSafe(ev, "eventLocation");
                            String datetime = formatRange(ev.get("registrationStart"), ev.get("registrationEnd"));

                            long invitedAt = 0L;
                            Timestamp ts = registrableDoc.getTimestamp("invitedAt");
                            if (ts != null) invitedAt = ts.toDate().getTime();

                            out.add(new Invitation(eventId, name, datetime, location, desc, invitedAt));

                            if (--pending[0] == 0) {
                                out.sort((a, b) -> Long.compare(b.getInvitedAt(), a.getInvitedAt()));
                                adapter.setItems(out);
                            }
                        }).addOnFailureListener(e -> {
                            if (--pending[0] == 0) adapter.setItems(out);
                        });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load invitations: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // Adapter actions
    @Override
    public void onAccept(Invitation inv, int position) {
        DbManager.getInstance()
                .addUserToRegistered(inv.getEventId(), userId)
                .addOnSuccessListener(_v -> {
                    adapter.removeAt(position);
                    Toast.makeText(requireContext(), "Invitation accepted.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Accept failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    @Override
    public void onDecline(Invitation inv, int position) {
        DbManager.getInstance()
                .cancelRegistrable(inv.getEventId(), userId)
                .addOnSuccessListener(_v -> {
                    adapter.removeAt(position);
                    Toast.makeText(requireContext(), "Invitation declined.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Decline failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // helpers
    private static String getStringSafe(DocumentSnapshot d, String key) {
        Object v = d.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    private static final SimpleDateFormat SDF = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

    private static String formatRange(Object start, Object end) {
        Date s = (start instanceof Timestamp) ? ((Timestamp) start).toDate() : null;
        Date e = (end   instanceof Timestamp) ? ((Timestamp) end).toDate() : null;
        if (s == null && e == null) return "";
        if (s != null && e != null) return SDF.format(s) + " – " + SDF.format(e);
        return s != null ? SDF.format(s) : SDF.format(e);
    }
}
