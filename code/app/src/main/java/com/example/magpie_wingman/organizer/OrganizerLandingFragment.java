package com.example.magpie_wingman.organizer;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.example.magpie_wingman.data.model.Event;
import com.example.magpie_wingman.data.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Organizer landing screen.
 *
 * Shows:
 *  - Search bar + settings button in the header
 *  - A list of events owned by the current organizer
 *  - A "New Event" button at the bottom
 *
 * Taps:
 *  - On the card -> OrganizerEventDetailsFragment
 *  - On the edit icon/button -> OrganizerEditEventFragment
 */
public class OrganizerLandingFragment extends Fragment {

    private EditText searchMyEvents;
    private RecyclerView recyclerView;
    private Button btnNewEvent;
    private ImageButton btnSettings;

    private final List<Event> organizerMyEvents = new ArrayList<>();
    private OrganizerEventsAdapter adapter;

    @Nullable
    private String organizerId;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault());

    public OrganizerLandingFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_landing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // views
        searchMyEvents = v.findViewById(R.id.search_my_events);
        recyclerView   = v.findViewById(R.id.recycler_organizer_events);
        btnNewEvent    = v.findViewById(R.id.button_new_event);
        btnSettings    = v.findViewById(R.id.button_settings);

        NavController navController = Navigation.findNavController(v);

        // current organizer
        User currentUser = MyApp.getInstance().getCurrentUser();
        if (currentUser != null) {
            organizerId = currentUser.getUserId();
        }

        if (organizerId == null) {
            Toast.makeText(requireContext(),
                    "No organizer account found; please log in again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Set up RecyclerView + adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new OrganizerEventsAdapter(
                organizerMyEvents,
                this::openEventDetails,
                this::openEventEditor
        );
        recyclerView.setAdapter(adapter);

        // New Event + Settings navigation
        btnNewEvent.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_organizerLandingFragment2_to_organizerNewEventFragment));

        btnSettings.setOnClickListener(x ->
                navController.navigate(
                        R.id.action_organizerLandingFragment2_to_organizerSettingsFragment));

        loadOrganizerEvents(organizerId);
    }

    // -------------------------------------------------------------------------
    // Firestore load
    // -------------------------------------------------------------------------

    /** Loads events where organizerId == current user's id and sorts them by event date. */
    private void loadOrganizerEvents(@NonNull String organizerId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("events");

        eventsRef
                .whereEqualTo("organizerId", organizerId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(),
                                "Failed to load your events",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    organizerMyEvents.clear();

                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Event event = doc.toObject(Event.class);

                            // Make sure we have the Firestore doc id in the model
                            event.setEventId(doc.getId());

                            organizerMyEvents.add(event);
                        }
                    }

                    // Sort by event date
                    Collections.sort(organizerMyEvents, new Comparator<Event>() {
                        @Override
                        public int compare(Event e1, Event e2) {
                            Date d1 = e1.getEventStartTime();
                            Date d2 = e2.getEventStartTime();
                            if (d1 == null && d2 == null) return 0;
                            if (d1 == null) return 1;
                            if (d2 == null) return -1;
                            return d1.compareTo(d2);
                        }
                    });

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Navigation helpers
    // -------------------------------------------------------------------------

    /** Card click -> open OrganizerEventDetailsFragment. */
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

        String posterUrl = event.getEventPosterURL();
        args.putString("eventPosterURL", posterUrl);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(
                R.id.action_organizerLandingFragment2_to_organizerEventDetailsFragment,
                args
        );
    }

    /** Edit button click -> open OrganizerEditEventFragment. */
    private void openEventEditor(@NonNull Event event) {
        Bundle args = new Bundle();
        args.putString("eventId", event.getEventId());

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(
                R.id.action_organizerLandingFragment2_to_organizerEditEventFragment,
                args
        );
    }

    // -------------------------------------------------------------------------
    // Adapter
    // -------------------------------------------------------------------------

    private static class OrganizerEventsAdapter
            extends RecyclerView.Adapter<OrganizerEventsAdapter.EventViewHolder> {

        interface OnEventClickListener {
            void onEventClick(@NonNull Event event);
        }

        private final List<Event> events;
        private final OnEventClickListener cardClickListener;
        private final OnEventClickListener editClickListener;
        private final SimpleDateFormat dateFormat =
                new SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault());

        OrganizerEventsAdapter(@NonNull List<Event> events,
                               @NonNull OnEventClickListener cardClickListener,
                               @NonNull OnEventClickListener editClickListener) {
            this.events = events;
            this.cardClickListener = cardClickListener;
            this.editClickListener = editClickListener;
        }

        @NonNull
        @Override
        public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_organizer_event_item, parent, false);
            return new EventViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
            Event event = events.get(position);

            String title = event.getEventName();
            holder.titleText.setText(!TextUtils.isEmpty(title) ? title : "Untitled event");

            String location = event.getEventLocation();
            holder.locationText.setText(
                    !TextUtils.isEmpty(location) ? location : "Location TBD"
            );

            Date start = event.getEventStartTime();
            if (start != null) {
                holder.dateText.setText(dateFormat.format(start));
            } else {
                holder.dateText.setText("Date TBD");
            }

            // Card click -> details
            holder.itemView.setOnClickListener(v -> cardClickListener.onEventClick(event));

            // Edit button click -> editor
            holder.editButton.setOnClickListener(v -> editClickListener.onEventClick(event));
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        static class EventViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView locationText;
            TextView dateText;
            ImageButton   editButton;

            EventViewHolder(@NonNull View itemView) {
                super(itemView);
                titleText    = itemView.findViewById(R.id.text_event_title);
                locationText = itemView.findViewById(R.id.text_event_location);
                dateText     = itemView.findViewById(R.id.text_event_date);
                editButton   = itemView.findViewById(R.id.button_edit_event);
            }
        }
    }
}
