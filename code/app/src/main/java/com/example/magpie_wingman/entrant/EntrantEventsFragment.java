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
import com.example.magpie_wingman.data.model.Event;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.entrant.EventAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class EntrantEventsFragment extends Fragment implements EventAdapter.OnEventListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private DbManager dbManager;

    public EntrantEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ensure DbManager is initialized before use
        try {
            dbManager = DbManager.getInstance();
        } catch (IllegalStateException e) {
            if (getContext() != null) {
                DbManager.init(getContext().getApplicationContext());
                dbManager = DbManager.getInstance();
            }
        }

        eventList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        // Only call the loading function once
        loadEventsFromDatabase();
    }

    private void loadEventsFromDatabase() {
        FirebaseFirestore db = dbManager.getDb();
        Timestamp now = Timestamp.now();

        db.collection("events")
                .whereGreaterThanOrEqualTo("registrationEnd", now) // Filter for upcoming registration
                .orderBy("registrationEnd", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> { // Use listener for real-time updates
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load events", Toast.LENGTH_SHORT).show();
                        Log.e("EntrantEvents", "Snapshot listener error", error);
                        return;
                    }

                    eventList.clear();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                eventList.add(event);
                            }
                        }
                    }

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onEventClick(int position) {
        Event clickedEvent = eventList.get(position);

        String eventDesc = clickedEvent.getEventDescription();
        if (eventDesc == null) {
            eventDesc = "";
        }

        String eventLoc = clickedEvent.getEventLocation();
        if (eventLoc == null) {
            eventLoc = "";
        }

        Bundle args = new Bundle();
        args.putString("eventId", clickedEvent.getEventId());

        // Pass the event ID and other details
        args.putString("eventName", clickedEvent.getEventName());
        args.putString("eventDescription", eventDesc);
        args.putString("eventLocation", eventLoc);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_entrantEventsFragment_to_detailedEventDescriptionFragment, args);
    }
}