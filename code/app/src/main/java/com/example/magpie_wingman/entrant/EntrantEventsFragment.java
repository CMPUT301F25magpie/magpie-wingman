package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EntrantEventsFragment extends Fragment {

    private static final String ARG_ENTRANT_ID = "entrantId";
    private String entrantId;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;

    public EntrantEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Ensure you are passing the entrantID via args or getting from a singleton
        // For now, defaulting to arguments
        if (getArguments() != null) {
            entrantId = getArguments().getString(ARG_ENTRANT_ID);
        }
        if (entrantId == null) {
            // Fallback for testing part 3 without full login flow
            entrantId = "test_user_id";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventList = new ArrayList<>();


        adapter = new EventAdapter(eventList, entrantId, event -> {

            Bundle bundle = new Bundle();
            bundle.putString("eventId", event.getEventId());
            bundle.putString("entrantId", entrantId);
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_entrantEventsFragment_to_detailedEventDescriptionFragment, bundle);
        });

        recyclerView.setAdapter(adapter);

        fetchEvents();
    }

    private void fetchEvents() {
        DbManager.getInstance().getDb().collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            eventList.add(event);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantEvents", "Error fetching events", e);
                });
    }
}