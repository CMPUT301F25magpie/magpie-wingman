package com.example.magpie_wingman.organizer;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AcceptedListFragment extends Fragment implements AcceptedEntrantsAdapter.OnEntrantRemoveListener {

    private RecyclerView recyclerView;
    private AcceptedEntrantsAdapter adapter;
    private List<Entrant> registeredEntrantsList;

    public AcceptedListFragment() {}

    // TODO: Use arguments to get real event ID
    private String eventId = "sampling#1213";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selected_entrants_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_selected_entrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        registeredEntrantsList = new ArrayList<>();
        adapter = new AcceptedEntrantsAdapter(registeredEntrantsList, this);
        recyclerView.setAdapter(adapter);

        loadRegisteredEntrants();
    }

    @Override
    public void onRemoveClicked(int position) {
        registeredEntrantsList.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, registeredEntrantsList.size());
    }

    private void loadRegisteredEntrants() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("events")
                .document(eventId)
                .collection("registered")
                .get()
                .addOnSuccessListener(snap -> {
                    registeredEntrantsList.clear();
                    if (snap.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }

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

                                    registeredEntrantsList.add(new Entrant(userId, userName, null, null, null, null));
                                    adapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {

                                    registeredEntrantsList.add(new Entrant(userId, userId, null, null, null, null));
                                    adapter.notifyDataSetChanged();
                                });
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load registered users.", Toast.LENGTH_SHORT).show()
                );
    }
}