package com.example.magpie_wingman.entrant;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;

/**
 * Settings screen for entrant users backed by a custom layout
 * {@code fragment_entrant_settings.xml}.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Load/display the user's display name.</li>
 *   <li>Toggle/persist notification preferences.</li>
 *   <li>Log out (clear local session and finish Activity).</li>
 *   <li>Delete account (calls preexisting {@link DbManager#deleteEntrant(String)}).</li>
 * </ul>
 *
 * <p>Pass the signed-in user's id via {@link #newInstance(String)} or ensure it is
 * available in SharedPreferences under "user_id".</p>
 */
public class EntrantSettingsFragment extends Fragment {

    /** Argument key for the signed-in entrant's app/user id. */
    public static final String ARG_ENTRANT_ID = "arg_entrant_id";

    // Cached once on create; used by UI actions like delete.
    private String entrantId;

    // UI
    private TextView nameText;
    private Switch adminNotificationsSwitch;
    private Switch organizerNotificationsSwitch;
    private Button logoutButton;
    private Button deleteButton;

    // Pref keys for local persistence of switch states
    private static final String PREFS = "app_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NOTIFY_ADMINS = "notify_admins";
    private static final String KEY_NOTIFY_ORGANIZERS = "notify_organizers";

    /**
     * Helper to build this fragment with an entrant id argument.
     *
     * @param entrantId the app/user id of the signed-in entrant
     * @return a configured EntrantSettingsFragment
     */
    public static EntrantSettingsFragment newInstance(@NonNull String entrantId) {
        Bundle b = new Bundle();
        b.putString(ARG_ENTRANT_ID, entrantId);
        EntrantSettingsFragment f = new EntrantSettingsFragment();
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

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        // Inflate XML
        View root = inflater.inflate(R.layout.fragment_entrant_settings, container, false);

        // --- Bind views ---
        ImageButton back = root.findViewById(R.id.button_back);
        nameText = root.findViewById(R.id.text_user_name);
        adminNotificationsSwitch = root.findViewById(R.id.switch_admin_notifications);
        organizerNotificationsSwitch = root.findViewById(R.id.switch_organizer_notifications);
        View rowEditProfile = root.findViewById(R.id.row_edit_profile);
        View rowAboutUs = root.findViewById(R.id.row_about_us);
        logoutButton = root.findViewById(R.id.button_logout);
        deleteButton = root.findViewById(R.id.button_delete_account);

        // --- Back button ---
        back.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // --- Load/display user name ---
        bindUserName();

        // --- Initialize switches from prefs and persist changes ---
        initNotificationSwitches();

        // --- Edit Profile ---
//        rowEditProfile.setOnClickListener(v -> {
            // TODO: Navigate to your EditProfile screen/fragment.
            // e.g., NavHostFragment.findNavController(this).navigate(R.id.action_settings_to_editProfile);

//        });

        // --- About Us ---
//        rowAboutUs.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
//                .setTitle("About us")
//                .setMessage("Wingman — sample app for event management.\nVersion 1.0")
//                .setPositiveButton(android.R.string.ok, null)
//                .show());

        // --- Log out ---
//        logoutButton.setOnClickListener(v -> {
//            clearUserIdFromPrefs();
//            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
//            requireActivity().finish();
//        });

        // --- Delete account (preexisting DbManager method) ---
        deleteButton.setOnClickListener(v -> confirmAndDeleteProfile());

        // Disable destructive buttons if id is unknown
        boolean hasId = !TextUtils.isEmpty(entrantId);
        deleteButton.setEnabled(hasId);
        logoutButton.setEnabled(true); // logout can still clear local state

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        ImageView btnAboutUs = view.findViewById(R.id.btn_about_us);
        Button btnLogOut = view.findViewById(R.id.button_logout);

        NavController navController = Navigation.findNavController(view);

        btnEditProfile.setOnClickListener(v -> navController.navigate(R.id.action_entrantSettingsFragment_to_entrantEditProfileFragment));
        btnAboutUs.setOnClickListener(v -> navController.navigate(R.id.action_entrantSettingsFragment_to_entrantAboutUsFragment));
        btnLogOut.setOnClickListener(v -> navController.navigate(R.id.action_entrantSettingsFragment_to_loginFragment));
    }

    // =============================================================================================
    // Data / actions
    // =============================================================================================

    /**
     * Resolves and displays the entrant's name using DbManager.getUserName(entrantId).
     * Falls back to the raw id if the name is missing.
     */
    private void bindUserName() {
        if (TextUtils.isEmpty(entrantId)) {
            nameText.setText("Guest");
            return;
        }
        try {
            DbManager.getInstance()
                    .getUserName(entrantId)
                    .addOnSuccessListener(name -> {
                        if (name == null || name.trim().isEmpty()) {
                            nameText.setText(entrantId);
                        } else {
                            nameText.setText(name);
                        }
                    })
                    .addOnFailureListener(e -> nameText.setText(entrantId));
        } catch (Throwable t) {
            // Defensive: if DbManager not ready for any reason, fall back to id
            nameText.setText(entrantId);
        }
    }

    /**
     * Initialize switches from SharedPreferences and persist changes.
     * Replace/augment with Firestore-backed settings if desired later.
     */
    private void initNotificationSwitches() {
        boolean adminsOn = getPrefs().getBoolean(KEY_NOTIFY_ADMINS, false);
        boolean organizersOn = getPrefs().getBoolean(KEY_NOTIFY_ORGANIZERS, true);

        adminNotificationsSwitch.setChecked(adminsOn);
        organizerNotificationsSwitch.setChecked(organizersOn);

        adminNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                getPrefs().edit().putBoolean(KEY_NOTIFY_ADMINS, isChecked).apply());

        organizerNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                getPrefs().edit().putBoolean(KEY_NOTIFY_ORGANIZERS, isChecked).apply());
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
                    clearUserIdFromPrefs();
                    Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show();
                    // Return to splash/login or close current activity.
                    requireActivity().finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // =============================================================================================
    // Local storage helpers
    // =============================================================================================

    /**
     * Reads a user id from SharedPreferences, or null if not set.
     *
     * @return cached user id or null
     */
    @Nullable
    private String loadUserIdFromPrefs() {
        try {
            return getPrefs().getString(KEY_USER_ID, null);
        } catch (Throwable t) {
            return null;
        }
    }

    /** Removes locally cached user id from SharedPreferences. */
    private void clearUserIdFromPrefs() {
        try {
            getPrefs().edit().remove(KEY_USER_ID).apply();
        } catch (Throwable ignored) {}
    }

    /** Convenience accessor for the app's SharedPreferences. */
    private android.content.SharedPreferences getPrefs() {
        return requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
