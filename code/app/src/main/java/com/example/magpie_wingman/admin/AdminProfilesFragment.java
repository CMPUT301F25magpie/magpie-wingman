package com.example.magpie_wingman.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.User;
import com.example.magpie_wingman.data.model.UserRole;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private List<User> userList;
    private DbManager dbManager;

    public AdminProfilesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            dbManager = DbManager.getInstance();
        } catch (IllegalStateException e) {
            if (getContext() != null) {
                DbManager.init(getContext().getApplicationContext());
                dbManager = DbManager.getInstance();
            }
        }

        userList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recycler_view_admin_profiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProfileAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        loadProfilesFromFirebase();
    }

    /**
     * Fetches all users from the "users" collection in Firestore.
     */
    private void loadProfilesFromFirebase() {
        FirebaseFirestore db = dbManager.getDb();

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        userList.clear();

                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {

                  

                            String userId = doc.getId();
                            String userName = doc.getString("name");
                            String userEmail = doc.getString("email");
                            String userPhone = doc.getString("phone");
                            String userDeviceId = doc.getString("deviceId");

                            // Read the "isOrganizer" boolean field
                            Boolean isOrganizer = doc.getBoolean("isOrganizer");

                            // Convert the boolean to the UserRole enum
                            UserRole role = UserRole.ENTRANT; // Default to Entrant
                            if (isOrganizer != null && isOrganizer) {
                                role = UserRole.ORGANIZER;
                            }

                            User user = new User(userId, userName, isOrganizer, null, userEmail, userPhone, userDeviceId);
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d("AdminProfilesFragment", "No users found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminProfilesFragment", "Error loading users", e);
                    Toast.makeText(getContext(), "Error loading users", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onRemoveClicked(int position) {
        User userToRemove = userList.get(position);
        String userId = userToRemove.getUserId();
        String userName = userToRemove.getName();

        dbManager.deleteEntrant(userId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Deleted " + userName, Toast.LENGTH_SHORT).show();
                    userList.remove(position);
                    adapter.notifyItemRemoved(position);
                    adapter.notifyItemRangeChanged(position, userList.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to delete " + userName, Toast.LENGTH_SHORT).show();
                    Log.e("AdminProfilesFragment", "Failed to delete user", e);
                });
    }
}