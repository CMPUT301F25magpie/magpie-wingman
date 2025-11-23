package com.example.magpie_wingman.organizer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import java.util.List;

public class SelectedEntrantsListFragment extends Fragment implements SelectedEntrantsAdapter.OnEntrantRemoveListener {

    private RecyclerView recyclerView;
    private SelectedEntrantsAdapter adapter;
    private List<UserProfile> selectedEntrantsList;
    private String eventId; //

    public SelectedEntrantsListFragment() {
        // Required empty public constructor
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        selectedEntrantsList = new ArrayList<>();
        adapter = new SelectedEntrantsAdapter(selectedEntrantsList, this);
        recyclerView.setAdapter(adapter);

        if (eventId != null) {
            loadEntrants();
        }
    }

    private void loadEntrants() {

        DbManager.getInstance().getEventRegistrable(eventId)
                .addOnSuccessListener(userIds -> {
                    selectedEntrantsList.clear();
                    if (userIds.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        return;
                    }


                    for (String uid : userIds) {
                        DbManager.getInstance().getUserName(uid).addOnSuccessListener(name -> {
                            // Create a temp profile to display
                            UserProfile profile = new UserProfile(uid, name, UserRole.ENTRANT);
                            selectedEntrantsList.add(profile);
                            adapter.notifyItemInserted(selectedEntrantsList.size() - 1);
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("SelectedEntrants", "Error loading list", e));
    }

    @Override
    public void onRemoveClicked(int position, UserProfile user) {
        DbManager.getInstance().cancelRegistrable(eventId, user.getUserId())
                .addOnSuccessListener(aVoid -> {
                    selectedEntrantsList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, selectedEntrantsList.size());
                    Toast.makeText(getContext(), "Removed " + user.getName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to remove", Toast.LENGTH_SHORT).show());
    }
}