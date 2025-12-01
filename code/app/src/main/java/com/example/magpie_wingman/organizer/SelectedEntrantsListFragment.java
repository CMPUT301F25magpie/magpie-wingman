package com.example.magpie_wingman.organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.UserProfile;
import com.example.magpie_wingman.data.model.UserRole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Fragment that displays the list of entrants who have been "Selected" (Invited to Apply).
 * This corresponds to the 'invited' and 'registrable' subcollections in Firestore.
 * The Organizer can view this list and manually remove entrants if needed.
 */
public class SelectedEntrantsListFragment extends Fragment implements SelectedEntrantsAdapter.OnEntrantRemoveListener {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private SelectedEntrantsAdapter adapter;

    // now holds user and status (invited / registrable)
    private List<SelectedEntrantRow> selectedEntrantsList;
    private String eventId;

    public SelectedEntrantsListFragment() {}

    // status for each row
    enum SelectedStatus {
        INVITED,       // "undecided"
        REGISTRABLE    // "accepted"
    }

    static class SelectedEntrantRow {
        final UserProfile profile;
        final SelectedStatus status;

        SelectedEntrantRow(UserProfile profile, SelectedStatus status) {
            this.profile = profile;
            this.status = status;
        }
    }

    /**
     * Retrieves the event ID passed to the fragment through arguments.
     *
     * @param savedInstanceState Saved state if the fragment is being recreated.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }
    /**
     * Inflates the selected entrants list layout for this fragment.
     *
     * @param inflater  The LayoutInflater used to inflate the views.
     * @param container The parent view for the fragment's UI.
     * @param savedInstanceState Saved fragment state, if any.
     * @return The inflated view for the selected entrants list screen.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selected_entrants_list, container, false);
    }

    /**
     * Initializes UI components including the RecyclerView, adapter, and back button.
     * If a valid event ID was supplied, loads the invited and registrable entrants for display.
     *
     * @param view               Root view returned by {@link #onCreateView}.
     * @param savedInstanceState Previously saved state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        recyclerView = view.findViewById(R.id.recycler_view_selected_entrants);
        emptyStateText = view.findViewById(R.id.text_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        selectedEntrantsList = new ArrayList<>();
        adapter = new SelectedEntrantsAdapter(selectedEntrantsList, this);
        recyclerView.setAdapter(adapter);

        if (eventId != null) {
            loadEntrants();
        } else {
            Toast.makeText(getContext(), "Error: Event ID missing", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads all user IDs from both the 'invited' and 'registrable' subcollections of the event.
     * For each ID, retrieves the user's name and constructs a {@link SelectedEntrantRow} indicating
     * whether the user is invited (undecided) or registrable (accepted but not fully registered).
     * The results are added to the RecyclerView's backing list and displayed to the user.
     */
    private void loadEntrants() {
        DbManager db = DbManager.getInstance();

        db.getEventInvited(eventId)
                .addOnSuccessListener(invitedIds ->
                        db.getEventRegistrable(eventId)
                                .addOnSuccessListener(registrableIds -> {
                                    selectedEntrantsList.clear();

                                    if (invitedIds.isEmpty() && registrableIds.isEmpty()) {
                                        adapter.notifyDataSetChanged();
                                        toggleEmptyState(true);
                                        return;
                                    }
                                    toggleEmptyState(false);

                                    Set<String> seen = new HashSet<>();

                                    // invited = "undecided" because they havent replied to the invitation yet
                                    for (String uid : invitedIds) {
                                        if (!seen.add(uid)) continue;
                                        db.getUserName(uid).addOnSuccessListener(name -> {
                                            UserProfile profile = new UserProfile(uid, name, UserRole.ENTRANT);
                                            selectedEntrantsList.add(
                                                    new SelectedEntrantRow(profile, SelectedStatus.INVITED));
                                            adapter.notifyItemInserted(selectedEntrantsList.size() - 1);
                                        });
                                    }

                                    // registrable = "accepted" (but haven't finalized registration yet, like RSVP)
                                    for (String uid : registrableIds) {
                                        if (!seen.add(uid)) continue;
                                        db.getUserName(uid).addOnSuccessListener(name -> {
                                            UserProfile profile = new UserProfile(uid, name, UserRole.ENTRANT);
                                            selectedEntrantsList.add(
                                                    new SelectedEntrantRow(profile, SelectedStatus.REGISTRABLE));
                                            adapter.notifyItemInserted(selectedEntrantsList.size() - 1);
                                        });
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Log.e("SelectedEntrants", "Error loading registrable list", e)))
                .addOnFailureListener(e ->
                        Log.e("SelectedEntrants", "Error loading invited list", e));
    }

    /**
     * Shows or hides the empty-state UI depending on whether the list is empty.
     *
     * @param isEmpty True if the list is empty and the empty-state message should be shown;
     *                false to show the RecyclerView instead.
     */
    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    /**
     * Callback triggered when the organizer clicks the remove button for a specific entrant.
     * Removes the entrant from either the 'registrable' or 'invited' subcollection depending
     * on their status, updates the UI list, and shows a Toast confirmation.
     *
     * @param position The adapter position of the entrant being removed.
     * @param user     The {@link UserProfile} representing the entrant to remove.
     */
    @Override
    public void onRemoveClicked(int position, UserProfile user) {
        if (position < 0 || position >= selectedEntrantsList.size()) return;

        SelectedEntrantRow row = selectedEntrantsList.get(position);
        boolean isRegistrable = (row.status == SelectedStatus.REGISTRABLE);

        if (isRegistrable) {
            DbManager.getInstance()
                    .cancelRegistrable(eventId, user.getUserId())
                    .addOnSuccessListener(aVoid -> {
                        selectedEntrantsList.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, selectedEntrantsList.size());
                        if (selectedEntrantsList.isEmpty()) {
                            toggleEmptyState(true);
                        }
                        Toast.makeText(getContext(),
                                "Removed " + user.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Failed to remove", Toast.LENGTH_SHORT).show());
        } else {
            DbManager.getInstance()
                    .cancelInvited(eventId, user.getUserId())
                    .addOnSuccessListener(aVoid -> {
                        selectedEntrantsList.remove(position);
                        adapter.notifyItemRemoved(position);
                        adapter.notifyItemRangeChanged(position, selectedEntrantsList.size());
                        if (selectedEntrantsList.isEmpty()) {
                            toggleEmptyState(true);
                        }
                        Toast.makeText(getContext(),
                                "Removed " + user.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Failed to remove", Toast.LENGTH_SHORT).show());
        }
    }
}