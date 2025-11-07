package com.example.magpie_wingman.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.User;
import com.example.magpie_wingman.data.model.UserRole; // Make sure this is imported

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<User> userList;
    private OnProfileRemoveListener removeListener;

    public interface OnProfileRemoveListener {
        void onRemoveClicked(int position); // Pass the position
    }

    public ProfileAdapter(List<User> userList, OnProfileRemoveListener removeListener) {
        this.userList = userList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_profile, parent, false);
        return new ProfileViewHolder(view, removeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User user = userList.get(position);

        String role = "Entrant"; // Default role

        // This now matches YOUR User.java file
        UserRole userRole = user.getRole();
        if (userRole == UserRole.ORGANIZER) {
            role = "Organizer";
        } else if (userRole == UserRole.ADMIN) {
            role = "Admin";
        }

        String profileInfo = user.getName() + " - " + role; // Use getUserName()
        holder.profileInfoTextView.setText(profileInfo);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * The ViewHolder class
     */
    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView profileInfoTextView;
        ImageView removeImageView;

        public ProfileViewHolder(@NonNull View itemView, OnProfileRemoveListener removeListener) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.image_view_profile);
            profileInfoTextView = itemView.findViewById(R.id.text_view_profile_info);
            removeImageView = itemView.findViewById(R.id.image_view_remove);

            removeImageView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onRemoveClicked(position);
                }
            });
        }
    }
}