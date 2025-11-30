package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Invitation;
import com.example.magpie_wingman.data.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntrantInvitationsFragment extends Fragment {

    private LinearLayout invitationsList;
    private final List<Invitation> invitations = new ArrayList<>();
    private String userId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_invitations, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Header back button
        ImageButton backBtn = v.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(_v ->
                    requireActivity().getOnBackPressedDispatcher().onBackPressed()
            );
        }

        invitationsList = v.findViewById(R.id.invitations_list);

        // Retrieve current logged-in user
        User current = MyApp.getInstance().getCurrentUser();
        userId = current.getUserId();
        loadInvitations();
    }

    // Load all events where this user is present in events/{eventId}/registrable/{userId}
    private void loadInvitations() {
        if (TextUtils.isEmpty(userId)) return;

        FirebaseFirestore db = DbManager.getInstance().getDb();

        db.collectionGroup("invited")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snaps -> {
                    List<Invitation> out = new ArrayList<>();
                    if (snaps.isEmpty()) {
                        applyInvitations(out);
                        return;
                    }

                    final int[] pending = {snaps.size()};
                    for (DocumentSnapshot invitedDoc : snaps) {
                        DocumentReference eventDoc = invitedDoc.getReference().getParent().getParent();
                        if (eventDoc == null) {
                            if (--pending[0] == 0) {
                                out.sort((a, b) -> Long.compare(b.getInvitedAt(), a.getInvitedAt()));
                                applyInvitations(out);
                            }
                            continue;
                        }

                        eventDoc.get().addOnSuccessListener(ev -> {
                            if (ev != null && ev.exists()) {
                                String eventId = ev.getId();
                                String name = getStringSafe(ev, "eventName");
                                String desc = getStringSafe(ev, "description");
                                String location = getStringSafe(ev, "eventLocation");
                                String datetime = formatRange(ev.get("registrationStart"),
                                        ev.get("registrationEnd"));

                                long invitedAt = readInvitedAt(invitedDoc);

                                out.add(new Invitation(eventId, name, datetime, location, desc, invitedAt));
                            }

                            if (--pending[0] == 0) {
                                // Sort by newest invitation
                                out.sort((a, b) -> Long.compare(b.getInvitedAt(), a.getInvitedAt()));
                                applyInvitations(out);
                            }
                        }).addOnFailureListener(e -> {
                            if (--pending[0] == 0) {
                                out.sort((a, b) -> Long.compare(b.getInvitedAt(), a.getInvitedAt()));
                                applyInvitations(out);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Invitations", "Failed to load invitations: " + e.getMessage());
                    Toast.makeText(
                            requireContext(),
                            "Failed to load invitations: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void applyInvitations(List<Invitation> newItems) {
        invitations.clear();
        invitations.addAll(newItems);
        renderInvitations();
    }

    private void renderInvitations() {
        if (!isAdded() || invitationsList == null) return;

        invitationsList.removeAllViews();

        if (invitations.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());

        for (int i = 0; i < invitations.size(); i++) {
            Invitation inv = invitations.get(i);

            // Reuse card layout (item_invitation.xml) for each invitation
            View card = inflater.inflate(R.layout.item_invitation, invitationsList, false);

            TextView titleTv = card.findViewById(R.id.text_event_title);
            TextView datetimeTv = card.findViewById(R.id.text_event_date);
            TextView locationTv = card.findViewById(R.id.text_event_location);
            TextView descTv = card.findViewById(R.id.text_event_description);
            ImageButton btnAccept = card.findViewById(R.id.button_accept);
            ImageButton btnDecline = card.findViewById(R.id.button_decline);

            titleTv.setText(inv.getEventName());
            datetimeTv.setText(inv.getDatetime());
            locationTv.setText(inv.getLocation());
            descTv.setText(inv.getDescription());

            final int position = i;
            btnAccept.setOnClickListener(v -> onAccept(inv, position));
            btnDecline.setOnClickListener(v -> onDecline(inv, position));

            invitationsList.addView(card);
        }
    }

    public void onAccept(Invitation inv, int position) {
        if (TextUtils.isEmpty(userId)) return;

        DbManager.getInstance()
                .moveUserFromInvitedToRegistrable(inv.getEventId(), userId)
                .addOnSuccessListener(_v -> {
                    removeInvitationAt(position);
                    Toast.makeText(requireContext(),
                            "Invitation accepted. You can now register.",
                            Toast.LENGTH_SHORT).show();

                    // Navigate to event details so user can complete registration
                    Bundle args = new Bundle();
                    args.putString("eventId", inv.getEventId());
                    args.putString("eventName", inv.getEventName());
                    args.putString("eventLocation", inv.getLocation());
                    args.putString("eventDescription", inv.getDescription());
                    // Start time will be reloaded from Firestore

                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigate(R.id.detailedEventDescriptionFragment, args);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Accept failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    public void onDecline(Invitation inv, int position) {
        if (TextUtils.isEmpty(userId)) return;

        DbManager.getInstance()
                .cancelInvited(inv.getEventId(), userId)
                .addOnSuccessListener(_v -> {
                    removeInvitationAt(position);
                    Toast.makeText(requireContext(),
                            "Invitation declined.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Decline failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    // helpers
    private static String getStringSafe(DocumentSnapshot d, String key) {
        Object v = d.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

    private static String formatRange(Object start, Object end) {
        Date s = (start instanceof Timestamp) ? ((Timestamp) start).toDate() : null;
        Date e = (end instanceof Timestamp) ? ((Timestamp) end).toDate() : null;
        if (s == null && e == null) return "";
        if (s != null && e != null) return SDF.format(s) + " – " + SDF.format(e);
        return s != null ? SDF.format(s) : SDF.format(e);
    }

    private static long readInvitedAt(DocumentSnapshot doc) {
        Object v = doc.get("invitedAt");
        if (v == null) return 0L;
        if (v instanceof Timestamp) return ((Timestamp) v).toDate().getTime();
        if (v instanceof Long) return (Long) v;
        if (v instanceof Double) return ((Double) v).longValue();
        return 0L;
    }

    private void removeInvitationAt(int position) {
        if (position < 0 || position >= invitations.size()) return;
        invitations.remove(position);
        renderInvitations();
    }
}
