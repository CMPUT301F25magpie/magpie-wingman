package com.example.magpie_wingman.organizer;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of entrants who have cancelled their participation
 * in a given event. Cancelled entrants are stored in the Firestore subcollection:
 *     events/{eventId}/cancelled
 * <p>This fragment fetches the list of cancelled user IDs, resolves the display names
 * using {@link DbManager#getUserName(String)}, and constructs simple row views
 * (icon + name) dynamically inside a vertical LinearLayout container.</p>
 */
public class CancelledListFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    private LinearLayout listContainer;

    public CancelledListFragment() {

    }
    /**
     * Creates a new instance of {@link CancelledListFragment} with the provided event ID
     * stored in its arguments bundle.
     *
     * @param eventId The Firestore event ID whose cancelled list should be displayed.
     * @return A new {@link CancelledListFragment} containing the event ID as an argument.
     */

    public static CancelledListFragment newInstance(String eventId) {
        CancelledListFragment f = new CancelledListFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }

    /**
     * Inflates the cancelled entrants list layout.
     *
     * @param inflater  The LayoutInflater used to inflate XML views.
     * @param container The parent into which the UI will be placed.
     * @param savedInstanceState Previously saved state, if any.
     * @return The root view for the cancelled list fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cancelled_list, container, false);
    }

    /**
     * Initializes UI elements including the back button and the container that will
     * hold dynamically-created rows. Retrieves the event ID from the fragment arguments
     * and triggers the Firestore load for the cancelled entrants list.
     *
     * @param view               The root view returned by {@link #onCreateView}.
     * @param savedInstanceState Saved fragment state, or null if newly created.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.button_back);
        listContainer = view.findViewById(R.id.cancelled_list_container);

        btnBack.setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp()
        );

        String eventId = (getArguments() != null)
                ? getArguments().getString(ARG_EVENT_ID)
                : null;

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing event id", Toast.LENGTH_LONG).show();
            return;
        }

        loadCancelledList(eventId);
    }

    /**
     * Loads all cancelled entrants for the specified event from Firestore.
     * Retrieves the list of user IDs from "events/{eventId}/cancelled", then fetches
     * each user's display name using {@link DbManager#getUserName(String)}.
     *
     * <p>Builds UI rows inside the fragment's LinearLayout container. Displays an
     * empty-state message if no cancelled users exist or if a Firestore error occurs.</p>
     *
     * @param eventId The ID of the event whose cancelled entrants should be displayed.
     */
    private void loadCancelledList(@NonNull String eventId) {
        DbManager db = DbManager.getInstance();

        db.getEventCancelled(eventId)
                .addOnSuccessListener(userIds -> {
                    listContainer.removeAllViews();

                    if (userIds == null || userIds.isEmpty()) {
                        addEmptyStateRow("No cancelled entrants.");
                        return;
                    }

                    // get names
                    List<Task<String>> nameTasks = new ArrayList<>(userIds.size());
                    for (String uid : userIds) {
                        nameTasks.add(db.getUserName(uid));
                    }

                    Tasks.whenAllSuccess(nameTasks)
                            .addOnSuccessListener(rawNames -> {
                                for (int i = 0; i < userIds.size(); i++) {
                                    String name = null;
                                    if (i < rawNames.size()) {
                                        Object o = rawNames.get(i);
                                        if (o instanceof String) {
                                            name = (String) o;
                                        }
                                    }
                                    if (name == null || name.trim().isEmpty()) {
                                        name = userIds.get(i);
                                    }
                                    listContainer.addView(makePersonRow(requireContext(), name));
                                }
                            })
                            .addOnFailureListener(e ->
                                    addEmptyStateRow("Failed to load names: " + e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    listContainer.removeAllViews();
                    addEmptyStateRow("Failed to load cancelled entrants: " + e.getMessage());
                });
    }

    /**
     * Creates a single row representing a cancelled entrant, consisting of a person icon
     * and their display name.
     *
     * @param ctx  The context used to construct the view.
     * @param name The display name to show in the row.
     * @return A fully constructed horizontal LinearLayout representing one person row.
     */
    private View makePersonRow(Context ctx, String name) {
        int padV = dp(ctx, 8);
        int iconSize = dp(ctx, 20);
        int iconMarginEnd = dp(ctx, 8);

        LinearLayout row = new LinearLayout(ctx);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, padV, 0, padV);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView iv = new ImageView(ctx);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        ivLp.setMarginEnd(iconMarginEnd);
        iv.setLayoutParams(ivLp);
        iv.setImageResource(R.drawable.ic_person);
        iv.setContentDescription("User");
        iv.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF000000));

        TextView tv = new TextView(ctx);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(name);
        tv.setTextColor(0xFF000000);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        row.addView(iv);
        row.addView(tv);
        return row;
    }

    /**
     * Adds a simple TextView to the container showing an empty-state or error message.
     *
     * @param message The text to display to the user.
     */
    private void addEmptyStateRow(String message) {
        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(message);
        tv.setTextColor(0xFF000000);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        listContainer.addView(tv);
    }

    /**
     * Converts density-independent pixels (dp) into raw pixels based on the device's
     * screen density.
     *
     * @param ctx The context providing display metrics.
     * @param dp  The dp value to convert.
     * @return The corresponding pixel value as an integer.
     */
    private static int dp(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }
}
