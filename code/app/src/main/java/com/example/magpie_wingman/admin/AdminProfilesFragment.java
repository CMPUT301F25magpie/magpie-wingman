package com.example.magpie_wingman.admin;

import android.app.AlertDialog;
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
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.UserProfile; // Make sure this import is correct
import com.example.magpie_wingman.data.model.UserRole;

import java.util.ArrayList;
import java.util.List;

// 1. Implement the adapter's interface
public class AdminProfilesFragment extends Fragment implements ProfileAdapter.OnProfileRemoveListener {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private final List<UserProfile> userProfileList = new ArrayList<>();

    public AdminProfilesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_admin_profiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // loadMockProfiles();

        // 2. Pass 'this' (the fragment) as the listener
        adapter = new ProfileAdapter(userProfileList, this);
        recyclerView.setAdapter(adapter);

        refreshProfiles(null);
    }

    private void refreshProfiles(@Nullable UserRole filter) {
        DbManager.getInstance().fetchProfiles(filter)
                .addOnSuccessListener(list -> {
                    userProfileList.clear();
                    userProfileList.addAll(list);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load profiles: " +
                                e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Creates mock data based on the new mockup.
     */
//    private void loadMockProfiles() {
//        userProfileList = new ArrayList<>();
//        userProfileList.add(new UserProfile("Person 1", "Entrant"));
//        userProfileList.add(new UserProfile("Person 2", "Entrant"));
//        userProfileList.add(new UserProfile("Person 3", "Entrant"));
//        userProfileList.add(new UserProfile("Person 4", "Entrant"));
//        userProfileList.add(new UserProfile("Person 5", "Entrant"));
//        userProfileList.add(new UserProfile("Person 6", "Entrant"));
//        userProfileList.add(new UserProfile("Person 7", "Entrant"));
//        userProfileList.add(new UserProfile("Person 8", "Entrant"));
//        userProfileList.add(new UserProfile("Jane Doe", "Entrant"));
//        userProfileList.add(new UserProfile("John Doe", "Entrant"));
//        userProfileList.add(new UserProfile("Stuart Little", "Entrant"));
//        userProfileList.add(new UserProfile("Satoru Gojo", "Entrant"));
//        userProfileList.add(new UserProfile("Spike Spiegel", "Organizer"));
//    }

    // 3. This method runs when the "X" is clicked
    @Override
    public void onRemoveClicked(int position) {
        UserProfile target = userProfileList.get(position);

        String warning = (target.getRole() == UserRole.ORGANIZER)
                ? "This will delete ALL events created by this organizer and remove them everywhere."
                : "This will remove this user from all events and delete their profile.";

        new AlertDialog.Builder(requireContext())
                .setTitle("Remove " + target.getName() + "?")
                .setMessage(warning + " This cannot be undone")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Remove", (d,w) -> {
                    DbManager.getInstance().deleteProfile(target.getUserId(), target.getRole())
                            .addOnSuccessListener(v -> {
                                userProfileList.remove(position);
                                adapter.notifyItemRemoved(position);
                                adapter.notifyItemRangeChanged(position, userProfileList.size());
                                Toast.makeText(requireContext(), "Profile removed", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(), "Remove failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                })
                .show();
    }
}