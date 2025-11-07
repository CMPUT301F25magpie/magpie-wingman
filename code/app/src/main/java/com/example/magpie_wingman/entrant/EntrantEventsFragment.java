package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast; // Import Toast

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Event; // Import our new model

import java.util.ArrayList;
import java.util.List;

// Implement the click listener interface
public class EntrantEventsFragment extends Fragment implements EventAdapter.OnEventListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;

    public EntrantEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_entrant_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Find the RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view_events);

        // 2. Set its layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 3. Load the mock data
        loadMockEvents();

        // 4. Create and set the adapter (pass 'this' as the listener)
        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Creates mock data for the event list.
     * Later, this will come from Firebase.
     */
    private void loadMockEvents() {
        eventList = new ArrayList<>();
        // UPDATED to include description
        // eventList.add(new Event("e1", "Tech Summit", "Nov 15", "Convention Centre", "The biggest tech summit..."));
        // eventList.add(new Event("e2", "Music Fest", "Nov 22", "Hawrelak Park", "Live bands and food trucks..."));
        // eventList.add(new Event("e3", "Pitch Night", "Nov 28", "Startup Edmonton", "See the latest local startups..."));
    }

    /**
     * This method runs when an event card is clicked.
     */
    @Override
    public void onEventClick(int position) {
        Event clickedEvent = eventList.get(position);

        // For now, just show a Toast.
        // Later, this will navigate to the Event Details screen.
        Toast.makeText(getContext(), "Clicked on: " + clickedEvent.getEventName(), Toast.LENGTH_SHORT).show();

        // Example of navigation (from your nav_graph):
        // NavHostFragment.findNavController(this)
        //     .navigate(R.id.action_entrantEventsFragment_to_detailedEventDescriptionFragment);
    }
}