package com.example.magpie_wingman.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin screen for viewing all events and removing them (US 03.01.01).
 *
 * This fragment loads every event from Firestore and shows them in a list.
 * The admin can tap the "X" button beside an event to permanently delete it.
 */
public class AdminEventsFragment extends Fragment
        implements AdminEventAdapter.OnEventRemoveClickListener {

    private RecyclerView recyclerView;
    private AdminEventAdapter adapter;
    private final List<Event> eventList = new ArrayList<>();

    public AdminEventsFragment() {
        // Required empty public constructor
    }

    /**
     * Called when the fragment needs its UI.
     * Inflates the layout that contains the header bar and the RecyclerView.
     *
     * @param inflater           LayoutInflater used to inflate the fragment layout.
     * @param container          Optional parent view the fragment UI will attach to.
     * @param savedInstanceState Previously saved state, or {@code null} if new.
     * @return The root view for the admin events screen.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_events, container, false);
    }

    /**
     * Called after the view is created.
     * Sets up the back button, the RecyclerView, and starts loading events from Firestore.
     *
     * @param view               The fragment's root view.
     * @param savedInstanceState Previously saved state, or {@code null} if new.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Back arrow
        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp()
        );

        // Event list
        recyclerView = view.findViewById(R.id.recycler_view_admin_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new AdminEventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        // Load events from Firestore
        loadEvents();
    }

    /**
     * Loads all events from Firestore using DbManager and displays them.
     * This is a one-time fetch; if events change on the server,
     * the admin can refresh by leaving and re-entering this screen.
     */
    private void loadEvents() {
        DbManager.getInstance()
                .getAllEvents()
                .addOnSuccessListener(events -> {
                    eventList.clear();
                    eventList.addAll(events);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to load events: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    /**
     * Called when the remove button for a row is tapped.
     * Shows a confirmation dialog. If the admin confirms, deletes the event
     * and its sub-collections from Firestore and removes it from the list.
     *
     * @param position index of the event row that was clicked.
     */
    @Override
    public void onRemoveEventClicked(int position) {
        if (position < 0 || position >= eventList.size()) {
            return;
        }

        Event target = eventList.get(position);
        String eventId = target.getEventId();
        String eventName = target.getEventName() != null
                ? target.getEventName()
                : "this event";

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(),
                    "Missing event ID – cannot remove this event.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove " + eventName + "?")
                .setMessage("This will delete the event and all related data. "
                        + "This action cannot be undone.")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Remove", (d, w) -> {
                    DbManager.getInstance()
                            .deleteEvent(eventId)
                            .addOnSuccessListener(v -> {
                                eventList.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, eventList.size());
                                Toast.makeText(requireContext(),
                                        "Event removed.",
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(),
                                            "Remove failed: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show());
                })
                .show();
    }
}
