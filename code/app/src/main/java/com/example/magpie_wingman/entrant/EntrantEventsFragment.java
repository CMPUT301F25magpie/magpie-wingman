package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.example.magpie_wingman.data.model.User;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class EntrantEventsFragment extends Fragment {

    private String entrantId;
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;

    public EntrantEventsFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User currentUser = MyApp.getInstance().getCurrentUser();
        entrantId = currentUser.getUserId();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

    /**
     * Loads the current entrant's registration history.
     * For each event document, it checks the "waitlist",
     * "registrable", and "registered" subcollections for this entrant.
     * Only matching events are shown in the list.
     */
    private void fetchEvents() {
        if (entrantId == null) {
            showShortToast("No entrant set; cannot load events.");
            return;
        }

        DbManager.getInstance()
                .getDb()
                .collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();

                    int totalEvents = queryDocumentSnapshots.size();
                    if (totalEvents == 0) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    AtomicInteger processedCount = new AtomicInteger(0);

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);

                        if (event == null) {
                            // Still count this event as processed
                            if (processedCount.incrementAndGet() == totalEvents) {
                                adapter.notifyDataSetChanged();
                            }
                            continue;
                        }

                        // Ensure the eventId field is populated from the document ID
                        event.setEventId(doc.getId());

                        DocumentReference eventRef = doc.getReference();
                        checkEventMembership(eventRef, event, processedCount, totalEvents);
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace(); // goes to Logcat
                    showShortToast("Error fetching events.");
                });
    }

    /**
     * Checks whether the current entrant appears in any of the event's
     * membership subcollections: "waitlist", "registrable", or "registered".
     *
     * If the entrant is found, the event is added to the list.
     * In all cases, we mark this event as processed and eventually
     * notify the adapter when all checks are done.
     */
    private void checkEventMembership(DocumentReference eventRef,
                                      Event event,
                                      AtomicInteger processedCount,
                                      int totalEvents) {

        final String userId = entrantId;
        if (userId == null) {
            onEventCheckComplete(processedCount, totalEvents);
            return;
        }

        // 1) Check waitlist
        eventRef.collection("waitlist")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(waitlistSnap -> {
                    if (!waitlistSnap.isEmpty()) {
                        eventList.add(event);
                        onEventCheckComplete(processedCount, totalEvents);
                    } else {
                        // 2) Check registrable
                        eventRef.collection("registrable")
                                .whereEqualTo("userId", userId)
                                .get()
                                .addOnSuccessListener(registrableSnap -> {
                                    if (!registrableSnap.isEmpty()) {
                                        eventList.add(event);
                                        onEventCheckComplete(processedCount, totalEvents);
                                    } else {
                                        // 3) Check registered
                                        eventRef.collection("registered")
                                                .whereEqualTo("userId", userId)
                                                .get()
                                                .addOnSuccessListener(registeredSnap -> {
                                                    if (!registeredSnap.isEmpty()) {
                                                        eventList.add(event);
                                                    }
                                                    onEventCheckComplete(processedCount, totalEvents);
                                                })
                                                .addOnFailureListener(e -> {
                                                    e.printStackTrace();
                                                    showShortToast("Error checking registered status.");
                                                    onEventCheckComplete(processedCount, totalEvents);
                                                });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    e.printStackTrace();
                                    showShortToast("Error checking registrable status.");
                                    onEventCheckComplete(processedCount, totalEvents);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    showShortToast("Error checking waitlist status.");
                    onEventCheckComplete(processedCount, totalEvents);
                });
    }

    /**
     * Helper that increments the processed event counter and
     * notifies the adapter once all events have been checked.
     */
    private void onEventCheckComplete(AtomicInteger processedCount, int totalEvents) {
        if (processedCount.incrementAndGet() == totalEvents) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Shows a short toast if the fragment is attached to a context.
     *
     * @param message The message to display.
     */
    private void showShortToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}