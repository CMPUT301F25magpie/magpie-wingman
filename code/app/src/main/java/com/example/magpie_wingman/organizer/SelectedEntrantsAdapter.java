package com.example.magpie_wingman.organizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.UserProfile;

import java.util.List;

public class SelectedEntrantsAdapter extends RecyclerView.Adapter<SelectedEntrantsAdapter.ViewHolder> {

    public interface OnEntrantRemoveListener {
        void onRemoveClicked(int position, UserProfile user);
    }

    private final List<UserProfile> entrants;
    private final OnEntrantRemoveListener listener;

    public SelectedEntrantsAdapter(List<UserProfile> entrants, OnEntrantRemoveListener listener) {
        this.entrants = entrants;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_selected_entrant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserProfile user = entrants.get(position);

        holder.name.setText(user.getName());

        // Handle "X" button click
        holder.removeBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClicked(holder.getAdapterPosition(), user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_view_name);
            removeBtn = itemView.findViewById(R.id.id_close);
        }
    }
}