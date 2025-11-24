package com.example.magpie_wingman.entrant;

import android.os.Bundle;
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
 * Dual-mode behavior:
 *  - List mode (no eventId): shows search + RecyclerView; bottom buttons act as navigation.
 *  - Event mode (eventId passed): reuses the "Events" button as Join/Leave waitlist for that event.
 *
 * Arguments:
 *  - arg_event_id   (optional) – if present, enables Join/Leave behavior.
 *  - arg_entrant_id (required for Join/Leave) – current entrant user id.
 *
 * Uses DbManager methods:
 *  - {@link DbManager#isUserInWaitlist(String, String)}
 *  - {@link DbManager#addUserToWaitlist(String, String)}
 *  - {@link DbManager#cancelWaitlist(String, String)}
 */
public class EntrantLandingFragment extends Fragment {

    private static final String ARG_EVENT_ID   = "arg_event_id";
    private static final String ARG_ENTRANT_ID = "arg_entrant_id";

    // Arguments
    @Nullable
    private String eventId;
    @Nullable
    private String entrantId;

    // UI from fragment_entrant_landing.xml
    private EditText   searchBar;
    private ImageView  btnFilter, btnInfo, btnSettings;
    private RecyclerView eventsRecycler;
    private Button     btnInvitations, btnScanQr, btnEventsPrimary, btnNotifications;

    // Local state for Join/Leave
    private boolean isOnWaitlist = false;

    public EntrantLandingFragment() {
        // Required empty public constructor
    }

    /** Optional factory if you ever want to create this fragment manually. */
    public static EntrantLandingFragment newInstance(@NonNull String eventId,
                                                     @NonNull String entrantId) {
        EntrantLandingFragment fragment = new EntrantLandingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_ENTRANT_ID, entrantId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId   = args.getString(ARG_EVENT_ID);
            entrantId = args.getString(ARG_ENTRANT_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        btnFilter.setOnClickListener(x ->
                navController.navigate(R.id.action_entrantLandingFragment3_to_entrantEventSearchFilterFragment));

        btnInfo.setOnClickListener(x ->
                navController.navigate(R.id.action_entrantLandingFragment3_to_entrantDetailsFragment));

        btnSettings.setOnClickListener(x ->
                navController.navigate(R.id.action_entrantLandingFragment3_to_entrantSettingsFragment));

        // --- Bottom bar actions (shared in both modes) ---
        btnInvitations.setOnClickListener(x ->
                navController.navigate(R.id.action_entrantLandingFragment3_to_entrantInvitationsFragment));

        btnScanQr.setOnClickListener(x ->
                navController.navigate(R.id.action_entrantLandingFragment3_to_scanQRFragment));

        btnNotifications.setOnClickListener(x ->
                navController.navigate(R.id.action_entrantLandingFragment3_to_entrantNotificationsFragment));

        // --- List mode vs Event mode wiring for the primary bottom button ---
        if (isEmpty(eventId)) {
            // LIST MODE: "Events" button just goes to the events list
            btnEventsPrimary.setEnabled(true);
            btnEventsPrimary.setText(R.string.events); // make sure label is Events
            btnEventsPrimary.setOnClickListener(x ->
                    navController.navigate(R.id.action_entrantLandingFragment3_to_entrantEventsFragment));

            // TODO: set up RecyclerView adapter & data source here, e.g.:
            // setupEventsListForEntrant(entrantId);
        } else {
            // EVENT MODE: repurpose primary button as Join/Leave for this eventId
            setupJoinLeaveForEvent();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Join / Leave waitlist using DbManager
    // ---------------------------------------------------------------------------------------------

    /**
     * Configures {@link #btnEventsPrimary} to act as a Join/Leave waitlist button
     * for {@link #eventId}, using DbManager's waitlist methods.
     */
    private void setupJoinLeaveForEvent() {
        // Validate required args; disable UI if missing entrantId
        if (isEmpty(entrantId)) {
            btnEventsPrimary.setEnabled(false);
            btnEventsPrimary.setText(R.string.join_waitlist);
            Toast.makeText(requireContext(),
                    "Missing entrantId for join/leave", Toast.LENGTH_LONG).show();
            return;
        }

        // Disable while checking membership state
        btnEventsPrimary.setEnabled(false);

        // Resolve current membership to set correct label
        DbManager.getInstance()
                .isUserInWaitlist(eventId, entrantId)
                .addOnSuccessListener(inWaitlist -> {
                    // inWaitlist is Boolean from Firestore; default to false if null
                    isOnWaitlist = inWaitlist != null && inWaitlist;
                    renderPrimaryActionLabel();
                    btnEventsPrimary.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    // Default to "Join" in case of error checking status
                    isOnWaitlist = false;
                    renderPrimaryActionLabel();
                    btnEventsPrimary.setEnabled(true);
                    Toast.makeText(requireContext(),
                            "Failed to check waitlist: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });

        // Toggle join/leave on click
        btnEventsPrimary.setOnClickListener(v -> {
            btnEventsPrimary.setEnabled(false); // prevent double taps
            if (isOnWaitlist) {
                leaveWaitlist();
            } else {
                joinWaitlist();
            }
        });
    }

    /** Updates the bottom primary button text based on {@link #isOnWaitlist}. */
    private void renderPrimaryActionLabel() {
        btnEventsPrimary.setText(
                isOnWaitlist ? R.string.leave_waitlist : R.string.join_waitlist
        );
    }

    /** Adds the current entrant to the event's waitlist and refreshes the UI on success. */
    private void joinWaitlist() {
        DbManager.getInstance()
                .addUserToWaitlist(eventId, entrantId)
                .addOnSuccessListener(v -> {
                    isOnWaitlist = true;
                    renderPrimaryActionLabel();
                    Toast.makeText(requireContext(),
                            "Joined waitlist", Toast.LENGTH_SHORT).show();
                    btnEventsPrimary.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Join failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnEventsPrimary.setEnabled(true);
                });
    }

    /** Removes the current entrant from the event's waitlist and refreshes the UI on success. */
    private void leaveWaitlist() {
        DbManager.getInstance()
                .cancelWaitlist(eventId, entrantId)
                .addOnSuccessListener(v -> {
                    isOnWaitlist = false;
                    renderPrimaryActionLabel();
                    Toast.makeText(requireContext(),
                            "Left waitlist", Toast.LENGTH_SHORT).show();
                    btnEventsPrimary.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(),
                            "Leave failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    btnEventsPrimary.setEnabled(true);
                });
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    private static boolean isEmpty(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }
}
