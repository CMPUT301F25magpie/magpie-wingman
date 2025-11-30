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
 * This corresponds to the 'registrable' subcollection in Firestore.
 * The Organizer can view this list and manually remove entrants if needed.
 */
public class SelectedEntrantsListFragment extends Fragment implements SelectedEntrantsAdapter.OnEntrantRemoveListener {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private SelectedEntrantsAdapter adapter;

    // now holds user + status (invited / registrable)
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
     * Loads all users from both 'invited' and 'registrable' subcollections.
     * This subset = all users who have received an invite, but haven't yet declined or registered.
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

                                    // invited -> "undecided"
                                    for (String uid : invitedIds) {
                                        if (!seen.add(uid)) continue;
                                        db.getUserName(uid).addOnSuccessListener(name -> {
                                            UserProfile profile = new UserProfile(uid, name, UserRole.ENTRANT);
                                            selectedEntrantsList.add(
                                                    new SelectedEntrantRow(profile, SelectedStatus.INVITED));
                                            adapter.notifyItemInserted(selectedEntrantsList.size() - 1);
                                        });
                                    }

                                    // registrable -> "accepted"
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