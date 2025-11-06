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
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Event; // Your team's model
import com.example.magpie_wingman.entrant.EventAdapter; // The updated adapter

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit; // For creating mock timestamps

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

        recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadMockEvents();

        // Pass 'this' as the listener
        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Creates mock data for the event list using the team's correct constructor.
     */
    private void loadMockEvents() {
        eventList = new ArrayList<>();

        // Mock timestamps (long)
        long now = System.currentTimeMillis();
        long inOneWeek = now + TimeUnit.DAYS.toMillis(7);
        long inTwoWeeks = now + TimeUnit.DAYS.toMillis(14);
        long inThreeWeeks = now + TimeUnit.DAYS.toMillis(21);

        // Call the correct 9-argument constructor
        // Event(eventId, organizerId, eventName, eventStartTime, eventEndTime, eventLocation, eventDescription, eventPosterURL, eventCapacity)
        eventList.add(new Event("e1", "org1", "Tech Summit", inOneWeek, inOneWeek + 3600000, "Convention Centre", "The biggest tech summit...", null, 100));
        eventList.add(new Event("e2", "org2", "Music Fest", inTwoWeeks, inTwoWeeks + 3600000, "Hawrelak Park", "Live bands and food trucks...", null, 500));
        eventList.add(new Event("e3", "org1", "Pitch Night", inThreeWeeks, inThreeWeeks + 3600000, "Startup Edmonton", "See the latest local startups...", null, 50));
    }

    /**
     * This method runs when an event card is clicked.
     */
    @Override
    public void onEventClick(int position) {
        Event clickedEvent = eventList.get(position);

        Toast.makeText(getContext(), "Clicked on: " + clickedEvent.getEventName(), Toast.LENGTH_SHORT).show();

        // Later, this will navigate to the Event Details screen.
    }
}