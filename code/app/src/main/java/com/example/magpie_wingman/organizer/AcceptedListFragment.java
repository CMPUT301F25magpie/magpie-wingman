package com.example.magpie_wingman.organizer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the list of entrants who are fully registered/signed up
 * for a given event (events/{eventId}/registered).
 */
public class AcceptedListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private AcceptedAdapter adapter;
    private List<String> registeredNames;
    private String eventId;


    public AcceptedListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_accepted_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        recyclerView = view.findViewById(R.id.recycler_accepted);
        progressBar = view.findViewById(R.id.accepted_progress);
        emptyText = view.findViewById(R.id.accepted_empty_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AcceptedAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            String fromArgs = getArguments().getString("eventId");
            if (fromArgs != null) {
                eventId = fromArgs;
            }
        }

        if (eventId != null) {
            loadRegisteredEntrants();
        } else {
            Toast.makeText(requireContext(), "Invalid event ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadRegisteredEntrants() {
        registeredNames = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        db.collection("events")
                .document(eventId)
                .collection("registered")
                .get()
                .addOnSuccessListener(snap -> {
                    final int total = snap.size();
                    if (total == 0) {
                        progressBar.setVisibility(View.GONE);
                        adapter.setData(new ArrayList<>());
                        emptyText.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> names = new ArrayList<>(total);
                    final int[] done = {0};

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
                                    names.add(userName);
                                })
                                .addOnFailureListener(e -> {
                                    // Fallback to userId when fail to fetch user doc
                                    names.add(userId);
                                })
                                .addOnCompleteListener(t -> {
                                    done[0]++;
                                    if (done[0] == total) {
                                        progressBar.setVisibility(View.GONE);
                                        if (names.isEmpty()) {
                                            emptyText.setVisibility(View.VISIBLE);
                                        } else {
                                            adapter.setData(names);
                                            recyclerView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Failed to load registered entrants.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Simple adapter that reuses the same row layout as the waitlist list
     * (item_waiting_person.xml), but for registered entrants.
     */
    private static class AcceptedAdapter
            extends RecyclerView.Adapter<AcceptedAdapter.ViewHolder> {

        private List<String> entrantList;

        AcceptedAdapter(List<String> entrantList) {
            this.entrantList = entrantList;
        }

        void setData(List<String> entrantList) {
            this.entrantList = entrantList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_waiting_person, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder,
                                     int position) {
            String name = entrantList.get(position);
            holder.nameText.setText(name);

            holder.locationButton.setOnClickListener(v ->
                    Toast.makeText(v.getContext(),
                            "Show location for " + name,
                            Toast.LENGTH_SHORT).show()
            );
        }

        @Override
        public int getItemCount() {
            return entrantList != null ? entrantList.size() : 0;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView nameText;
            final ImageButton locationButton;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.text_person_name);
                locationButton = itemView.findViewById(R.id.button_location);
                nameText.setTextColor(Color.BLACK);
            }
        }
    }
}
