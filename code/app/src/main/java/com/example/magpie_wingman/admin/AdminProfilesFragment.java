package com.example.magpie_wingman.admin;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.UserProfile; // Make sure this import is correct

import java.util.ArrayList;
import java.util.List;

// 1. Implement the adapter's interface
public class AdminProfilesFragment extends Fragment implements ProfileAdapter.OnProfileRemoveListener {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<UserProfile> userProfileList;

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
        loadMockProfiles();

        // 2. Pass 'this' (the fragment) as the listener
        adapter = new ProfileAdapter(userProfileList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Creates mock data based on the new mockup.
     */
    private void loadMockProfiles() {
        userProfileList = new ArrayList<>();
        userProfileList.add(new UserProfile("Person 1", "Entrant"));
        userProfileList.add(new UserProfile("Person 2", "Entrant"));
        userProfileList.add(new UserProfile("Person 3", "Entrant"));
        userProfileList.add(new UserProfile("Person 4", "Entrant"));
        userProfileList.add(new UserProfile("Person 5", "Entrant"));
        userProfileList.add(new UserProfile("Person 6", "Entrant"));
        userProfileList.add(new UserProfile("Person 7", "Entrant"));
        userProfileList.add(new UserProfile("Person 8", "Entrant"));
        userProfileList.add(new UserProfile("Jane Doe", "Entrant"));
        userProfileList.add(new UserProfile("John Doe", "Entrant"));
        userProfileList.add(new UserProfile("Stuart Little", "Entrant"));
        userProfileList.add(new UserProfile("Satoru Gojo", "Entrant"));
        userProfileList.add(new UserProfile("Spike Spiegel", "Organizer"));
    }

    // 3. This method runs when the "X" is clicked
    @Override
    public void onRemoveClicked(int position) {
        // Remove the item from our data list
        userProfileList.remove(position);

        // Tell the adapter that the item was removed
        adapter.notifyItemRemoved(position);

        // Update all the other items' positions
        adapter.notifyItemRangeChanged(position, userProfileList.size());
    }
}