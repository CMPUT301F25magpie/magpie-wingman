package com.example.magpie_wingman.organizer;

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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrganizerEventDetailsFragment extends Fragment {


    private String eventId;


    private TextView titleView, locationView, dateView, descriptionView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public OrganizerEventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        titleView = view.findViewById(R.id.text_event_title);
        locationView = view.findViewById(R.id.text_event_location);
        dateView = view.findViewById(R.id.text_event_date_time);
        descriptionView = view.findViewById(R.id.text_event_description);
        ImageButton btnBack = view.findViewById(R.id.button_back);
        Button btnWaitlist = view.findViewById(R.id.btn_waiting_list);
        Button btnSelected = view.findViewById(R.id.btn_selected_list);
        Button btnAccepted = view.findViewById(R.id.btn_accepted_list);
        Button btnCancelled = view.findViewById(R.id.btn_cancelled_list);
        Button btnLottery = view.findViewById(R.id.btn_lottery);
        Button btnNotify = view.findViewById(R.id.btn_notify);
        Button btnFinalize = view.findViewById(R.id.btn_finalize);

        //Setup Logic
        if (eventId != null) {
            loadEventDetails();
        }
        else {

            titleView.setText("Error: No Event ID Passed");
        }

        NavController navController = Navigation.findNavController(view);

        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        btnWaitlist.setOnClickListener(v -> navController.navigate(R.id.action_organizerEventDetailsFragment_to_waitingListFragment));
        btnSelected.setOnClickListener(v -> navController.navigate(R.id.action_organizerEventDetailsFragment_to_selectedEntrantsListFragment));
        btnAccepted.setOnClickListener(v -> navController.navigate(R.id.action_organizerEventDetailsFragment_to_acceptedListFragment));
        btnCancelled.setOnClickListener(v -> navController.navigate(R.id.action_organizerEventDetailsFragment_to_cancelledListFragment));
        btnLottery.setOnClickListener(v -> navController.navigate(R.id.action_organizerEventDetailsFragment_to_organizerLotteryFragment));
        btnNotify.setOnClickListener(v -> navController.navigate(R.id.action_organizerEventDetailsFragment_to_organizerNotifyFragment));
        btnFinalize.setOnClickListener(v -> navController.navigate(R.id.action_organizerEventDetailsFragment_to_organizerFinalizedListFragment));
    }

    /**
     * Helper to wire up navigation buttons safely.
     */
    private void setupNavButton(View view, Button button, int actionId) {
        if (button != null) {
            button.setOnClickListener(v -> {
                if (eventId == null) {
                    Toast.makeText(getContext(), "Cannot navigate: Missing Event ID", Toast.LENGTH_SHORT).show();
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString("eventId", eventId);
                try {
                    Navigation.findNavController(view).navigate(actionId, bundle);
                } catch (Exception e) {
                    Log.e("OrgDetails", "Nav Error", e);
                }
            });
        }
    }

    /**
     * Loads the event data from Firestore to populate the card.
     */
    private void loadEventDetails() {
        DbManager.getInstance().getDb().collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        titleView.setText(event.getEventName());
                        locationView.setText(event.getEventLocation() != null ? event.getEventLocation() : "TBD");
                        descriptionView.setText(event.getDescription());

                        if (event.getEventStartTime() != null) {
                            dateView.setText(dateFormat.format(event.getEventStartTime()));
                        } else {
                            dateView.setText("Date TBD");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("OrgDetails", "Error loading event", e));
    }
}