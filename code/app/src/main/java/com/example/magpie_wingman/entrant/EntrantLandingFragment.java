package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;

/**
 * Entrant landing screen.
 *
 * <p>Dual-mode behavior:</p>
 * <ul>
 *   <li><b>List mode</b> (no eventId passed): shows search + RecyclerView; bottom buttons are regular actions.</li>
 *   <li><b>Event mode</b> (eventId passed): reuses the bottom "Events" button as a Join/Leave waitlist action for that event.</li>
 * </ul>
 *
 * <p>Arguments:</p>
 * <ul>
 *   <li>{@code arg_event_id} (optional) – if present, enables Join/Leave behavior.</li>
 *   <li>{@code arg_entrant_id} (required for Join/Leave) – current user id.</li>
 * </ul>
 */
public class EntrantLandingFragment extends Fragment {

    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_ENTRANT_ID = "arg_entrant_id";

    // Arguments
    private String eventId;
    private String entrantId;

    // UI from XML
    private EditText searchBar;
    private ImageView btnFilter, btnInfo, btnSettings;
    private RecyclerView eventsRecycler;
    private Button btnInvitations, btnScanQr, btnEventsPrimary, btnNotifications;

    // Local state for Join/Leave
    private boolean isOnWaitlist = false;

    public EntrantLandingFragment() {
        // Required empty public constructor
    }

    /**
     * create a properly-initialized instance.
     *
     * @param eventId   Firestore id of the event document (optional; if present, enables join/leave)
     * @param entrantId App/user id of the signed-in entrant (required for join/leave)
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
        // Inflate your provided XML
        return inflater.inflate(R.layout.fragment_entrant_landing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // --- Bind views to XML IDs ---
        searchBar        = v.findViewById(R.id.search_bar);
        btnFilter        = v.findViewById(R.id.btn_filter);
        btnInfo          = v.findViewById(R.id.btn_info);
        btnSettings      = v.findViewById(R.id.btn_settings);
        eventsRecycler   = v.findViewById(R.id.recycler_view_events);
        btnInvitations   = v.findViewById(R.id.btn_invitations);
        btnScanQr        = v.findViewById(R.id.btn_scan_qr);
        btnEventsPrimary = v.findViewById(R.id.btn_events);
        btnNotifications = v.findViewById(R.id.btn_notification);

        NavController navController = Navigation.findNavController(v);

        // --- Top bar actions ---
        btnFilter.setOnClickListener(x -> navController.navigate(R.id.action_entrantLandingFragment3_to_entrantEventSearchFilterFragment));

        btnInfo.setOnClickListener(x ->
                navController.navigate(R.id.action_entrantLandingFragment3_to_entrantDetailsFragment));

        btnSettings.setOnClickListener( x -> navController.navigate(R.id.action_entrantLandingFragment3_to_entrantSettingsFragment));

        // --- Bottom bar actions ---
        btnInvitations.setOnClickListener(x -> navController.navigate(R.id.action_entrantLandingFragment3_to_entrantInvitationsFragment));
        btnScanQr.setOnClickListener(x -> navController.navigate(R.id.action_entrantLandingFragment3_to_scanQRFragment));
        btnEventsPrimary.setOnClickListener(x -> navController.navigate(R.id.action_entrantLandingFragment3_to_entrantEventsFragment));
        btnNotifications.setOnClickListener(x -> navController.navigate(R.id.action_entrantLandingFragment3_to_entrantNotificationsFragment));

//        // --- List mode and Event mode wiring ---
//        if (isEmpty(eventId)) {
//            // LIST MODE: keep "Events" label and normal behavior
////            btnEventsPrimary.setText(R.string.events);
//            btnEventsPrimary.setEnabled(true);
//            btnEventsPrimary.setOnClickListener(x ->
//                    Toast.makeText(requireContext(), "Events tapped", Toast.LENGTH_SHORT).show());

//            // TODO: set up RecyclerView adapter & data source here.
//            // eventsRecycler.setLayoutManager(...); eventsRecycler.setAdapter(...);
//            // loadEventsAndSubmitToAdapter();
//        } else {
//            // EVENT MODE: reuse this button as Join/Leave for the given eventId
//            setupJoinLeaveForEvent();
//        }
    }

    // Join/Leave logic (only active when eventId is provided)

    /**
     * Initializes Join/Leave behavior for the bottom primary button using
     * {@link DbManager#isUserInWaitlist(String, String)},
     * {@link DbManager#addUserToWaitlist(String, String)}, and
     * {@link DbManager#cancelWaitlist(String, String)}.
     */
//    private void setupJoinLeaveForEvent() {
//        // Validate required args; disable UI if missing.
//        if (isEmpty(entrantId)) {
//            btnEventsPrimary.setEnabled(false);
//            btnEventsPrimary.setText(R.string.join_waitlist);
//            Toast.makeText(requireContext(), "Missing entrantId for join/leave", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        // Disable while checking membership state
//        btnEventsPrimary.setEnabled(false);
//
//        // Resolve current membership to set correct label
//        DbManager.getInstance()
//                .isUserInWaitlist(eventId, entrantId)
//                .addOnSuccessListener(inWaitlist -> {
//                    isOnWaitlist = inWaitlist != null && inWaitlist;
//                    renderPrimaryActionLabel();
//                    btnEventsPrimary.setEnabled(true);
//                })
//                .addOnFailureListener(e -> {
//                    // Default to "Join" if check fails
//                    isOnWaitlist = false;
//                    renderPrimaryActionLabel();
//                    btnEventsPrimary.setEnabled(true);
//                });
//
//        // Toggle join/leave on click
//        btnEventsPrimary.setOnClickListener(v -> {
//            btnEventsPrimary.setEnabled(false); // prevent double taps during network call
//            if (isOnWaitlist) {
//                leaveWaitlist();
//            } else {
//                joinWaitlist();
//            }
//        });
//    }

    /** Updates the bottom primary button text based on {@link #isOnWaitlist}. */
//    private void renderPrimaryActionLabel() {
//        btnEventsPrimary.setText(isOnWaitlist ? R.string.leave_waitlist : R.string.join_waitlist);
//    }

    /** Adds the current entrant to the event's waitlist and refreshes the UI on success. */
    private void joinWaitlist() {
//        DbManager.getInstance()
//                .addUserToWaitlist(eventId, entrantId)
//                .addOnSuccessListener(v -> {
//                    isOnWaitlist = true;
//                    renderPrimaryActionLabel();
//                    Toast.makeText(requireContext(), "Joined waitlist", Toast.LENGTH_SHORT).show();
//                    btnEventsPrimary.setEnabled(true);
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(requireContext(), "Join failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                    btnEventsPrimary.setEnabled(true);
//                });
    }

    /** Removes the current entrant from the event's waitlist and refreshes the UI on success. */
    private void leaveWaitlist() {
//        DbManager.getInstance()
//                .cancelWaitlist(eventId, entrantId)
//                .addOnSuccessListener(v -> {
//                    isOnWaitlist = false;
//                    renderPrimaryActionLabel();
//                    Toast.makeText(requireContext(), "Left waitlist", Toast.LENGTH_SHORT).show();
//                    btnEventsPrimary.setEnabled(true);
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(requireContext(), "Leave failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                    btnEventsPrimary.setEnabled(true);
//                });
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

//    private static boolean isEmpty(@Nullable String s) {
//        return s == null || s.trim().isEmpty();
//    }
}