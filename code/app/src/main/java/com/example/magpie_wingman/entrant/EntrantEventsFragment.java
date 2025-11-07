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
import com.example.magpie_wingman.data.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EntrantEventsFragment extends Fragment {

    private static final String ARG_ENTRANT_ID = "arg_entrant_id";

    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList = new ArrayList<>();
    private String entrantId = ""; // supply from args or auth

    public static EntrantEventsFragment newInstance(@NonNull String entrantId) {
        Bundle b = new Bundle();
        b.putString(ARG_ENTRANT_ID, entrantId);
        EntrantEventsFragment f = new EntrantEventsFragment();
        f.setArguments(b);
        return f;
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            entrantId = getArguments().getString(ARG_ENTRANT_ID, "");
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

        recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new EventAdapter(
                eventList,
                entrantId,
                event -> Toast.makeText(getContext(),
                        "Clicked on: " + (event.getEventName() == null ? "" : event.getEventName()),
                        Toast.LENGTH_SHORT).show()
        );
        recyclerView.setAdapter(adapter);

        // TODO: load events into eventList, then adapter.notifyDataSetChanged();
    }
}
