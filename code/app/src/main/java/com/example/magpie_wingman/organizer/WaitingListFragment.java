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
import com.example.magpie_wingman.data.model.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
/**
 * Fragment that displays the waiting list of entrants for a given event.
 * The waitlist corresponds to the "waitlist" subcollection of an event in Firestore.
 *
 * <p>The fragment retrieves the list of user IDs from Firestore, resolves each
 * to a name from the "users" collection, and displays them in a RecyclerView.</p>
 */
public class WaitingListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private WaitlistAdapter adapter;
    private List<Entrant> waitlist;
    private String eventId = "sampling#1213";

    public WaitingListFragment() {}

    /**
     * Inflates the waiting list layout for this fragment.
     *
     * @param inflater  The LayoutInflater used to inflate the fragment's view.
     * @param container The parent view the UI is attached to.
     * @param savedInstanceState Previously saved state, if available.
     * @return The root view for the waiting list fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_waiting_list, container, false);
    }

    /**
     * Initializes UI components including the RecyclerView, back button, and progress bar.
     * Retrieves the event ID from fragment arguments and triggers loading of the waiting list.
     *
     * @param view               The root view created by {@link #onCreateView}.
     * @param savedInstanceState Previously saved state, if available.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

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
            loadWaitlist();
        } else {
            Toast.makeText(requireContext(), "Invalid event ID", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads the list of users waiting for an event from Firestore. Queries the event's
     * "waitlist" subcollection to get user IDs, then resolves each ID to a display name
     * from the "users" collection.
     *
     * <p>The method updates the progress bar, empty-state message, and RecyclerView adapter
     * based on the results. All operations are asynchronous and handled through Firestore listeners.</p>
     */
    private void loadWaitlist() {
        waitlist = new ArrayList<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        db.collection("events")
                .document(eventId)
                .collection("waitlist")
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
                                "Failed to load waitlist.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * RecyclerView adapter used to display a list of entrants on the waitlist.
     * Each row corresponds to one person and uses the layout from item_waiting_person.xml.
     */
    private static class WaitlistAdapter
            extends RecyclerView.Adapter<WaitlistAdapter.ViewHolder> {

        private List<String> entrantList;

        WaitlistAdapter(List<String> entrantList) {
            this.entrantList = entrantList;
        }

        /**
         * Replaces the current data set with a new list of entrant names and refreshes the UI.
         *
         * @param entrantList The new list of names to display in the RecyclerView.
         */
        void setData(List<String> entrantList) {
            this.entrantList = entrantList;
            notifyDataSetChanged();
        }

        /**
         * Inflates a new view holder for the waitlist item layout.
         *
         * @param parent  The parent ViewGroup for the row.
         * @param viewType Unused viewType parameter.
         * @return A new {@link ViewHolder} instance for the row.
         */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_waiting_person, parent, false);
            return new ViewHolder(view);
        }

        /**
         * Binds the entrant's name to the row and sets the click listener for the
         * location button, which currently displays a Toast placeholder.
         *
         * @param holder   The ViewHolder representing the row.
         * @param position The position of the item being bound.
         */
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

        /**
         * Returns the number of entrants in the waitlist.
         *
         * @return The size of the entrant list, or 0 if the list is null.
         */
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
