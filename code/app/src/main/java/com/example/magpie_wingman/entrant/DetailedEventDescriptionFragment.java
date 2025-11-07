package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.provider.Settings; // Import this
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;

public class DetailedEventDescriptionFragment extends Fragment {

    // UI Components
    private TextView titleTextView;
    private TextView locationTextView;
    private TextView descriptionTextView;
    private Button signUpButton;

    // Data
    private String eventId;
    private String eventName;
    private String eventDescription;
    private String eventLocation;

    // Firebase
    private DbManager dbManager;

    public DetailedEventDescriptionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the DbManager instance
        dbManager = DbManager.getInstance();

        // Get the event data passed from the list
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            eventName = getArguments().getString("eventName");
            eventDescription = getArguments().getString("eventDescription");
            eventLocation = getArguments().getString("eventLocation");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        // Set the event data to the views
        titleTextView.setText(eventName);
        locationTextView.setText(eventLocation);
        descriptionTextView.setText(eventDescription);

        // Set the click listener for the "Sign Up" button
        signUpButton.setOnClickListener(v -> {
            signUpForEvent();
        });
    }

    /**
     * Signs the user up for the current event.
     */
    private void signUpForEvent() {
        if (eventId == null) {
            Toast.makeText(getContext(), "Error: Event ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the device ID as a mock/stand-in for the userId
        String mockUserId = Settings.Secure.getString(
                getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (mockUserId == null) {
            Toast.makeText(getContext(), "Error: Could not get user ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable the button to prevent multiple clicks
        signUpButton.setEnabled(false);
        signUpButton.setText("Signing up...");

        // Call your team's DbManager function
        dbManager.addUserToWaitlist(eventId, mockUserId)
                .addOnSuccessListener(aVoid -> {
                    // Success!
                    signUpButton.setText("Signed Up!");
                    Toast.makeText(getContext(), "Successfully signed up for " + eventName, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Failure
                    Log.e("DetailedEventDesc", "Failed to sign up", e);
                    Toast.makeText(getContext(), "Failed to sign up. Please try again.", Toast.LENGTH_LONG).show();
                    signUpButton.setEnabled(true);
                    signUpButton.setText("Sign Up for Event");
                });
    }
}