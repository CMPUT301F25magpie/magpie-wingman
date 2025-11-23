package com.example.magpie_wingman.admin;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.UserProfile;
import com.example.magpie_wingman.data.model.UserRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin screen for viewing and managing user profiles.
 */
public class AdminProfilesFragment extends Fragment implements ProfileAdapter.OnProfileRemoveListener {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private final List<UserProfile> userProfileList = new ArrayList<>();

    public AdminProfilesFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view_admin_profiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProfileAdapter(userProfileList, this);
        recyclerView.setAdapter(adapter);

        refreshProfiles(null);
    }

    /** Loads profiles from Firestore and refreshes the list. */
    private void refreshProfiles(@Nullable UserRole filter) {
        DbManager.getInstance().fetchProfiles(filter)
                .addOnSuccessListener(list -> {
                    userProfileList.clear();
                    userProfileList.addAll(list);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to load profiles: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // Called when the "X" is clicked in a row
    @Override
    public void onRemoveClicked(int position) {
        UserProfile target = userProfileList.get(position);

        if (target.getRole() == UserRole.ORGANIZER) {
            // Organizer-specific actions
            new AlertDialog.Builder(requireContext())
                    .setTitle("Manage " + target.getName())
                    .setMessage("Choose what to do with this organizer:")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setNeutralButton("Delete Account", (d, w) -> {
                        // HARD DELETE: remove account + from all events (existing path)
                        DbManager.getInstance().deleteProfile(target.getUserId(), target.getRole())
                                .addOnSuccessListener(v -> {
                                    userProfileList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemRangeChanged(position, userProfileList.size());
                                    Toast.makeText(requireContext(), "Profile removed", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Remove failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    })
                    .setPositiveButton("Revoke & Delete Events", (d, w) -> {
                        // SOFT REMOVE: revoke organizer + delete all events (keeps account as Entrant)
                        DbManager.getInstance().revokeOrganizerAndDeleteEvents(target.getUserId())
                                .addOnSuccessListener(v -> {
                                    // EITHER: update row locally with a NEW immutable UserProfile…
                                    UserProfile updated = new UserProfile(
                                            target.getUserId(),
                                            target.getName(),
                                            UserRole.ENTRANT,
                                            target.getProfileImageUrl()
                                    );
                                    userProfileList.set(position, updated);
                                    adapter.notifyItemChanged(position);

                                    // …OR if you prefer server truth, comment the 3 lines above and use:
                                    // refreshProfiles(null);

                                    Toast.makeText(requireContext(), "Organizer revoked and events deleted.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Action failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    })
                    .show();

        } else {
            // Entrant: behave like before (hard delete)
            new AlertDialog.Builder(requireContext())
                    .setTitle("Remove " + target.getName() + "?")
                    .setMessage("This will remove this user from all events and delete their profile. This cannot be undone.")
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton("Remove", (d, w) -> {
                        DbManager.getInstance().deleteProfile(target.getUserId(), target.getRole())
                                .addOnSuccessListener(v -> {
                                    userProfileList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                    adapter.notifyItemRangeChanged(position, userProfileList.size());
                                    Toast.makeText(requireContext(), "Profile removed", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(), "Remove failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    })
                    .show();
        }
    }
}