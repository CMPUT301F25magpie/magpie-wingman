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
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminEventsFragment extends Fragment implements EventAdapter.OnEventListener {

    private static final String TAG = "AdminEventsFragment";
    private RecyclerView recyclerView;
    private EventAdapter adapter;
    private List<Event> eventList;
    private DbManager dbManager;

    public AdminEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize DbManager safely
        try {
            dbManager = DbManager.getInstance();
        } catch (IllegalStateException e) {
            if (getContext() != null) {
                DbManager.init(getContext().getApplicationContext());
                dbManager = DbManager.getInstance();
            } else {
                Log.e(TAG, "Context is null, cannot initialize DbManager");
                return;
            }
        }

        // Initialize RecyclerView
        eventList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_view_admin_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(eventList, this);
        recyclerView.setAdapter(adapter);

        loadEventsFromFirebase();
    }

    /**
     * Safely parses a date field from Firestore that might be stored as Date or Timestamp
     */
    private Date parseDateSafely(DocumentSnapshot doc, String fieldName) {
        try {
            // Try to get it as a direct Date object first
            Date date = doc.getDate(fieldName);
            if (date != null) {
                return date;
            }

            // Try to get it as a Timestamp object and convert
            Timestamp timestamp = doc.getTimestamp(fieldName);
            if (timestamp != null) {
                return timestamp.toDate();
            }

            // Try to get the raw object and handle it
            Object rawValue = doc.get(fieldName);
            if (rawValue instanceof Timestamp) {
                return ((Timestamp) rawValue).toDate();
            } else if (rawValue instanceof Date) {
                return (Date) rawValue;
            }

        } catch (Exception e) {
            Log.w(TAG, "Failed to parse date field '" + fieldName + "' in document " + doc.getId(), e);
        }

        return null; // Return null if all parsing attempts fail
    }

    /**
     * Safely gets a string field with a default value
     */
    private String getStringSafely(DocumentSnapshot doc, String fieldName, String defaultValue) {
        try {
            String value = doc.getString(fieldName);
            return (value != null && !value.isEmpty()) ? value : defaultValue;
        } catch (Exception e) {
            Log.w(TAG, "Failed to get string field '" + fieldName + "'", e);
            return defaultValue;
        }
    }

    /**
     * Safely gets an integer field with a default value
     */
    private int getIntSafely(DocumentSnapshot doc, String fieldName, int defaultValue) {
        try {
            Long value = doc.getLong(fieldName);
            return (value != null) ? value.intValue() : defaultValue;
        } catch (Exception e) {
            Log.w(TAG, "Failed to get int field '" + fieldName + "'", e);
            return defaultValue;
        }
    }

    /**
     * Fetches all events for the Admin by manually parsing the document fields.
     */
    private void loadEventsFromFirebase() {
        if (dbManager == null) {
            Log.e(TAG, "DbManager is null, cannot load events");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Database not initialized", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        FirebaseFirestore db = dbManager.getDb();

        db.collection("events")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || snapshot.isEmpty()) {
                        Log.d(TAG, "No events found in Firestore");
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "No events available", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    eventList.clear();
                    int successCount = 0;
                    int failCount = 0;

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        try {
                            // Parse required fields with safe defaults
                            String eventId = getStringSafely(doc, "eventId", doc.getId());
                            String organizerId = getStringSafely(doc, "organizerId", "unknown");
                            String eventName = getStringSafely(doc, "eventName", "Unnamed Event");

                            // Parse dates safely
                            Date registrationStart = parseDateSafely(doc, "registrationStart");
                            Date registrationEnd = parseDateSafely(doc, "registrationEnd");

                            // Parse optional fields
                            String eventLocation = getStringSafely(doc, "eventLocation", "");
                            String eventDescription = getStringSafely(doc, "eventDescription", "");
                            String eventPosterURL = getStringSafely(doc, "eventPosterURL", null);
                            int eventCapacity = getIntSafely(doc, "eventCapacity", 0);

                            // Create Event object using the 9-argument constructor
                            Event event = new Event(
                                    eventId,
                                    organizerId,
                                    eventName,
                                    registrationStart,
                                    registrationEnd,
                                    eventLocation,
                                    eventDescription,
                                    eventPosterURL,
                                    eventCapacity
                            );

                            eventList.add(event);
                            successCount++;

                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse event document: " + doc.getId(), e);
                            failCount++;
                        }
                    }

                    Log.d(TAG, "Loaded " + successCount + " events successfully, " + failCount + " failed");
                    adapter.notifyDataSetChanged();

                    if (getContext() != null && eventList.isEmpty() && failCount > 0) {
                        Toast.makeText(getContext(),
                                "Some events failed to load",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(error -> {
                    Log.e(TAG, "Failed to load events from Firestore", error);
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Failed to load events: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEventClick(int position) {
        if (position < 0 || position >= eventList.size()) {
            Log.w(TAG, "Invalid event position clicked: " + position);
            return;
        }

        Event clickedEvent = eventList.get(position);
        String message = "Admin clicked: " + clickedEvent.getEventName();
        Log.d(TAG, message);

        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up to prevent memory leaks
        if (adapter != null) {
            adapter = null;
        }
        eventList = null;
        recyclerView = null;
    }
}