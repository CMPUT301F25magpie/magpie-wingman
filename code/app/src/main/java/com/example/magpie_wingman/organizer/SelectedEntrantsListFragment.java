package com.example.magpie_wingman.organizer;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Entrant;
import com.example.magpie_wingman.data.model.User;
import com.example.magpie_wingman.data.model.UserRole;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class SelectedEntrantsListFragment extends Fragment implements SelectedEntrantsAdapter.OnEntrantRemoveListener {

    private RecyclerView recyclerView;
    private SelectedEntrantsAdapter adapter;
    private List<Entrant> selectedEntrantsList;

    private DbManager dbManager;
    private String eventId;

    public SelectedEntrantsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.eventId = getArguments().getString("eventId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selected_entrants_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            dbManager = DbManager.getInstance();
        } catch (IllegalStateException e) {
            if (getContext() != null) {
                DbManager.init(getContext().getApplicationContext());
                dbManager = DbManager.getInstance();
            }
        }

        selectedEntrantsList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_view_selected_entrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new SelectedEntrantsAdapter(selectedEntrantsList, this);
        recyclerView.setAdapter(adapter);

        if (eventId != null) {
            loadSelectedEntrants(eventId);
        } else {
            Toast.makeText(getContext(), "Error: No Event ID provided", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Fetches the list of "registrable" user IDs, then fetches
     * each User object for that list.
     */
    private void loadSelectedEntrants(String eventId) {
        FirebaseFirestore db = dbManager.getDb();

        dbManager.getEventRegistrable(eventId)
                .addOnSuccessListener(userIds -> {
                    if (userIds == null || userIds.isEmpty()) {
                        Log.d("SelectedEntrants", "No entrants in registrable list.");
                        return;
                    }

                    List<Task<DocumentSnapshot>> userTasks = new ArrayList<>();
                    for (String userId : userIds) {
                        userTasks.add(db.collection("users").document(userId).get());
                    }

                    Tasks.whenAllSuccess(userTasks)
                            .addOnSuccessListener(results -> {
                                selectedEntrantsList.clear();

                                for (Object docObj : results) {
                                    DocumentSnapshot doc = (DocumentSnapshot) docObj;

                                    String userId = doc.getId();
                                    String userName = doc.getString("name"); 
                                    String userEmail = doc.getString("email");
                                    String userPhone = doc.getString("phone");
                                    String userProfileImage = doc.getString("ProfileImageUrl");
                                    String userDeviceId = doc.getString("deviceId");

                                    Boolean isOrganizer = doc.getBoolean("isOrganizer");
                                    if (isOrganizer == null || !isOrganizer) {
                                        // Use Entrant constructor.
                                        Entrant entrant = new Entrant(
                                                userId,
                                                userName,
                                                userProfileImage,
                                                userEmail,
                                                userPhone,
                                                userDeviceId
                                        );
                                        selectedEntrantsList.add(entrant);
                                    }
                                    // --- END OF FIX ---
                                }

                                adapter.notifyDataSetChanged();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("SelectedEntrants", "Failed to fetch user profiles", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("SelectedEntrants", "Failed to get registrable list", e);
                });
    }

    @Override
    public void onRemoveClicked(int position) {
        Entrant entrantToRemove = selectedEntrantsList.get(position);
        String userId = entrantToRemove.getUserId();
        String userName = entrantToRemove.getName();

        dbManager.cancelRegistrable(eventId, userId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Removed " + userName, Toast.LENGTH_SHORT).show();
                    selectedEntrantsList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, selectedEntrantsList.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to remove " + userName, Toast.LENGTH_SHORT).show();
                    Log.e("SelectedEntrants", "Failed to remove user", e);
                });
    }
}