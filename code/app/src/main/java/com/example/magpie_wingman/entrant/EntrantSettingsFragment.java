package com.example.magpie_wingman.entrant;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
/**
 * Settings screen for entrant users.
 *
 */
public class EntrantSettingsFragment extends PreferenceFragmentCompat {
    public static final String ARG_ENTRANT_ID = "arg_entrant_id";

    // Cached once on create; used by the delete flow.
    private String entrantId;

    /**
     * Helper to build this fragment with an entrant id argument.
     *
     * @param entrantId the app/user id of the signed-in entrant
     * @return a configured EntrantSettingsFragment
     */
    public static EntrantSettingsFragment newInstance(String entrantId) {
        EntrantSettingsFragment f = new EntrantSettingsFragment();
        Bundle b = new Bundle();
        b.putString(ARG_ENTRANT_ID, entrantId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1) Prefer the id passed in via arguments.
        if (getArguments() != null) {
            entrantId = getArguments().getString(ARG_ENTRANT_ID);
        }

        // 2) Fallback to a locally cached id (guest/session flows).
        if (TextUtils.isEmpty(entrantId)) {
            entrantId = loadUserIdFromPrefs();
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference deletePref = findPreference("pref_delete_account");
        if (deletePref != null) {
            // Disable the action if we don't have a valid id.
            deletePref.setEnabled(!TextUtils.isEmpty(entrantId));

            deletePref.setOnPreferenceClickListener(pref -> {
                confirmAndDeleteProfile();
                return true;
            });
        }
    }


    /**
     * Shows a confirmation dialog before deleting the user's profile.
     * No-op if the user cancels.
     */
    private void confirmAndDeleteProfile() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete your profile?")
                .setMessage("This will remove you from all waitlists/registrations and delete your account. This cannot be undone.")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Delete", (d, w) -> performDelete())
                .show();
    }

    /**
     * Calls the preexisting DbManager.deleteEntrant(...) and finishes the Activity on success.
     * Clears any locally cached "user_id" to avoid stale sessions.
     */
    private void performDelete() {
        if (TextUtils.isEmpty(entrantId)) {
            Toast.makeText(requireContext(), "Could not resolve your user ID.", Toast.LENGTH_LONG).show();
            return;
        }

        DbManager.getInstance()
                .deleteEntrant(entrantId)
                .addOnSuccessListener(v -> {
                    // Clear locally cached id for guest/legacy flows, if present.
                    try {
                        requireContext()
                                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .remove("user_id")
                                .apply();
                    } catch (Throwable ignored) {}

                    Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
                    // Return to splash/login or close current activity.
                    requireActivity().finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Reads a user id from SharedPreferences, or null if not set.
     *
     * @return cached user id or null
     */
    @Nullable
    private String loadUserIdFromPrefs() {
        try {
            return requireContext()
                    .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .getString("user_id", null);
        } catch (Throwable t) {
            return null;
        }
    }
}