package com.example.magpie_wingman.entrant;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Entrant home screen.
 *
 * <p>This fragment is the landing page for entrant users. It shows:
 * <ul>
 * <li>A search bar and top icons (filter, info, settings)</li>
 * <li>A list of all events that the entrant can browse</li>
 * <li>Bottom navigation buttons (invitations, scan QR, events, notifications)</li>
 * </ul>
 *
 * <p>The list itself does <b>not</b> perform join/leave. Instead:
 * <ul>
 * <li>Each row has a "Join / Leave" button whose appearance reflects the user's status.</li>
 * <li>Clicking that button opens the event details screen, where the user can actually
 * join or leave the waitlist.</li>
 * </ul>
 */
public class EntrantLandingFragment extends Fragment {

    // -------------------------------------------------------------------------
    // UI references
    // -------------------------------------------------------------------------

    private EditText   searchBar; // Changed from TextView to EditText for US 01.01.04
    private ImageView  btnFilter;
    private ImageView  btnInfo;
    private ImageView  btnSettings;
    private RecyclerView eventsRecycler;
    private Button     btnInvitations;
    private Button     btnScanQr;
    private Button     btnEventsPrimary;
    private Button     btnNotifications;

    // Two lists: Master (All Data from DB) vs Display (Filtered View)
    private final List<Event> masterEventList = new ArrayList<>();
    private final List<Event> eventList = new ArrayList<>();

    private EventAdapter adapter;

    // Currently logged-in entrant's id (from MyApp)
    private String entrantId;

    // Filter State
    private boolean filterAvailableOnly = false;

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
        entrantId = currentUser.getUserId();


        // Configure the events RecyclerView and load data from Firestore
        setupEventsListForEntrant(entrantId);

        // --- Search & Filter Logic (US 01.01.04) ---
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnFilter.setOnClickListener(x -> showFilterDialog());
    }

    // -------------------------------------------------------------------------
    // UI chrome wiring (top bar + bottom bar)
    // -------------------------------------------------------------------------

    private void setupChromeClickListeners(@NonNull NavController navController) {
        btnInfo.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_entrantDetailsFragment));

        btnSettings.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_entrantSettingsFragment));

        // Bottom bar actions
        btnInvitations.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_entrantInvitationsFragment));

        btnScanQr.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_entrantLandingFragment3_to_scanQRFragment));

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

    private void setupEventsListForEntrant(@Nullable String entrantId) {
        // Use a simple vertical list layout manager
        eventsRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventsRecycler.setHasFixedSize(true);
        eventList.clear();

        String userIdForAdapter = (entrantId != null) ? entrantId : "test_user_id";

        adapter = new EventAdapter(
                eventList,
                userIdForAdapter,
                this::openEventDetails
        );
        eventsRecycler.setAdapter(adapter);

        loadEvents();
    }

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
        String picUrl = event.getEventPosterURL();
        args.putString("eventPosterURL", picUrl != null ? picUrl : "");

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(
                R.id.action_entrantLandingFragment3_to_detailedEventDescriptionFragment,
                args
        );
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void loadEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("events");

        Timestamp queryNow = Timestamp.now();

        eventsRef
                .whereGreaterThanOrEqualTo("registrationEnd", queryNow)
                .orderBy("registrationEnd")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(),
                                "Failed to load events", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    masterEventList.clear();
                    Timestamp now = Timestamp.now();

                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Timestamp regStart = doc.getTimestamp("registrationStart");
                            if (regStart != null && regStart.compareTo(now) > 0) {
                                continue; // skip
                            }

                            Event event = doc.toObject(Event.class);
                            masterEventList.add(event);
                        }
                    }

                    // Now rebuild the filtered list & refresh the adapter
                    applyFilters();
                });
    }

    /**
     * Filters the master list based on search text AND availability toggle.
     * Populates 'eventList' and notifies adapter.
     */
    private void applyFilters() {
        eventList.clear();
        String query = "";
        if (searchBar != null && searchBar.getText() != null) {
            query = searchBar.getText().toString().toLowerCase().trim();
        }

        for (Event event : masterEventList) {
            boolean matchesSearch = false;
            boolean matchesFilter = true;

            // 1. Text Search
            if (query.isEmpty()) {
                matchesSearch = true;
            } else {
                if (event.getEventName() != null && event.getEventName().toLowerCase().contains(query)) {
                    matchesSearch = true;
                } else if (event.getDescription() != null && event.getDescription().toLowerCase().contains(query)) {
                    matchesSearch = true;
                }
            }

            // 2. Availability Filter (Limit)
            if (filterAvailableOnly) {
                // If limit > 0 and waitlist >= limit, hide it
                if (event.getWaitingListLimit() > 0 && event.getWaitlistCount() >= event.getWaitingListLimit()) {
                    matchesFilter = false;
                }
            }

            if (matchesSearch && matchesFilter) {
                eventList.add(event);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showFilterDialog() {
        String[] options = {"Show Only Available Events (Not Full)"};
        boolean[] checkedItems = {filterAvailableOnly};

        new AlertDialog.Builder(getContext())
                .setTitle("Filter Events")
                .setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
                    if (which == 0) filterAvailableOnly = isChecked;
                })
                .setPositiveButton("Apply", (dialog, which) -> {
                    applyFilters();
                    String status = filterAvailableOnly ? "Showing available only" : "Showing all events";
                    Toast.makeText(getContext(), status, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}