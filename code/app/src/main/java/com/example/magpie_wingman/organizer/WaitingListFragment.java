package com.example.magpie_wingman.organizer;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * US 02.02.01
 * organizer can view the list of entrants currently on the event’s waiting list
 */
public class WaitingListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private WaitlistAdapter adapter;

    private String eventId;

    public WaitingListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_waiting_list, container, false);

        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_waitlist);
        NavController navController = NavHostFragment.findNavController(this);
        toolbar.setNavigationOnClickListener(v -> navController.navigateUp());

        recyclerView = view.findViewById(R.id.recycler_waitlist);
        progressBar = view.findViewById(R.id.waitlist_progress);
        emptyText = view.findViewById(R.id.waitlist_empty_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new WaitlistAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        if (eventId != null) {
            loadWaitlist(eventId);
        } else {
            Toast.makeText(requireContext(),
                    getString(R.string.toast_failed_waitlist, "Invalid event ID"),
                    Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadWaitlist(String eventId) {
        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        FirebaseFirestore db = DbManager.getInstance().getDb();

        db.collection("events")
                .document(eventId)
                .collection("waitlist")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    List<String> entrants = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name = doc.getString("name");
                        if (name == null || name.isEmpty()) {
                            name = getString(R.string.waitlist_default_name);
                        }
                        entrants.add(name);
                    }

                    if (entrants.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                    } else {
                        adapter.setData(entrants);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            getString(R.string.toast_failed_waitlist, e.getMessage()),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private static class WaitlistAdapter extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {
        private List<String> entrantList;

        public WaitlistAdapter(List<String> entrantList) {
            this.entrantList = entrantList;
        }

        public void setData(List<String> entrantList) {
            this.entrantList = entrantList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(entrantList.get(position));
        }

        @Override
        public int getItemCount() {
            return entrantList.size();
        }

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