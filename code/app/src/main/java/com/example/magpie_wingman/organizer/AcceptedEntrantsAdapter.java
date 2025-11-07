
package com.example.magpie_wingman.organizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Entrant; // Make sure this import is correct

import java.util.List;

public class AcceptedEntrantsAdapter extends RecyclerView.Adapter<AcceptedEntrantsAdapter.EntrantViewHolder> {

    private List<Entrant> entrantList;
    private OnEntrantRemoveListener removeListener; // Listener

    // 1. Define an interface for the click event
    public interface OnEntrantRemoveListener {
        void onRemoveClicked(int position);
    }

    // 2. Update the constructor to accept the listener
    public AcceptedEntrantsAdapter(List<Entrant> entrantList, OnEntrantRemoveListener removeListener) {
        this.entrantList = entrantList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_entrant, parent, false);
        // 3. Pass the listener to the ViewHolder
        return new EntrantViewHolder(view, removeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Entrant entrant = entrantList.get(position);
        holder.nameTextView.setText(entrant.getName());
        // We removed the status text view, so no code is needed here for it
    }

    @Override
    public int getItemCount() {
        return entrantList.size();
    }

    /**
     * The ViewHolder class
     */
    public static class EntrantViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView nameTextView;
        ImageView removeImageView; // 4. Add reference for remove icon

        public EntrantViewHolder(@NonNull View itemView, OnEntrantRemoveListener removeListener) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.image_view_profile);
            nameTextView = itemView.findViewById(R.id.text_view_entrant_name);
            removeImageView = itemView.findViewById(R.id.image_view_remove); // 5. Find remove icon

            // 6. Set the click listener on the "X"
            removeImageView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                // Make sure the position is valid
                if (position != RecyclerView.NO_POSITION) {
                    removeListener.onRemoveClicked(position);
                }
            });
        }
    }
}