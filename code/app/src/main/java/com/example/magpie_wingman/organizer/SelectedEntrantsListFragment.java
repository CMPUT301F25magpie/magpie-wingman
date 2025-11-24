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
import com.example.magpie_wingman.data.model.Entrant; // Make sure this import is correct
import com.example.magpie_wingman.data.DbManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectedEntrantsListFragment extends Fragment implements SelectedEntrantsAdapter.OnEntrantRemoveListener {

    private RecyclerView recyclerView;
    private SelectedEntrantsAdapter adapter;
    private List<Entrant> selectedEntrantsList;

    private DbManager dbManager;

    public SelectedEntrantsListFragment() {
        // Required empty public constructor
    }

    private String eventId;

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

        // init list + adapter BEFORE loading data
        selectedEntrantsList = new ArrayList<>();
        adapter = new SelectedEntrantsAdapter(selectedEntrantsList, this);
        recyclerView.setAdapter(adapter);

        loadSelectedEntrants();
    }


    // 4. This is the new method from the interface
    // It runs when the "X" is clicked
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

    private void loadSelectedEntrants() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .collection("registrable")
                .get()
                .addOnSuccessListener(snap -> {
                    selectedEntrantsList.clear();

                    for (QueryDocumentSnapshot doc : snap) {
                        String userId = doc.getId();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userSnap -> {
                                    String userName = userSnap.getString("name");
                                    if (userName == null || userName.isEmpty()) {
                                        userName = userId;
                                    }
                                    selectedEntrantsList.add(new Entrant(userId, userName));
                                    adapter.notifyDataSetChanged(); //
                                })
                                .addOnFailureListener(e -> {

                                    selectedEntrantsList.add(new Entrant(userId, userId));
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to load registrable users.",
                                Toast.LENGTH_SHORT).show()
                );
    }

}