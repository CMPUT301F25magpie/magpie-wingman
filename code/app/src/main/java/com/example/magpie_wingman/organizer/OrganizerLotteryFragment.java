package com.example.magpie_wingman.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.NotificationFunction;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
/**
 * Fragment that allows an organizer to run a lottery on the waitlist for an event.
 * US 02.05.02 and US 02.05.01
 * Features include:
 * Selecting a number of entrants to draw into the "invited" list
 * Auto-filling the draw count based on event capacity. This is how the "Backfill cancelled entrants" user stories work
 * When an organizer wishes to replace spots previously occupied by now-cancelled entrants, they just re-do draw by capacity and extra spots are pulled from waitlist
 * Sending notifications to selected and non-selected entrants
 * The lottery is executed using {@link DbManager#drawInvitees(String, int)}, and
 * all notifications are sent using {@link NotificationFunction}.</p>
 */


public class OrganizerLotteryFragment extends Fragment {

    private EditText sampleInput;
    private Button selectButton;
    private CheckBox drawToCapacityCheckBox;
    private String eventId;

    /**
     * Inflates the lottery screen UI and initializes all view references such as input fields,
     * buttons, and checkboxes. Retrieves the eventId from fragment arguments and attaches
     * click listeners for running the lottery and auto-filling the sample size.
     *
     * @param inflater  LayoutInflater used to inflate the fragment's layout.
     * @param container The parent view that will contain the fragment.
     * @param savedInstanceState Saved state if the fragment is being recreated.
     * @return The inflated root view for the lottery screen.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_lottery, container, false);

        sampleInput = view.findViewById(R.id.edit_maximum);
        selectButton = view.findViewById(R.id.button_select);
        drawToCapacityCheckBox = view.findViewById(R.id.checkbox_draw_to_capacity);

        NavController navController = NavHostFragment.findNavController(this);
        View toolbar = view.findViewById(R.id.button_back);
        if (toolbar != null) toolbar.setOnClickListener(v -> navController.navigateUp());

        // Get eventId from navigation args
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        selectButton.setOnClickListener(v -> runLottery());

        // Draw to capacity checkbox
        if (drawToCapacityCheckBox != null) {
            drawToCapacityCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    autoFillSampleToCapacity();
                }
            });
        }

        return view;
    }

    /**
     * Validates user input and performs the lottery draw for the current event.
     * Steps performed:
     *  Ensures eventId and input sample count are valid
     *  Calls {@link DbManager#drawInvitees(String, int)} to select entrants from the waitlist
     *  Sends notifications to selected (invited) entrants
     *  Sends notifications to non-selected (remaining waitlist) entrants
     *  Displays success or error messages to the organizer
     */
    private void runLottery() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(),
                    "Event ID missing — open lottery from event details.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String input = sampleInput.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(getContext(),
                    R.string.error_empty_number,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int sampleCount;
        try {
            sampleCount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(),
                    R.string.error_invalid_number,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (sampleCount <= 0) {
            Toast.makeText(getContext(),
                    R.string.error_non_positive_number,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Lottery draw from db
        DbManager.getInstance()
                .drawInvitees(eventId, sampleCount)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            getContext(),
                            getString(R.string.msg_sample_success, sampleCount),
                            Toast.LENGTH_SHORT
                    ).show();

                    // Notify winners (moved to invited)
                    NotificationFunction notifier = new NotificationFunction();
                    String winnerMessage = getString(R.string.msg_selected_notification);

                    notifier.notifyEntrants(eventId, "invited", winnerMessage)
                            .addOnSuccessListener(unused -> {
                                // Notify losers (remaining in waitlist )
                                String loserMessage = getString(R.string.msg_not_selected_notification);
                                notifier.notifyEntrants(eventId, "waitlist", loserMessage)
                                        .addOnSuccessListener(unused2 -> Toast.makeText(
                                                        getContext(),
                                                        R.string.msg_notify_success,
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        )
                                        .addOnFailureListener(e -> Toast.makeText(
                                                getContext(),
                                                getString(R.string.error_notify_failed, e.getMessage()),
                                                Toast.LENGTH_LONG
                                        ).show());
                            })
                            .addOnFailureListener(e -> Toast.makeText(
                                    getContext(),
                                    getString(R.string.error_notify_failed, e.getMessage()),
                                    Toast.LENGTH_LONG
                            ).show());
                })
                .addOnFailureListener(e -> Toast.makeText(
                        getContext(),
                        getString(R.string.error_lottery_failed, e.getMessage()),
                        Toast.LENGTH_LONG
                ).show());
    }

    /**
     * Auto-fills the sampleInput EditText based on capacity logic:
     *  - If eventCapacity exists:
     *      drawCount = eventCapacity - (invited + registrable + registered)
     *  - If eventCapacity is null/missing:
     *      drawCount = current waitlist size.
     */
    private void autoFillSampleToCapacity() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(),
                    "Event ID missing — cannot compute capacity.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseFirestore db = DbManager.getInstance().getDb();
        DocumentReference eventRef = db.collection("events").document(eventId);

        eventRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Toast.makeText(getContext(),
                        "Event not found.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            Long capLong = doc.getLong("eventCapacity");

            // get # of users for invited, registrable, registered, waitlist
            eventRef.collection("invited").get()
                    .addOnSuccessListener(invSnap -> {
                        int invitedCount = safeSize(invSnap);

                        eventRef.collection("registrable").get()
                                .addOnSuccessListener(regableSnap -> {
                                    int registrableCount = safeSize(regableSnap);

                                    eventRef.collection("registered").get()
                                            .addOnSuccessListener(registeredSnap -> {
                                                int registeredCount = safeSize(registeredSnap);

                                                eventRef.collection("waitlist").get()
                                                        .addOnSuccessListener(waitSnap -> {
                                                            int waitlistCount = safeSize(waitSnap);

                                                            int drawCount;
                                                            if (capLong != null) {
                                                                int capacity = capLong.intValue();
                                                                int used = invitedCount
                                                                        + registrableCount
                                                                        + registeredCount;

                                                                int slotsLeft = capacity - used;
                                                                if (slotsLeft < 0) {
                                                                    slotsLeft = 0; // event over capacity already
                                                                }

                                                                // Do not draw more than there are people in the waitlist
                                                                drawCount = Math.min(waitlistCount, slotsLeft);
                                                            } else {
                                                                // No capacity set = draw up to full waitlist
                                                                drawCount = waitlistCount;
                                                            }

                                                            sampleInput.setText(
                                                                    String.valueOf(drawCount));
                                                        })
                                                        .addOnFailureListener(e ->
                                                                showCapacityError(e.getMessage()));
                                            })
                                            .addOnFailureListener(e ->
                                                    showCapacityError(e.getMessage()));
                                })
                                .addOnFailureListener(e ->
                                        showCapacityError(e.getMessage()));
                    })
                    .addOnFailureListener(e ->
                            showCapacityError(e.getMessage()));

        }).addOnFailureListener(e ->
                showCapacityError(e.getMessage()));
    }

    /**
     * Safely returns the number of documents in a {@link QuerySnapshot},
     * returning 0 if the snapshot is null.
     *
     * @param snap The Firestore QuerySnapshot, or null.
     * @return The number of documents in {@code snap}, or 0 if null.
     */
    private int safeSize(@Nullable QuerySnapshot snap) {
        return (snap == null) ? 0 : snap.size();
    }

    /**
     * Displays a Toast describing an error that occurred during the capacity
     * auto-fill calculation.
     *
     * @param msg Optional error message to append to the Toast. May be null.
     */
    private void showCapacityError(@Nullable String msg) {
        Toast.makeText(getContext(),
                "Failed to compute capacity: " + (msg != null ? msg : ""),
                Toast.LENGTH_LONG).show();
    }
}
