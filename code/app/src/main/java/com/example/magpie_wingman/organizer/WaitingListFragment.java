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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Entrant;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WaitingListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private WaitlistAdapter adapter;
    private List<Entrant> waitlist;
    private String eventId = "sampling#1213";

    public WaitingListFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_waiting_list, container, false);

        // THE FOLLOWING MADE THE APP CRASH
//        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_waitlist);
//
//        try {
//            NavController navController = NavHostFragment.findNavController(this);
//            toolbar.setNavigationOnClickListener(v -> navController.navigateUp());
//        } catch (Exception e) { /* Ignore for testing */ }

        recyclerView = view.findViewById(R.id.recycler_waitlist);
        progressBar = view.findViewById(R.id.waitlist_progress);
        emptyText = view.findViewById(R.id.waitlist_empty_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WaitlistAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) eventId = getArguments().getString("eventId");

        if (eventId != null) {
            loadWaitlist();
        } else {
            Toast.makeText(requireContext(), "Invalid event ID", Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
    }

    private void loadWaitlist() {
        waitlist = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(snap -> {
                    waitlist.clear();
                    final int total = snap.size();
                    if (total == 0) {
                        progressBar.setVisibility(View.GONE);
                        adapter.setData(new ArrayList<>());
                        emptyText.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> names = new ArrayList<>();
                    final int[] done = {0};

                    for (QueryDocumentSnapshot doc : snap) {
                        String userId = doc.getId();

                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(userSnap -> {
                                    String userName = userSnap.getString("name");
                                    if (userName == null || userName.isEmpty()) userName = userId;

                                    names.add(userName);
                                })
                                .addOnFailureListener(e -> {

                                    waitlist.add(new Entrant(userId, userId, null, null, null, null));
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
                    if (getContext() != null) Toast.makeText(getContext(), "Failed to load waitlist.", Toast.LENGTH_SHORT).show();
                });
    }

    private static class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {
        private List<String> entrantList;
        public WaitlistAdapter(List<String> entrantList) { this.entrantList = entrantList; }
        public void setData(List<String> entrantList) { this.entrantList = entrantList; notifyDataSetChanged(); }

        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(entrantList.get(position));
        }
        @Override public int getItemCount() { return entrantList.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(16);
            }
        }
    }
}