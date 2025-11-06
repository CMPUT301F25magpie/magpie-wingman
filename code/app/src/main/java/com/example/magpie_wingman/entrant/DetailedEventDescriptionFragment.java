package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magpie_wingman.R;

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

    public DetailedEventDescriptionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Get the arguments passed from the list fragment
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detailed_event_description, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 2. Find all the views from the layout
        titleTextView = view.findViewById(R.id.text_view_event_title_detail);
        locationTextView = view.findViewById(R.id.text_view_event_location_detail);
        descriptionTextView = view.findViewById(R.id.text_view_event_description_detail);
        signUpButton = view.findViewById(R.id.button_sign_up);

        // 3. Set the data to the views
        titleTextView.setText(eventName);
        locationTextView.setText(eventLocation);
        descriptionTextView.setText(eventDescription);

        // (In a real app, you might check if the user is *already* signed up
        // and change the button text, e.g., "View QR Code")

        // 4. Set the click listener for the "Sign Up" button
        signUpButton.setOnClickListener(v -> {
            // This is where you would call your DatabaseManager
            // to sign the user up for this 'eventId'.

            // For now, just show a Toast.
            Toast.makeText(getContext(), "Signing up for " + eventName, Toast.LENGTH_SHORT).show();

            // You might also disable the button after click
            signUpButton.setEnabled(false);
            signUpButton.setText("Signed Up!");
        });
    }
}