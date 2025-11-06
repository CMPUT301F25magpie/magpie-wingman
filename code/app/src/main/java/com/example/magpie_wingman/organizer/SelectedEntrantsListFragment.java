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

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Entrant; // Your team's file

import java.util.ArrayList;
import java.util.List;

public class SelectedEntrantsListFragment extends Fragment implements SelectedEntrantsAdapter.OnEntrantRemoveListener {

    private RecyclerView recyclerView;
    private SelectedEntrantsAdapter adapter;
    private List<Entrant> selectedEntrantsList;

    public SelectedEntrantsListFragment() {
        // Required empty public constructor
    }

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
        loadMockEntrants();

        adapter = new SelectedEntrantsAdapter(selectedEntrantsList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Creates mock data using the correct Entrant constructor.
     */
    private void loadMockEntrants() {
        selectedEntrantsList = new ArrayList<>();




        selectedEntrantsList.add(new Entrant("p5", "Person 5", "p5@email.com", "555-0005", "dev5"));
        selectedEntrantsList.add(new Entrant("p6", "Person 6", "p6@email.com", "555-0006", "dev6"));
        selectedEntrantsList.add(new Entrant("p7", "Person 7", "p7@email.com", "555-0007", "dev7"));
        selectedEntrantsList.add(new Entrant("p8", "Person 8", "p8@email.com", "555-0008", "dev8"));
    }

    // This is the new method from the interface
    // It runs when the "X" is clicked
    @Override
    public void onRemoveClicked(int position) {
        selectedEntrantsList.remove(position);
        adapter.notifyItemRemoved(position);
        adapter.notifyItemRangeChanged(position, selectedEntrantsList.size());
    }
}