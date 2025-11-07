package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.example.magpie_wingman.entrant.EventAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.magpie_wingman.data.model.Event; // Import our new model
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.entrant.EventAdapter;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntrantEventsFragment extends Fragment implements EventAdapter.OnEventListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;

    // Firestore
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private DbManager dbManager;

    public EntrantEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbManager = DbManager.getInstance();

        eventList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create and set the adapter (pass 'this' as the listener)
        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        loadEventsFromFirebase();
    }

    /**
     * Fetches all events from the "events" collection in Firestore.
     */
    private void loadEventsFromFirebase() {
        FirebaseFirestore db = dbManager.getDb();

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        eventList.clear();

                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                eventList.add(event);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("EntrantEventsFragment", "No events found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantEventsFragment", "Error loading events", e);
                    Toast.makeText(getContext(), "Error loading events", Toast.LENGTH_SHORT).show();
        // 3. Load the data (now from Firebase instead of mock)
        loadEventsFromDatabase();

        // 4. Create and set the adapter (pass 'this' as the listener)
        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        loadEventsFromDatabase();
    }

    /**
     * Creates data for the event list.
     * Now this comes from Firebase.
     */
    private void loadEventsFromDatabase() {
        eventList = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        Timestamp now = Timestamp.now(); //get current timestamp

        eventsRef
                .whereGreaterThanOrEqualTo("registrationEnd", now) //filter out events that have registration dates that have already passed
                .orderBy("registrationEnd")
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    eventList.clear();
                    if (snapshot != null) {
                        for (QueryDocumentSnapshot doc : snapshot) {
                            Event event = doc.toObject(Event.class);
                            eventList.add(event);
                        }
                    }

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * This method runs when an event card is clicked.
     */
    @Override
    public void onEventClick(int position) {
        Event clickedEvent = eventList.get(position);

        // Get description and check for null to prevent crash
        String eventDesc = clickedEvent.getEventDescription();
        if (eventDesc == null) {
            eventDesc = ""; // Pass an empty string instead of null
        }

        // Get location and check for null to prevent crash
        String eventLoc = clickedEvent.getEventLocation();
        if (eventLoc == null) {
            eventLoc = ""; // Pass an empty string instead of null
        }

        // Create a bundle to pass data to the details screen
        Bundle args = new Bundle();
        args.putString("eventId", clickedEvent.getEventId());
        args.putString("eventName", clickedEvent.getEventName());
        args.putString("eventDescription", eventDesc); // Use the safe variable
        args.putString("eventLocation", eventLoc); // Use the safe variable

        // Navigate using the action from nav_graph.xml
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_entrantEventsFragment_to_detailedEventDescriptionFragment, args);
    }
}