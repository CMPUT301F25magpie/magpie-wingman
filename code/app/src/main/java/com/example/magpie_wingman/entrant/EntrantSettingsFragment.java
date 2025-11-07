package com.example.magpie_wingman.entrant;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
/**
 * Settings screen for entrant users.
 *
 */
public class EntrantSettingsFragment extends PreferenceFragmentCompat {
    /**
     * Inflates preferences and attaches the delete-account handler.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        Preference deletePref = findPreference("pref_delete_account");
        if (deletePref != null) {
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

    private void performDelete() {
        String userId = resolveCurrentUserId();
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(requireContext(), "Could not resolve your user ID.", Toast.LENGTH_LONG).show();
            return;
        }
        DbManager.getInstance().deleteEntrant(userId)
                .addOnSuccessListener(v -> {

                    // If you cache a local user id for guest flows, clear it here:
                    try {
                        requireContext()
                                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .remove("user_id")
                                .apply();
                    } catch (Throwable ignored) {}

                    Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
                    // Return to splash/login or close current activity
                    requireActivity().finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private String resolveCurrentUserId() {
        return Settings.Secure.getString(requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

}