package com.example.magpie_wingman.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.magpie_wingman.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the admin's event poster grid.
 */
public class AdminImagesAdapter extends RecyclerView.Adapter<AdminImagesAdapter.ImageViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(AdminImageItem item);
    }

    private List<AdminImageItem> items;
    private final OnDeleteClickListener deleteListener;

    public AdminImagesAdapter(List<AdminImageItem> items,
                              OnDeleteClickListener deleteListener) {
        this.items = items != null ? items : new ArrayList<>();
        this.deleteListener = deleteListener;
    }

    public void setItems(List<AdminImageItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(AdminImageItem item) {
        int index = items.indexOf(item);
        if (index != -1) {
            items.remove(index);
            notifyItemRemoved(index);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_admin_image, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        AdminImageItem item = items.get(position);

        Glide.with(holder.poster.getContext())
                .load(item.getPosterUrl())
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_broken_image)
                .into(holder.poster);

        holder.removeButton.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView poster;
        ImageButton removeButton;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.image_poster);
            removeButton = itemView.findViewById(R.id.button_remove_image);
        }
    }
}

