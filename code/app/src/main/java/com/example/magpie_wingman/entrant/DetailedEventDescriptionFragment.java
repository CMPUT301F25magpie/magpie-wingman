package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event; // Import Event model
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailedEventDescriptionFragment extends Fragment {

    // UI Components
    private TextView titleTextView;
    private TextView locationTextView;
    private TextView descriptionTextView;
    private Button signUpButton;

    // Data
    private String eventId;
    private Event currentEvent;

    // Firebase
    private DbManager dbManager;

    public DetailedEventDescriptionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            dbManager = DbManager.getInstance();
        } catch (IllegalStateException e) {
            if (getContext() != null) {
                DbManager.init(getContext().getApplicationContext());
                dbManager = DbManager.getInstance();
            }
        }

        // We only pass eventId in the navigation now
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailed_event_description, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find views
        titleTextView = view.findViewById(R.id.text_view_event_title_detail);
        locationTextView = view.findViewById(R.id.text_view_event_location_detail);
        descriptionTextView = view.findViewById(R.id.text_view_event_description_detail);
        signUpButton = view.findViewById(R.id.button_sign_up);

        signUpButton.setEnabled(false);
        signUpButton.setText("Loading...");

        // Load the event details from Firebase using the eventId
        if (eventId != null) {
            loadEventDetails();
        } else {
            Toast.makeText(getContext(), "Error: No Event ID", Toast.LENGTH_SHORT).show();
        }

        signUpButton.setOnClickListener(v -> {
            signUpForEvent();
        });
    }

    /**
     * Fetches the full event object from Firebase and updates UI
     */
    private void loadEventDetails() {
        FirebaseFirestore db = dbManager.getDb();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentEvent = doc.toObject(Event.class); // Auto-convert
                        if (currentEvent != null) {
                            // Set the data to the views using the final getters
                            titleTextView.setText(currentEvent.getEventName());
                            locationTextView.setText(currentEvent.getEventLocation());
                            descriptionTextView.setText(currentEvent.getEventDescription());

                            signUpButton.setEnabled(true);
                            signUpButton.setText("Sign Up for Event");
                        }
                    } else {
                        Toast.makeText(getContext(), "Error: Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load event details", Toast.LENGTH_SHORT).show();
                    Log.e("DetailFragment", "Failed to load event", e);
                });
    }

    /**
     * Signs the user up for the current event
     */
    private void signUpForEvent() {
        // Use the device ID as a stand-in for the real user ID
        String mockUserId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        signUpButton.setEnabled(false);
        signUpButton.setText("Signing up...");

        // Call your team's DbManager function
        dbManager.addUserToWaitlist(eventId, mockUserId)
                .addOnSuccessListener(aVoid -> {
                    signUpButton.setText("Signed Up!");
                    Toast.makeText(getContext(), "Successfully joined waitlist for " + currentEvent.getEventName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("DetailFragment", "Failed to sign up", e);
                    Toast.makeText(getContext(), "Failed to sign up. Please try again.", Toast.LENGTH_LONG).show();
                    signUpButton.setEnabled(true);
                    signUpButton.setText("Sign Up for Event");
                });
    }
}