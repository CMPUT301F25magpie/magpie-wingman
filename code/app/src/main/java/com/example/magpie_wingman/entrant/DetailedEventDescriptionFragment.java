package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DetailedEventDescriptionFragment extends Fragment {

    private String eventId;
    private String entrantId;

    private TextView title, location, date, description, waitlistCount;
    private Button joinButton;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault());

    public DetailedEventDescriptionFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            entrantId = getArguments().getString("entrantId");
        }
        if (entrantId == null) entrantId = "test_user_id";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailed_event_description, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = view.findViewById(R.id.text_event_title);
        location = view.findViewById(R.id.text_event_location);
        date = view.findViewById(R.id.text_event_date);
        description = view.findViewById(R.id.text_event_description);
        waitlistCount = view.findViewById(R.id.text_waiting_list);
        joinButton = view.findViewById(R.id.button_join_waitlist);
        ImageButton backBtn = view.findViewById(R.id.button_back);

        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        loadEventDetails();
        updateButtonState();

        joinButton.setOnClickListener(v -> toggleJoinStatus());
    }

    private void loadEventDetails() {
        if (eventId == null) return;
        DbManager.getInstance().getDb().collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        title.setText(event.getEventName());
                        location.setText(event.getEventLocation());
                        description.setText(event.getDescription());
                        waitlistCount.setText("Waiting List: " + event.getWaitlistCount());
                        if (event.getEventStartTime() != null) {
                            date.setText(dateFormat.format(event.getEventStartTime()));
                        }
                    }
                });
    }

    private void updateButtonState() {
        joinButton.setEnabled(false);
        DbManager.getInstance().isUserInWaitlist(eventId, entrantId)
                .addOnSuccessListener(isJoined -> {
                    joinButton.setEnabled(true);
                    if (isJoined) {
                        joinButton.setText("Leave Waiting List");
                        joinButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light, null));
                    } else {
                        joinButton.setText("Join Waiting List");
                        joinButton.setBackgroundResource(R.drawable.green_button_bg);
                    }
                });
    }

    private void toggleJoinStatus() {
        joinButton.setEnabled(false);
        DbManager.getInstance().isUserInWaitlist(eventId, entrantId)
                .addOnSuccessListener(isJoined -> {
                    if (isJoined) {
                        DbManager.getInstance().cancelWaitlist(eventId, entrantId)
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(getContext(), "Left Waitlist", Toast.LENGTH_SHORT).show();
                                    updateButtonState();
                                });
                    } else {
                        DbManager.getInstance().addUserToWaitlist(eventId, entrantId)
                                .addOnSuccessListener(v -> {
                                    Toast.makeText(getContext(), "Joined Waitlist", Toast.LENGTH_SHORT).show();
                                    updateButtonState();
                                });
                    }
                });
    }
}