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

/**
 * Fragment that displays and manages all event invitations for the current entrant.
 *
 * <p>This screen allows entrants to:
 * <ul>
 *     <li>View a list of pending invitations for events</li>
 *     <li>See event details such as name, time range, location, and description</li>
 *     <li>Accept an invitation (moving them into the registrable list for that event)</li>
 *     <li>Decline an invitation (removing it from the invited list)</li>
 * </ul>
 *
 * <p>Data is loaded from Firestore using a collection group query on the
 * {@code invited} subcollections, and additional event details are resolved via
 * their parent event documents.</p>
 */
public class EntrantInvitationsFragment extends Fragment {

    private LinearLayout invitationsList;
    private final List<Invitation> invitations = new ArrayList<>();
    private String userId;

    /**
     * Inflates the invitations fragment layout that contains the container
     * for rendered invitation cards.
     *
     * @param inflater  layout inflater used to inflate views
     * @param container optional parent view
     * @param savedInstanceState previously saved state, if any
     * @return the inflated root view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_invitations, container, false);
    }

    /**
     * Initializes the toolbar back button, invitations list container,
     * resolves the current entrant, and triggers loading of invitations.
     *
     * @param v root view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState previously saved state, if any
     */
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

    /**
     * Loads all invitations for the current user.
     *
     * <p>Implementation details:
     * <ul>
     *     <li>Queries the {@code invited} collection group where {@code userId} matches the current entrant</li>
     *     <li>For each invited doc, resolves the parent event document</li>
     *     <li>Builds an {@link Invitation} model with event details and invited timestamp</li>
     *     <li>Sorts invitations by newest first and applies them to the UI</li>
     * </ul>
     */
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

    /**
     * Replaces the current in-memory invitations list with the given items
     * and re-render the invitations.
     *
     * @param newItems list of invitations to display
     */
    private void applyInvitations(List<Invitation> newItems) {
        invitations.clear();
        invitations.addAll(newItems);
        renderInvitations();
    }

    /**
     * Renders all invitations into the {@link #invitationsList} container
     *
     * <p>Each invitation shows:
     * <ul>
     *     <li>Event title</li>
     *     <li>Registration time range</li>
     *     <li>Location and description</li>
     *     <li>Accept and decline buttons</li>
     * </ul>
     */
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

    /**
     * Handles acceptance of an invitation:
     * <ul>
     *     <li>Moves the entrant from {@code invited} to {@code registrable} for that event</li>
     *     <li>Removes the invitation card from the UI</li>
     *     <li>Shows a confirmation toast</li>
     *     <li>Navigates to the detailed event description so the entrant can register</li>
     * </ul>
     *
     * @param inv      the invitation being accepted
     * @param position index of the invitation in the current list
     */
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

    /**
     * Handles declination of an invitation:
     * <ul>
     *     <li>Removes the user from the event's {@code invited} list</li>
     *     <li>Removes the invitation card from the UI</li>
     *     <li>Shows a confirmation toast</li>
     * </ul>
     *
     * @param inv      the invitation being declined
     * @param position index of the invitation in the current list
     */
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

    /**
     * Extracts a string field from a {@link DocumentSnapshot}.
     *
     * @param d   snapshot containing the field
     * @param key field name to read
     * @return string value or an empty string if null
     */
    private static String getStringSafe(DocumentSnapshot d, String key) {
        Object v = d.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    /**
     * Shared date formatter for displaying registration window ranges.
     */
    private static final SimpleDateFormat SDF =
            new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

    /**
     * Formats a registration start/end range into a readable string.
     *
     * <p>Note:
     * <ul>
     *     <li>If both start and end are present: {@code start – end}</li>
     *     <li>If only one is present: formats that single timestamp</li>
     *     <li>If neither is present: returns an empty string</li>
     * </ul>
     *
     * @param start start timestamp (expected {@link Timestamp} or null)
     * @param end   end timestamp (expected {@link Timestamp} or null)
     * @return formatted date range string
     */
    private static String formatRange(Object start, Object end) {
        Date s = (start instanceof Timestamp) ? ((Timestamp) start).toDate() : null;
        Date e = (end instanceof Timestamp) ? ((Timestamp) end).toDate() : null;
        if (s == null && e == null) return "";
        if (s != null && e != null) return SDF.format(s) + " – " + SDF.format(e);
        return s != null ? SDF.format(s) : SDF.format(e);
    }

    /**
     * Reads the {@code invitedAt} field from an invitation document and returns it as a millis value.
     *
     * <p>Supports multiple stored types:
     * <ul>
     *     <li>{@link Timestamp}: uses its underlying {@link Date}</li>
     *     <li>{@link Long}: used directly</li>
     *     <li>{@link Double}: converted to {@code long}</li>
     * </ul>
     * Falls back to {@code 0L} if the field is missing or of an unknown type.</p>
     *
     * @param doc invitation document snapshot
     * @return epoch milliseconds when the invitation was created, or 0 if unavailable
     */
    private static long readInvitedAt(DocumentSnapshot doc) {
        Object v = doc.get("invitedAt");
        if (v == null) return 0L;
        if (v instanceof Timestamp) return ((Timestamp) v).toDate().getTime();
        if (v instanceof Long) return (Long) v;
        if (v instanceof Double) return ((Double) v).longValue();
        return 0L;
    }

    /**
     * Removes an invitation from the in-memory list at the given position and re-renders
     * the UI to reflect the change.
     *
     * @param position index of the invitation to remove
     */
    private void removeInvitationAt(int position) {
        if (position < 0 || position >= invitations.size()) return;
        invitations.remove(position);
        renderInvitations();
    }
}
