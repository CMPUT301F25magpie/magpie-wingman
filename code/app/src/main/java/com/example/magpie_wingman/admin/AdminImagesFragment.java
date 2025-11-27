package com.example.magpie_wingman.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin screen showing all event posters with delete controls.
 */
public class AdminImagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AdminImagesAdapter adapter;
    private DbManager db;

    public AdminImagesFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = DbManager.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        recyclerView = view.findViewById(R.id.recycler_view_admin_images);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        adapter = new AdminImagesAdapter(new ArrayList<>(), this::confirmDelete);
        recyclerView.setAdapter(adapter);

        loadImages();
    }

    private void loadImages() {
        db.getAllEvents()
                .addOnSuccessListener(events -> {
                    List<AdminImageItem> items = new ArrayList<>();
                    for (Event e : events) {
                        String url = e.getEventPosterURL();
                        if (url != null && !url.isEmpty()) {
                            items.add(new AdminImageItem(
                                    e.getEventId(),
                                    e.getEventName(),
                                    url
                            ));
                        }
                    }
                    adapter.setItems(items);
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load images.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void confirmDelete(AdminImageItem item) {
        new AlertDialog.Builder(getContext())
                .setTitle("Remove poster")
                .setMessage("Remove the poster for \"" + item.getEventName() + "\"?")
                .setPositiveButton("Remove", (d, w) -> deletePoster(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePoster(AdminImageItem item) {
        db.removeEventPoster(item.getEventId(), item.getPosterUrl())
                .addOnSuccessListener(unused -> {
                    adapter.removeItem(item);
                    Toast.makeText(getContext(), "Poster removed.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Failed to remove poster.", Toast.LENGTH_SHORT).show());
    }
}