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

/**
 * RecyclerView Adapter for displaying a list of selected entrants SelectedEntrantRows.
 * Handles the view binding for each row and the click listener for the remove button.
 */
public class SelectedEntrantsAdapter
        extends RecyclerView.Adapter<SelectedEntrantsAdapter.ViewHolder> {

    public interface OnEntrantRemoveListener {
        void onRemoveClicked(int position, com.example.magpie_wingman.data.model.UserProfile user);
    }

    // this one stores SelectedEntrantRow instead of UserProfile because structure is slightly different
    // includes handling of status indicators for invited + registrable users
    private final List<SelectedEntrantsListFragment.SelectedEntrantRow> entrants;
    private final OnEntrantRemoveListener listener;

    public SelectedEntrantsAdapter(
            List<SelectedEntrantsListFragment.SelectedEntrantRow> entrants,
            OnEntrantRemoveListener listener
    ) {
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

        SelectedEntrantsListFragment.SelectedEntrantRow row = entrants.get(position);

        holder.name.setText(row.profile.getName());

        // small status badge text
        if (holder.status != null) {
            if (row.status == SelectedEntrantsListFragment.SelectedStatus.INVITED) {
                holder.status.setText("Undecided");
            } else {
                holder.status.setText("Accepted");
            }
        }

        holder.removeBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClicked(holder.getAdapterPosition(), row.profile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView status;
        ImageView removeBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_view_name);
            status = itemView.findViewById(R.id.text_view_status); // must add to XML
            removeBtn = itemView.findViewById(R.id.id_close);
        }
    }
}