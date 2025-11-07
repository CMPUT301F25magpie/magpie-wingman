package com.example.magpie_wingman.entrant;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;

/**
 * Displays an entrant's landing screen for a specific event and
 * lets the user join or leave that event's waitlist.
 */
public class EntrantLandingFragment extends Fragment {

    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_ENTRANT_ID = "arg_entrant_id";



    // Event the entrant is viewing.
    private String eventId;
    // Current logged-in entrant/user id injected by the caller.
    private String entrantId;

    private String joinLeaveBtn;
    // Local UI flag reflecting whether the user is currently on the waitlist.
    private boolean isOnWaitlist = false;
    public EntrantLandingFragment() {
        // Required empty public constructor
    }

    /**
     * Factory: create a properly-initialized instance.
     *
     * @param eventId   Firestore id of the event document
     * @param entrantId App/user id of the signed-in entrant
     * @return configured fragment instance
     */
    public static EntrantLandingFragment newInstance(@NonNull String eventId,
                                                     @NonNull String entrantId) {
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_ENTRANT_ID, entrantId);
        EntrantLandingFragment f = new EntrantLandingFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Pull required arguments.
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
            entrantId = getArguments().getString(ARG_ENTRANT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_landing, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        joinLeaveBtn = view.findViewById(R.id.button_join_leave);
        joinLeaveBtn.setEnabled(false);

        DbManager.getInstance()
                .isUserInWaitlist(eventId, entrantId)
                .addOnSuccessListener(inWaitlist -> {
                    isOnWaitlist = inWaitlist != null && inWaitlist;
                    renderButton();
                    joinLeaveBtn.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    // If check fails, default to "Join".
                    isOnWaitlist = false;
                    renderButton();
                    joinLeaveBtn.setEnabled(true);
                });

        // Validate required args early; disable UI if missing.
        if (isEmpty(eventId) || isEmpty(entrantId)) {
            if (joinLeaveBtn != null) joinLeaveBtn.setEnabled(false);
            Toast.makeText(requireContext(), "Missing eventId or entrantId", Toast.LENGTH_LONG).show();
            return;
        }

        // Toggle join/leave when the button is pressed.
        joinLeaveBtn.setOnClickListener(v -> {
            joinLeaveBtn.setEnabled(false); // prevent double taps while network call runs
            if (isOnWaitlist) {
                leaveWaitlist();
            } else {
                joinWaitlist();
            }
        });
    }

    /**
     * Updates the button text to match the current membership state.
     * Shows "Leave waitlist" when on waitlist, "Join waitlist" otherwise.
     */

    private void renderButton() {
        joinLeaveBtn.setText(isOnWaitlist ? R.string.leave_waitlist : R.string.join_waitlist);
    }


    /** Adds the current entrant to the event's waitlist and refreshes the UI on success. */
    private void joinWaitlist() {
        DbManager.getInstance()
                .addUserToWaitlist(eventId, entrantId)
                .addOnSuccessListener(v -> {
                    isOnWaitlist = true;
                    renderButton();
                    Toast.makeText(requireContext(), "Joined waitlist", Toast.LENGTH_SHORT).show();
                    joinLeaveBtn.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Join failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    joinLeaveBtn.setEnabled(true);
                });
    }

    /** Removes the current entrant from the event's waitlist and refreshes the UI on success. */
    private void leaveWaitlist() {
        DbManager.getInstance()
                .cancelWaitlist(eventId, entrantId)
                .addOnSuccessListener(v -> {
                    isOnWaitlist = false;
                    renderButton();
                    Toast.makeText(requireContext(), "Left waitlist", Toast.LENGTH_SHORT).show();
                    joinLeaveBtn.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Leave failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    joinLeaveBtn.setEnabled(true);
                });
    }


    /** Small null/empty helper. */
    private static boolean isEmpty(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

}