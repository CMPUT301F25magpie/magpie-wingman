package com.example.magpie_wingman.admin;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

import java.util.ArrayList;
import java.util.List;

public class AdminEventsFragment extends Fragment implements EventAdapter.OnEventListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;

    private DbManager dbManager;

    public AdminEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Get the instance of your team's DbManager
        // This assumes DbManager.init(context) was called in your "MyApp" or "MainActivity"
        dbManager = DbManager.getInstance();

        // 2. Prepare the list and RecyclerView
        eventList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_view_admin_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Create the adapter with the *empty* list (it will be filled)
        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        // 4. Load the real data from Firebase
        loadEventsFromFirebase();
    }

    /**
     * Replaces loadMockEvents().
     * Fetches all events from the "events" collection in Firestore.
     */
    private void loadEventsFromFirebase() {
        // Get the raw db instance from your manager
        FirebaseFirestore db = dbManager.getDb();

        db.collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        // Clear the list just in case
                        eventList.clear();

                        // Loop through all documents in the "events" collection
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            // Use Firestore's built-in converter to create an Event object
                            // This works because your Event.java has an empty constructor
                            Event event = doc.toObject(Event.class);
                            if (event != null) {
                                eventList.add(event);
                            }
                        }

                        // Tell the adapter that the data has changed!
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("AdminEventsFragment", "No events found.");
                    }
                })
                .addOnFailureListener(e -> {
                    // Tell the user something went wrong
                    Log.e("AdminEventsFragment", "Error loading events", e);
                    Toast.makeText(getContext(), "Error loading events", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onEventClick(int position) {
        Event clickedEvent = eventList.get(position);
        Toast.makeText(getContext(), "Admin clicked on: " + clickedEvent.getEventName(), Toast.LENGTH_SHORT).show();
    }
}