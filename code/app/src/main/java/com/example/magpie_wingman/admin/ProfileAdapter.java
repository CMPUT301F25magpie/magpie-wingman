package com.example.magpie_wingman.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.UserProfile; // Make sure this import is correct

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<UserProfile> profileList;
    private OnProfileRemoveListener removeListener; // Listener

    // Interface for the click event
    public interface OnProfileRemoveListener {
        void onRemoveClicked(int position);
    }

    // Constructor
    public ProfileAdapter(List<UserProfile> profileList, OnProfileRemoveListener removeListener) {
        this.profileList = profileList;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_admin_profile, parent, false);
        // Pass listener to ViewHolder
        return new ProfileViewHolder(view, removeListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        UserProfile profile = profileList.get(position);

        // Combine Name and Role into one string
        String profileInfo = profile.getName() + " - " + profile.getRole();
        holder.profileInfoTextView.setText(profileInfo);
    }

    @Override
    public int getItemCount() {
        return profileList.size();
    }

    /**
     * The ViewHolder class
     */
    public static class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView profileInfoTextView; // 5. Only one TextView now
        ImageView removeImageView;

        public ProfileViewHolder(@NonNull View itemView, OnProfileRemoveListener removeListener) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.ic_person);
            profileInfoTextView = itemView.findViewById(R.id.text_profile_name); // 6. Find the combined TextView
            removeImageView = itemView.findViewById(R.id.button_remove);

            // Set the click listener on the button
            removeImageView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && removeListener != null) {
                    removeListener.onRemoveClicked(position);
                }
            });
        }
    }
}