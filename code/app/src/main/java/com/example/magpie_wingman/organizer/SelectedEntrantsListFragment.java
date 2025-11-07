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
import com.example.magpie_wingman.data.model.Entrant; // Make sure this import is correct

import java.util.ArrayList;
import java.util.List;

// 1. Implement the adapter's interface
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_selected_entrants_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_selected_entrants);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadMockEntrants();

        // 2. Pass 'this' (the fragment) as the listener to the adapter
        adapter = new SelectedEntrantsAdapter(selectedEntrantsList, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Creates mock data to display in the list.
     */
    private void loadMockEntrants() {
        selectedEntrantsList = new ArrayList<>();
        // 3. Update mock data to match the "Person X" format from the mockup
        /**selectedEntrantsList.add(new Entrant("Person 5", "Invited"));
        selectedEntrantsList.add(new Entrant("Person 6", "Invited"));
        selectedEntrantsList.add(new Entrant("Person 7", "Invited"));
        selectedEntrantsList.add(new Entrant("Person 8", "Invited"));*/
    }

    // 4. This is the new method from the interface
    // It runs when the "X" is clicked
    @Override
    public void onRemoveClicked(int position) {
        // Remove the item from our data list
        selectedEntrantsList.remove(position);

        // Tell the adapter that the item was removed so it can update the screen
        adapter.notifyItemRemoved(position);

        // This makes sure all other positions are updated correctly
        adapter.notifyItemRangeChanged(position, selectedEntrantsList.size());
    }
}



    /*
    in fire base change to
    private void loadMockEntrants() {
    // Get the real data from Firebase
    // This will call your DatabaseManager
    firebaseDb.collection("events")
              .document("myEventId")
              .collection("selectedEntrants")
              .get()
              .addOnCompleteListener(task -> {
                  // ... loop over the results and add them to the list ...
                  // ... then notify the adapter ...
              });
}
     */
