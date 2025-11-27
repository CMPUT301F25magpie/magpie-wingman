package com.example.magpie_wingman.entrant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.example.magpie_wingman.data.model.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment that displays the full details of a single event for entrant users.
 *
 * <p>This screen shows the event title, location, time, description, and the
 * current waitlist size. It also lets the entrant join or leave the waitlist
 * for this event using the "Join / Leave" button.</p>
 */
public class DetailedEventDescriptionFragment extends Fragment {

    private static final String ARG_EVENT_ID          = "eventId";
    private static final String ARG_EVENT_NAME        = "eventName";
    private static final String ARG_EVENT_LOCATION    = "eventLocation";
    private static final String ARG_EVENT_START_TIME  = "eventStartTime";
    private static final String ARG_EVENT_DESCRIPTION = "eventDescription";
    private static final String ARG_EVENT_POSTER_URL  = "eventPosterURL";

    private String eventId;
    private String eventPosterUrl;
    private String entrantId;

    private String eventName;
    private String eventLocation;
    private long   eventStartTimeMillis = -1;
    private String eventDescription;

    private TextView titleText, locationText, dateText, descriptionText, textWaitingList;
    private ImageView posterImage;
    private Button   joinButton;

    private boolean isOnWaitlist = false;
    private int     waitlistCount = 0;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd - hh:mm a", Locale.getDefault());

    public DetailedEventDescriptionFragment() { }

    public static DetailedEventDescriptionFragment newInstance(String eventId, String eventName, String eventLocation, long eventStartTime, String description, @Nullable String posterUrl) {
        DetailedEventDescriptionFragment fragment = new DetailedEventDescriptionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putString(ARG_EVENT_LOCATION, eventLocation);
        args.putLong(ARG_EVENT_START_TIME, eventStartTime);
        args.putString(ARG_EVENT_DESCRIPTION, description);
        args.putString(ARG_EVENT_POSTER_URL, posterUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            eventId              = args.getString(ARG_EVENT_ID);
            eventName            = args.getString(ARG_EVENT_NAME);
            eventLocation        = args.getString(ARG_EVENT_LOCATION);
            eventStartTimeMillis = args.getLong(ARG_EVENT_START_TIME, -1L);
            eventDescription     = args.getString(ARG_EVENT_DESCRIPTION);
            eventPosterUrl       = args.getString(ARG_EVENT_POSTER_URL);
        }

        User current = MyApp.getInstance().getCurrentUser();
        if (current != null) {
            entrantId = current.getUserId();
        } else {
            entrantId = "test_user_id";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailed_event_description, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ImageButton backButton = v.findViewById(R.id.button_back);
        posterImage = v.findViewById(R.id.image_event_poster);
        titleText = v.findViewById(R.id.text_event_title);
        locationText = v.findViewById(R.id.text_event_location);
        dateText = v.findViewById(R.id.text_event_date);
        textWaitingList = v.findViewById(R.id.text_waiting_list);
        descriptionText = v.findViewById(R.id.text_event_description);
        joinButton = v.findViewById(R.id.button_join_waitlist);

        if (!TextUtils.isEmpty(eventPosterUrl)) {
            Glide.with(this).load(eventPosterUrl).placeholder(R.drawable.ic_music).error(R.drawable.ic_music).into(posterImage);
        } else {
            posterImage.setImageResource(R.drawable.ic_music);
        }

        titleText.setText(!TextUtils.isEmpty(eventName) ? eventName : "Event");
        locationText.setText(!TextUtils.isEmpty(eventLocation) ? eventLocation : "Location TBD");
        if (eventStartTimeMillis > 0L) {
            dateText.setText(dateFormat.format(new Date(eventStartTimeMillis)));
        } else {
            dateText.setText("Date TBD");
        }
        if (!TextUtils.isEmpty(eventDescription)) descriptionText.setText(eventDescription);

        backButton.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });

        if (eventId == null || entrantId == null) {
            joinButton.setEnabled(false);
            return;
        }

        // --- FIX FOR QR SCANNER LOADING ---
        loadEventDetailsFromFirestore();
        loadWaitlistState();

        joinButton.setOnClickListener(v1 -> toggleJoinLeave());
    }

    private void loadEventDetailsFromFirestore() {
        if (eventId == null) return;
        DbManager.getInstance().getDb().collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        if (titleText != null) titleText.setText(event.getEventName());
                        if (locationText != null) locationText.setText(event.getEventLocation());
                        if (descriptionText != null) descriptionText.setText(event.getDescription());
                        if (event.getEventStartTime() != null && dateText != null) {
                            dateText.setText(dateFormat.format(event.getEventStartTime()));
                        }
                        if (!TextUtils.isEmpty(event.getEventPosterURL()) && posterImage != null && getContext() != null) {
                            Glide.with(getContext()).load(event.getEventPosterURL()).placeholder(R.drawable.ic_music).into(posterImage);
                        }
                    }
                });
    }

    private void loadWaitlistState() {
        DbManager.getInstance().isUserInWaitlist(eventId, entrantId).addOnSuccessListener(isIn -> {
            isOnWaitlist = Boolean.TRUE.equals(isIn);
            renderJoinButton();
        });
        DbManager.getInstance().getEventWaitlist(eventId).addOnSuccessListener(users -> {
            waitlistCount = (users != null) ? users.size() : 0;
            renderWaitlistCount();
        });
    }

    private void toggleJoinLeave() {
        joinButton.setEnabled(false);
        if (isOnWaitlist) {
            DbManager.getInstance().cancelWaitlist(eventId, entrantId).addOnSuccessListener(v -> {
                isOnWaitlist = false;
                if (waitlistCount > 0) waitlistCount--;
                renderJoinButton();
                renderWaitlistCount();
                Toast.makeText(requireContext(), "Left waitlist", Toast.LENGTH_SHORT).show();
                joinButton.setEnabled(true);
            });
        } else {
            DbManager.getInstance().addUserToWaitlist(eventId, entrantId).addOnSuccessListener(v -> {
                isOnWaitlist = true;
                waitlistCount++;
                renderJoinButton();
                renderWaitlistCount();
                Toast.makeText(requireContext(), "Joined waitlist", Toast.LENGTH_SHORT).show();
                joinButton.setEnabled(true);
            });
        }
    }

    private void renderJoinButton() {
        if (joinButton == null) return;
        joinButton.setText(isOnWaitlist ? R.string.leave_waitlist : R.string.join_waitlist);
    }

    private void renderWaitlistCount() {
        if (textWaitingList != null) textWaitingList.setText("waiting list: " + waitlistCount);
    }
}