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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment that displays the full details of a single event for entrant users.
 *
 *  Implemented functionality
 *  - Poster image only show when URL exists, otherwise hide
 *  - QR flow
 *  - Waitlist / invitation / registration logic
 */
public class DetailedEventDescriptionFragment extends Fragment {

    // Argument keys used when constructing this fragment with a Bundle.
    private static final String ARG_EVENT_ID          = "eventId";
    private static final String ARG_EVENT_NAME        = "eventName";
    private static final String ARG_EVENT_LOCATION    = "eventLocation";
    private static final String ARG_EVENT_START_TIME  = "eventStartTime";
    private static final String ARG_EVENT_DESCRIPTION = "eventDescription";
    private static final String ARG_EVENT_POSTER_URL  = "eventPosterURL";

    // Membership states for the current entrant
    private enum MembershipState {
        NONE,          // not in any list
        WAITLIST,      // in waitlist
        INVITED,       // in invited
        REGISTRABLE,   // in registrable
        REGISTERED,     // in registered
        CANCELLED // in cancelled

    }

    // Event and user identifiers.
    private String eventId;
    private String eventPosterUrl;
    private String entrantId;

    // Basic event details passed through arguments.
    private String eventName;
    private String eventLocation;
    private long   eventStartTimeMillis = -1L;
    private String eventDescription;

    // UI references.
    private TextView titleText;
    private TextView locationText;
    private TextView dateText;
    private TextView descriptionText;
    private TextView textWaitingList;
    private ImageView posterImage;
    private Button   joinButton;

    // Membership + waitlist info
    private MembershipState membershipState = MembershipState.NONE;
    private int waitlistCount = 0;

    // Formatter used to show the event date and time.
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd - hh:mm a", Locale.getDefault());

    public DetailedEventDescriptionFragment() {
        // Required empty constructor
    }

    /**
     * Convenience factory method for creating a new instance of this fragment
     * with the given event details bundled as arguments.
     */
    public static DetailedEventDescriptionFragment newInstance(String eventId,
                                                               String eventName,
                                                               String eventLocation,
                                                               long eventStartTime,
                                                               String description,
                                                               @Nullable String posterUrl) {
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

        // Read args if present (normal navigation).
        Bundle args = getArguments();
        if (args != null) {
            eventId              = args.getString(ARG_EVENT_ID);
            eventName            = args.getString(ARG_EVENT_NAME);
            eventLocation        = args.getString(ARG_EVENT_LOCATION);
            eventStartTimeMillis = args.getLong(ARG_EVENT_START_TIME, -1L);
            eventDescription     = args.getString(ARG_EVENT_DESCRIPTION);
            eventPosterUrl       = args.getString(ARG_EVENT_POSTER_URL);
        }

        // Current user (entrant)
        User current = MyApp.getInstance().getCurrentUser();
        if (current != null) {
            entrantId = current.getUserId();
        } else {
            entrantId = "test_user_id";
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailed_event_description, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Bind UI
        ImageButton backButton = v.findViewById(R.id.button_back);
        posterImage            = v.findViewById(R.id.image_event_poster);
        titleText              = v.findViewById(R.id.text_event_title);
        locationText           = v.findViewById(R.id.text_event_location);
        dateText               = v.findViewById(R.id.text_event_date);
        textWaitingList        = v.findViewById(R.id.text_waiting_list);
        descriptionText        = v.findViewById(R.id.text_event_description);
        joinButton             = v.findViewById(R.id.button_join_waitlist);

        // Use whatever data we already have from arguments
        bindStaticDetailsFromArgs();

        // back navigation
        backButton.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });

        // missing IDs
        if (eventId == null || entrantId == null) {
            joinButton.setEnabled(false);
            Toast.makeText(
                    requireContext(),
                    "Missing event or user information; cannot join.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // fetch fresh details from Firestore by eventId
        loadEventDetailsFromFirestore();

        // load membership & waitlist info
        loadMembershipStateAndCount();

        // main button behavior
        joinButton.setOnClickListener(v1 -> toggleJoinLeave());
    }

    // -------------------------------------------------------------------------
    // Initial binding from args
    // -------------------------------------------------------------------------

    private void bindStaticDetailsFromArgs() {
        // if poster
        if (!TextUtils.isEmpty(eventPosterUrl)) {
            posterImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(eventPosterUrl)
                    .into(posterImage);
        } else {
            // No poster
            posterImage.setVisibility(View.GONE);
        }

        // Title
        titleText.setText(
                !TextUtils.isEmpty(eventName) ? eventName : "Event"
        );

        // Location
        locationText.setText(
                !TextUtils.isEmpty(eventLocation) ? eventLocation : "Location TBD"
        );

        // Date/Time
        if (eventStartTimeMillis > 0L) {
            dateText.setText(dateFormat.format(new Date(eventStartTimeMillis)));
        } else {
            dateText.setText("Date TBD");
        }

        // Description
        if (!TextUtils.isEmpty(eventDescription)) {
            descriptionText.setText(eventDescription);
        }
    }

    // -------------------------------------------------------------------------
    // Firestore reload
    // -------------------------------------------------------------------------

    /**
     * Fetches fresh event details from Firestore using the eventId.
     * This is important when we navigated here by QR and only had the ID.
     */
    private void loadEventDetailsFromFirestore() {
        if (TextUtils.isEmpty(eventId)) return;

        DbManager.getInstance()
                .getDb()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Event event = documentSnapshot.toObject(Event.class);
                    if (event == null) return;

                    // Update fields only if missing or generic placeholders
                    if (TextUtils.isEmpty(eventName) ||
                            "Event".contentEquals(titleText.getText())) {
                        eventName = event.getEventName();
                        if (!TextUtils.isEmpty(eventName)) {
                            titleText.setText(eventName);
                        }
                    }

                    if (TextUtils.isEmpty(eventLocation) ||
                            "Location TBD".contentEquals(locationText.getText())) {
                        eventLocation = event.getEventLocation();
                        if (!TextUtils.isEmpty(eventLocation)) {
                            locationText.setText(eventLocation);
                        }
                    }

                    if (event.getEventStartTime() != null) {
                        eventStartTimeMillis = event.getEventStartTime().getTime();
                        dateText.setText(dateFormat.format(event.getEventStartTime()));
                    }

                    eventDescription = event.getDescription();
                    if (!TextUtils.isEmpty(eventDescription)) {
                        descriptionText.setText(eventDescription);
                    }

                    // Poster: if we didn't get one from args, use Firestore value
                    if (TextUtils.isEmpty(eventPosterUrl)) {
                        String fromDb = event.getEventPosterURL();
                        if (!TextUtils.isEmpty(fromDb) && getContext() != null) {
                            eventPosterUrl = fromDb;
                            posterImage.setVisibility(View.VISIBLE);
                            Glide.with(getContext())
                                    .load(eventPosterUrl)
                                    .into(posterImage);
                        } else {
                            // Still no URL → keep it hidden
                            posterImage.setVisibility(View.GONE);
                        }
                    }
                });
    }

    // -------------------------------------------------------------------------
    // Membership + waitlist logic
    // -------------------------------------------------------------------------

    /**
     * Loads:
     *  - which list the user is in (waitlist, invited, registrable, registered)
     *  - the current waitlist count
     */
    private void loadMembershipStateAndCount() {
        refreshMembershipState();

        // Waitlist size for display
        DbManager.getInstance()
                .getEventWaitlist(eventId)
                .addOnSuccessListener(users -> {
                    waitlistCount = (users != null) ? users.size() : 0;
                    renderWaitlistCount();
                });
    }

    private void refreshMembershipState() {
        if (TextUtils.isEmpty(eventId) || TextUtils.isEmpty(entrantId)) return;

        FirebaseFirestore db = DbManager.getInstance().getDb();

        // Check canceled
        db.collection("events")
                .document(eventId)
                .collection("cancelled")
                .document(entrantId)
                .get()
                .addOnSuccessListener(cancelDoc -> {
                    if (cancelDoc.exists()) {
                        membershipState = MembershipState.CANCELLED;
                        renderJoinButton();
                    } else {
                        // Next check registered
                        db.collection("events")
                                .document(eventId)
                                .collection("registered")
                                .document(entrantId)
                                .get()
                                .addOnSuccessListener(regDoc -> {
                                    if (regDoc.exists()) {
                                        membershipState = MembershipState.REGISTERED;
                                        renderJoinButton();
                                    } else {
                                        // Next registrable
                                        checkRegistrable(db);
                                    }
                                });
                    }
                });
    }

    private void checkRegistrable(FirebaseFirestore db) {
        db.collection("events")
                .document(eventId)
                .collection("registrable")
                .document(entrantId)
                .get()
                .addOnSuccessListener(regableDoc -> {
                    if (regableDoc.exists()) {
                        membershipState = MembershipState.REGISTRABLE;
                        renderJoinButton();
                    } else {
                        // Next check invited
                        checkInvited(db);
                    }
                });
    }

    private void checkInvited(FirebaseFirestore db) {
        db.collection("events")
                .document(eventId)
                .collection("invited")
                .document(entrantId)
                .get()
                .addOnSuccessListener(invitedDoc -> {
                    if (invitedDoc.exists()) {
                        membershipState = MembershipState.INVITED;
                        renderJoinButton();
                    } else {
                        // If still nothing check waitlist
                        DbManager.getInstance()
                                .isUserInWaitlist(eventId, entrantId)
                                .addOnSuccessListener(isIn -> {
                                    membershipState = Boolean.TRUE.equals(isIn)
                                            ? MembershipState.WAITLIST
                                            : MembershipState.NONE;
                                    renderJoinButton();
                                });
                    }
                });
    }

    /**
     * Main button behavior depending on membershipState:
     *  - NONE -> join waitlist
     *  - WAITLIST -> leave waitlist
     *  - INVITED -> accept invitation (invited -> registrable)
     *  - REGISTRABLE -> register (registrable -> registered)
     *  - REGISTERED -> nothing
     *  - CANCELED -> also nothing
     */
    private void toggleJoinLeave() {
        if (joinButton == null) return;
        joinButton.setEnabled(false);

        switch (membershipState) {
            case NONE:
                // Join waitlist
                DbManager.getInstance()
                        .addUserToWaitlist(eventId, entrantId)
                        .addOnSuccessListener(v -> {
                            membershipState = MembershipState.WAITLIST;
                            waitlistCount++;
                            renderJoinButton();
                            renderWaitlistCount();
                            Toast.makeText(requireContext(),
                                    "Joined waitlist", Toast.LENGTH_SHORT).show();
                            joinButton.setEnabled(true);
                        })
                        .addOnFailureListener(e -> joinButton.setEnabled(true));
                break;

            case WAITLIST:
                // Leave waitlist
                DbManager.getInstance()
                        .cancelWaitlist(eventId, entrantId)
                        .addOnSuccessListener(v -> {
                            membershipState = MembershipState.NONE;
                            if (waitlistCount > 0) waitlistCount--;
                            renderJoinButton();
                            renderWaitlistCount();
                            Toast.makeText(requireContext(),
                                    "Left waitlist", Toast.LENGTH_SHORT).show();
                            joinButton.setEnabled(true);
                        })
                        .addOnFailureListener(e -> joinButton.setEnabled(true));
                break;

            case INVITED:
                // Accept invitation: invited -> registrable
                DbManager.getInstance()
                        .moveUserFromInvitedToRegistrable(eventId, entrantId)
                        .addOnSuccessListener(v -> {
                            membershipState = MembershipState.REGISTRABLE;
                            renderJoinButton();
                            Toast.makeText(requireContext(),
                                    "Invitation accepted. You can now register.",
                                    Toast.LENGTH_SHORT).show();
                            joinButton.setEnabled(true);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(),
                                    "Failed to accept invitation: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            joinButton.setEnabled(true);
                        });
                break;

            case REGISTRABLE:
                // Register: registrable -> registered
                DbManager.getInstance()
                        .addUserToRegistered(eventId, entrantId)
                        .addOnSuccessListener(v -> {
                            membershipState = MembershipState.REGISTERED;
                            renderJoinButton();
                            Toast.makeText(requireContext(),
                                    "You are now registered.", Toast.LENGTH_SHORT).show();
                            // keep disabled
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(),
                                    "Failed to register: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            joinButton.setEnabled(true);
                        });
                break;

            case REGISTERED:
                // Nothing to do; already registered
                Toast.makeText(requireContext(),
                        "You are already registered.", Toast.LENGTH_SHORT).show();
                joinButton.setEnabled(false);
                break;
            case CANCELLED:
                Toast.makeText(requireContext(),
                        "You have been removed from this event.", Toast.LENGTH_SHORT).show();
                joinButton.setEnabled(false);
                break;
        }
    }

    private void renderJoinButton() {
        if (joinButton == null) return;

        switch (membershipState) {
            case NONE:
                joinButton.setEnabled(true);
                joinButton.setText(R.string.join_waitlist);
                break;

            case WAITLIST:
                joinButton.setEnabled(true);
                joinButton.setText(R.string.leave_waitlist);
                break;

            case INVITED:
                joinButton.setEnabled(true);
                joinButton.setText("Accept invitation");
                break;

            case REGISTRABLE:
                joinButton.setEnabled(true);
                joinButton.setText("Register");
                break;

            case REGISTERED:
                joinButton.setEnabled(false);
                joinButton.setText("Registered");
                joinButton.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFCCCCCC) // light gray
                );
                joinButton.setTextColor(0xFF000000); // black text
                break;

            case CANCELLED:
                joinButton.setEnabled(false);
                joinButton.setText("You have been removed from this event");
                joinButton.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(0xFFCCCCCC)
                );
                joinButton.setTextColor(0xFF000000);
                break;
        }
    }

    private void renderWaitlistCount() {
        if (textWaitingList != null) {
            textWaitingList.setText("waiting list: " + waitlistCount);
        }
    }
}
