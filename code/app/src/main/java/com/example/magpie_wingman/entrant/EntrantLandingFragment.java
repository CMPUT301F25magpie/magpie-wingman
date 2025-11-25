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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.example.magpie_wingman.data.model.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entrant home screen.
 *
 * <p>This fragment is the landing page for entrant users. It shows:
 * <ul>
 *     <li>A search bar and top icons (filter, info, settings)</li>
 *     <li>A list of all events that the entrant can browse</li>
 *     <li>Bottom navigation buttons (invitations, scan QR, events, notifications)</li>
 * </ul>
 *
 * <p>The list itself does <b>not</b> perform join/leave. Instead:
 * <ul>
 *     <li>Each row has a "Join / Leave" button whose appearance reflects the user's status.</li>
 *     <li>Clicking that button opens the event details screen, where the user can actually
 *         join or leave the waitlist.</li>
 * </ul>
 */
public class EntrantLandingFragment extends Fragment {

    // -------------------------------------------------------------------------
    // UI references
    // -------------------------------------------------------------------------

    private EditText   searchBar;
    private ImageView  btnFilter;
    private ImageView  btnInfo;
    private ImageView  btnSettings;
    private RecyclerView eventsRecycler;
    private Button     btnInvitations;
    private Button     btnScanQr;
    private Button     btnEventsPrimary;
    private Button     btnNotifications;

    // Currently logged-in entrant's id (from MyApp)
    @Nullable
    private String entrantId;

    public EntrantLandingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout described in fragment_entrant_landing.xml
        return inflater.inflate(R.layout.fragment_entrant_landing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Bind views from XML
        searchBar        = v.findViewById(R.id.search_bar);
        btnFilter        = v.findViewById(R.id.btn_filter);
        btnInfo          = v.findViewById(R.id.btn_info);
        btnSettings      = v.findViewById(R.id.btn_settings);
        eventsRecycler   = v.findViewById(R.id.recycler_view_events);
        btnInvitations   = v.findViewById(R.id.btn_invitations);
        btnScanQr        = v.findViewById(R.id.btn_scan_qr);
        btnEventsPrimary = v.findViewById(R.id.btn_events);
        btnNotifications = v.findViewById(R.id.btn_notification);

        // NavController for all navigation actions
        NavController navController = Navigation.findNavController(v);

        // Set up click listeners for top and bottom bars
        setupChromeClickListeners(navController);

        // Resolve the currently logged-in entrant from MyApp
        User currentUser = MyApp.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Assuming User exposes getUserId()
            entrantId = currentUser.getUserId();
        } else {
            entrantId = null;
            Toast.makeText(requireContext(),
                    "No logged in user; event status may be unavailable.",
                    Toast.LENGTH_SHORT).show();
        }

        // Configure the events RecyclerView and load data from Firestore
        setupEventsListForEntrant(entrantId);
    }

    // -------------------------------------------------------------------------
    // UI chrome wiring (top bar + bottom bar)
    // -------------------------------------------------------------------------

    /**
     * Wires up the top icons (filter, info, settings) and bottom navigation buttons
     * so they navigate to the appropriate entrant screens.
     *
     * @param navController Navigation controller used to perform fragment transitions.
     */
    private void setupChromeClickListeners(@NonNull NavController navController) {
        // Top bar actions
        btnFilter.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_entrantEventSearchFilterFragment));

        btnInfo.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_entrantDetailsFragment));

        btnSettings.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_entrantSettingsFragment));

        // Bottom bar actions (invitations, scan QR, enrolled events, notifications)
        btnInvitations.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_entrantInvitationsFragment));

        btnScanQr.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_scanQRFragment));

        // This "Events" button goes to the screen that shows events the user is enrolled in
        btnEventsPrimary.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_entrantEventsFragment));

        btnNotifications.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_entrantNotificationsFragment));
    }

    // -------------------------------------------------------------------------
    // Event list wiring
    // -------------------------------------------------------------------------

    /**
     * Sets up the RecyclerView to display a list of events and loads data
     * from Firestore via {@link DbManager#getAllEvents()}.
     *
     * <p>The adapter is given a callback so that when the "Join / Leave" button
     * in a row is pressed, we navigate to the event details screen.</p>
     *
     * @param entrantId the current entrant's ID, used by the adapter to reflect
     *                  join/leave status; may be {@code null}.
     */
    private void setupEventsListForEntrant(@Nullable String entrantId) {
        // Use a simple vertical list layout manager
        eventsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventsRecycler.setHasFixedSize(true);

        // Backing data for the adapter
        List<Event> eventItems = new ArrayList<>();

        // If we don't know the entrant id, pass an empty string;
        // the adapter can handle this case.
        String userIdForAdapter = (entrantId != null) ? entrantId : "";

        // Adapter: clicking the join/leave button should open the details screen
        EventAdapter adapter = new EventAdapter(
                eventItems,
                userIdForAdapter,
                this::openEventDetails
        );
        eventsRecycler.setAdapter(adapter);

        // Load all events from Firestore and refresh the list when they arrive
        DbManager.getInstance()
                .getAllEvents()
                .addOnSuccessListener(events -> {
                    eventItems.clear();
                    if (events != null) {
                        eventItems.addAll(events);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(
                        requireContext(),
                        "Failed to load events: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    /**
     * Navigates to the event detail screen for the given event, bundling
     * basic event information (id, name, location, time, description) so the
     * detail fragment can render without having to re-query immediately.
     *
     * @param event the event whose details should be shown.
     */
    private void openEventDetails(@NonNull Event event) {
        Bundle args = new Bundle();
        args.putString("eventId", event.getEventId());
        String name = event.getEventName();
        args.putString("eventName", name != null ? name : "");

        String location = event.getEventLocation();
        args.putString("eventLocation", location != null ? location : "");

        Date start = event.getEventStartTime();
        if (start != null) {
            args.putLong("eventStartTime", start.getTime());
        }

        String desc = event.getDescription();
        args.putString("eventDescription", desc != null ? desc : "");

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(
                R.id.action_entrantLandingFragment3_to_detailedEventDescriptionFragment,
                args
        );
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Simple null-or-whitespace check for strings.
     *
     * @param s string to check
     * @return {@code true} if {@code s} is null, empty, or only whitespace.
     */
    private static boolean isEmpty(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }
}
