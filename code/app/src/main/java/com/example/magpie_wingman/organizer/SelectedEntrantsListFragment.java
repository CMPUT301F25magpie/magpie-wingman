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
import java.util.List;

/**
 * Fragment that displays the list of entrants who have been "Selected" (Invited to Apply).
 * This corresponds to the 'registrable' subcollection in Firestore.
 * The Organizer can view this list and manually remove entrants if needed.
 */
public class SelectedEntrantsListFragment extends Fragment implements SelectedEntrantsAdapter.OnEntrantRemoveListener {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private SelectedEntrantsAdapter adapter;
    private List<UserProfile> selectedEntrantsList;
    private String eventId;

    public SelectedEntrantsListFragment() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selected_entrants_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        recyclerView = view.findViewById(R.id.recycler_view_selected_entrants);
        emptyStateText = view.findViewById(R.id.text_empty_state); // Bind the new text view

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
     * Fetches users from the 'invited' subcollection of the event.
     */
    private void loadEntrants() {
        DbManager.getInstance().getEventInvited(eventId)
                .addOnSuccessListener(userIds -> {
                    selectedEntrantsList.clear();

                    // Handle Empty State
                    if (userIds.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        toggleEmptyState(true);
                        return;
                    }
                    toggleEmptyState(false);

                    // Fetch names for each ID
                    for (String uid : userIds) {
                        DbManager.getInstance().getUserName(uid).addOnSuccessListener(name -> {
                            // Using UserProfile model for display
                            UserProfile profile = new UserProfile(uid, name, UserRole.ENTRANT);
                            selectedEntrantsList.add(profile);
                            adapter.notifyItemInserted(selectedEntrantsList.size() - 1);
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("SelectedEntrants", "Error loading list", e));
    }

    private void toggleEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRemoveClicked(int position, UserProfile user) {
        // Remove from 'registrable' collection in Firestore
        DbManager.getInstance().cancelRegistrable(eventId, user.getUserId())
                .addOnSuccessListener(aVoid -> {
                    selectedEntrantsList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, selectedEntrantsList.size());

                    // Check if list became empty after removal
                    if (selectedEntrantsList.isEmpty()) {
                        toggleEmptyState(true);
                    }

                    Toast.makeText(getContext(), "Removed " + user.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove", Toast.LENGTH_SHORT).show());
    }
}