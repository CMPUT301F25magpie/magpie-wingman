package com.example.magpie_wingman.admin;

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
import com.example.magpie_wingman.data.model.Event;
import com.example.magpie_wingman.entrant.EventAdapter; // Using the updated adapter

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdminEventsFragment extends Fragment implements EventAdapter.OnEventListener {

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;

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

        recyclerView = view.findViewById(R.id.recycler_view_admin_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadMockEvents();

        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Creates mock data using the correct Event constructor.
     */
    private void loadMockEvents() {
        eventList = new ArrayList<>();

        // Mock timestamps (long)
        long now = System.currentTimeMillis();
        long inOneWeek = now + TimeUnit.DAYS.toMillis(7);
        long inTwoWeeks = now + TimeUnit.DAYS.toMillis(14);
        long inThreeWeeks = now + TimeUnit.DAYS.toMillis(21);
        long inFourWeeks = now + TimeUnit.DAYS.toMillis(28);

        // Call the correct 9-argument constructor
        // Event(eventId, organizerId, eventName, eventStartTime, eventEndTime, eventLocation, eventDescription, eventPosterURL, eventCapacity)
        eventList.add(new Event("e1", "org1", "Tech Summit", inOneWeek, inOneWeek + 3600000, "Convention Centre", "The biggest tech summit...", null, 100));
        eventList.add(new Event("e2", "org2", "Music Fest", inTwoWeeks, inTwoWeeks + 3600000, "Hawrelak Park", "Live bands and food trucks...", null, 500));
        eventList.add(new Event("e3", "org1", "Pitch Night", inThreeWeeks, inThreeWeeks + 3600000, "Startup Edmonton", "See the latest local startups...", null, 50));
        eventList.add(new Event("e4", "org3", "Winter Market", inFourWeeks, inFourWeeks + 3600000, "Ice District", "Holiday crafts and food...", null, 200));
    }

    @Override
    public void onEventClick(int position) {
        Event clickedEvent = eventList.get(position);
        Toast.makeText(getContext(), "Admin clicked on: " + clickedEvent.getEventName(), Toast.LENGTH_SHORT).show();
    }
}