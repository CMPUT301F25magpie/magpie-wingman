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

import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.User;

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

    // Argument keys used when constructing this fragment with a Bundle.
    private static final String ARG_EVENT_ID          = "eventId";
    private static final String ARG_EVENT_NAME        = "eventName";
    private static final String ARG_EVENT_LOCATION    = "eventLocation";
    private static final String ARG_EVENT_START_TIME  = "eventStartTime";
    private static final String ARG_EVENT_DESCRIPTION = "eventDescription";

    // Event and user identifiers.
    private String eventId;
    private String entrantId;

    // Basic event details passed through arguments.
    private String eventName;
    private String eventLocation;
    private long   eventStartTimeMillis = -1;
    private String eventDescription;

    // UI references.
    private TextView textWaitingList;
    private Button   joinButton;

    // Waitlist state for the current entrant.
    private boolean isOnWaitlist = false;
    private int     waitlistCount = 0;

    // Formatter used to show the event date and time.
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd - hh:mm a", Locale.getDefault());

    /**
     * Required empty public constructor.
     *
     * <p>Fragments must have a public no-argument constructor so the system
     * can recreate them when needed.</p>
     */
    public DetailedEventDescriptionFragment() {
        // Required empty constructor
    }

    /**
     * Convenience factory method for creating a new instance of this fragment
     * with the given event details bundled as arguments.
     *
     * @param eventId        unique ID of the event (Firestore document id)
     * @param eventName      name/title of the event
     * @param eventLocation  location of the event, or null if unknown
     * @param eventStartTime start time of the event in milliseconds since epoch
     * @param description    description text for the event
     * @return a new {@link DetailedEventDescriptionFragment} with arguments set
     */
    public static DetailedEventDescriptionFragment newInstance(String eventId,
                                                               String eventName,
                                                               String eventLocation,
                                                               long eventStartTime,
                                                               String description) {
        DetailedEventDescriptionFragment fragment = new DetailedEventDescriptionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        args.putString(ARG_EVENT_NAME, eventName);
        args.putString(ARG_EVENT_LOCATION, eventLocation);
        args.putLong(ARG_EVENT_START_TIME, eventStartTime);
        args.putString(ARG_EVENT_DESCRIPTION, description);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called when the fragment is being created.
     *
     * <p>Here we pull event information out of the arguments bundle and resolve
     * the currently logged-in user from {@link MyApp}.</p>
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read the event details from fragment arguments (if provided).
        Bundle args = getArguments();
        if (args != null) {
            eventId              = args.getString(ARG_EVENT_ID);
            eventName            = args.getString(ARG_EVENT_NAME);
            eventLocation        = args.getString(ARG_EVENT_LOCATION);
            eventStartTimeMillis = args.getLong(ARG_EVENT_START_TIME, -1L);
            eventDescription     = args.getString(ARG_EVENT_DESCRIPTION);
        }

        // Get the current entrant user from the application singleton.
        User current = MyApp.getInstance().getCurrentUser();
        if (current != null) {
            // Use whatever getter returns the Firestore user ID.
            entrantId = current.getUserId(); // adjust if your User API differs
        }
    }

    /**
     * Inflates the fragment's layout.
     *
     * @return the root view for this fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detailed_event_description, container, false);
    }

    /**
     * Called after the view has been created.
     *
     * <p>This method binds view references, populates the basic event details,
     * sets up the back button, and initializes the join/leave waitlist logic.</p>
     */
    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Bind all the views from the layout.
        ImageButton backButton   = v.findViewById(R.id.button_back);
        ImageView   posterImage  = v.findViewById(R.id.image_event_poster);
        TextView    titleText    = v.findViewById(R.id.text_event_title);
        TextView    locationText = v.findViewById(R.id.text_event_location);
        TextView    dateText     = v.findViewById(R.id.text_event_date);
        textWaitingList          = v.findViewById(R.id.text_waiting_list);
        TextView descriptionText = v.findViewById(R.id.text_event_description);
        joinButton               = v.findViewById(R.id.button_join_waitlist);

        // Fill in basic static event details (title, location, date, description).
        titleText.setText(!TextUtils.isEmpty(eventName) ? eventName : "Event");
        locationText.setText(
                !TextUtils.isEmpty(eventLocation) ? eventLocation : "Location TBD"
        );

        if (eventStartTimeMillis > 0L) {
            dateText.setText(dateFormat.format(new Date(eventStartTimeMillis)));
        } else {
            dateText.setText("Date TBD");
        }

        if (!TextUtils.isEmpty(eventDescription)) {
            descriptionText.setText(eventDescription);
        }

        // Back button simply pops this fragment off the back stack.
        backButton.setOnClickListener(view -> {
            NavController navController = Navigation.findNavController(view);
            navController.popBackStack();
        });

        // If we are missing event or user information, disable joining.
        if (eventId == null || entrantId == null) {
            joinButton.setEnabled(false);
            Toast.makeText(
                    requireContext(),
                    "Missing event or user information; cannot join waitlist.",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        // Load the current waitlist state from Firestore (my status + total count).
        loadWaitlistState();

        // Tapping the button should toggle between join and leave on the waitlist.
        joinButton.setOnClickListener(v1 -> toggleJoinLeave());
    }

    /**
     * Loads the current waitlist state for this event from Firestore.
     *
     * <p>This checks:
     * <ul>
     *     <li>whether the current entrant is already on the waitlist</li>
     *     <li>how many total users are in the waitlist</li>
     * </ul>
     * and then updates the UI accordingly.</p>
     */
    private void loadWaitlistState() {
        // Check if this entrant is already on the waitlist.
        DbManager.getInstance()
                .isUserInWaitlist(eventId, entrantId)
                .addOnSuccessListener(isIn -> {
                    isOnWaitlist = Boolean.TRUE.equals(isIn);
                    renderJoinButton();
                });

        // Fetch the full waitlist and compute its size.
        DbManager.getInstance()
                .getEventWaitlist(eventId)
                .addOnSuccessListener(users -> {
                    waitlistCount = (users != null) ? users.size() : 0;
                    renderWaitlistCount();
                });
    }

    /**
     * Toggles the current entrant's membership on the waitlist.
     *
     * <p>If the entrant is currently on the waitlist, this removes them. If they
     * are not on the waitlist, this adds them. On success, it updates the local
     * state and refreshes the button and count.</p>
     */
    private void toggleJoinLeave() {
        // Prevent multiple quick taps while the Firestore operation is pending.
        joinButton.setEnabled(false);

        if (isOnWaitlist) {
            // User is currently on the waitlist → remove them.
            DbManager.getInstance()
                    .cancelWaitlist(eventId, entrantId)
                    .addOnSuccessListener(v -> {
                        isOnWaitlist = false;
                        if (waitlistCount > 0) {
                            waitlistCount--;
                        }
                        renderJoinButton();
                        renderWaitlistCount();
                        Toast.makeText(requireContext(),
                                "Left waitlist", Toast.LENGTH_SHORT).show();
                        joinButton.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                "Leave failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        joinButton.setEnabled(true);
                    });
        } else {
            // User is not on the waitlist yet → add them.
            DbManager.getInstance()
                    .addUserToWaitlist(eventId, entrantId)
                    .addOnSuccessListener(v -> {
                        isOnWaitlist = true;
                        waitlistCount++;
                        renderJoinButton();
                        renderWaitlistCount();
                        Toast.makeText(requireContext(),
                                "Joined waitlist", Toast.LENGTH_SHORT).show();
                        joinButton.setEnabled(true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                "Join failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        joinButton.setEnabled(true);
                    });
        }
    }

    /**
     * Updates the join button text to reflect the current waitlist status.
     *
     * <p>Shows "Join waitlist" when the entrant is not on the waitlist and
     * "Leave waitlist" when they are.</p>
     */
    private void renderJoinButton() {
        if (joinButton == null) return;
        joinButton.setText(
                isOnWaitlist ? R.string.leave_waitlist : R.string.join_waitlist
        );
    }

    /**
     * Updates the waitlist count label with the current number of users waiting.
     */
    private void renderWaitlistCount() {
        if (textWaitingList != null) {
            textWaitingList.setText("waiting list: " + waitlistCount);
        }
    }
}
