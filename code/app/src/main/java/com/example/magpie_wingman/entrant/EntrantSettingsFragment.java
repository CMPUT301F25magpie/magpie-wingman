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

import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.User;
import com.example.magpie_wingman.data.model.UserRole;

/**
 * Settings screen for entrant users backed by a custom layout
 * {@code fragment_entrant_settings.xml}.
 *
 * Responsibilities:
 *  - Load/display the user's display name.
 *  - Toggle/persist notification preferences.
 *  - Log out (clear local session and finish Activity).
 *  - Delete account (calls preexisting DbManager#deleteEntrant(String)).
 */
public class EntrantSettingsFragment extends Fragment {

    public static final String ARG_ENTRANT_ID = "arg_entrant_id";

    private String entrantId;

    // UI
    private TextView nameText;
    private Switch adminNotificationsSwitch;
    private Switch organizerNotificationsSwitch;
    private Button logoutButton;
    private Button deleteButton;

    // Pref keys
    private static final String PREFS = "app_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NOTIFY_ADMINS = "notify_admins";
    private static final String KEY_NOTIFY_ORGANIZERS = "notify_organizers";

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

        if (getArguments() != null) {
            entrantId = getArguments().getString(ARG_ENTRANT_ID);
        }

        if (TextUtils.isEmpty(entrantId)) {
            entrantId = loadUserIdFromPrefs();
        }

        User currentUser = MyApp.getInstance().getCurrentUser();
        entrantId = currentUser.getUserId();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
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

        back.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        bindUserName();
        initNotificationSwitches();

        deleteButton.setOnClickListener(v -> confirmAndDeleteProfile());

        boolean hasId = !TextUtils.isEmpty(entrantId);
        deleteButton.setEnabled(hasId);
        logoutButton.setEnabled(true);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        ImageView btnAboutUs = view.findViewById(R.id.btn_about_us);
        Button btnLogOut = view.findViewById(R.id.button_logout);

        // NEW: mode-switch buttons
        Button btnEntrantMode = view.findViewById(R.id.button_entrant_mode);
        Button btnOrganizerMode = view.findViewById(R.id.button_organizer_mode);

        NavController navController = Navigation.findNavController(view);

        btnEditProfile.setOnClickListener(
                v -> navController.navigate(R.id.action_entrantSettingsFragment_to_entrantEditProfileFragment)
        );
        btnAboutUs.setOnClickListener(
                v -> navController.navigate(R.id.action_entrantSettingsFragment_to_entrantAboutUsFragment)
        );
        btnLogOut.setOnClickListener(
                v -> navController.navigate(R.id.action_entrantSettingsFragment_to_loginFragment)
        );

        // --- Mode switch logic based on current user ---
        User currentUser = MyApp.getInstance().getCurrentUser();

        if (currentUser == null || !currentUser.isOrganizer()) {
            // Not an organizer -> no mode switching
            btnEntrantMode.setVisibility(View.GONE);
            btnOrganizerMode.setVisibility(View.GONE);
        } else {
            // Entrant settings = already in entrant mode
            btnEntrantMode.setEnabled(false);
            btnOrganizerMode.setEnabled(true);

            btnOrganizerMode.setOnClickListener(v ->
                    navController.navigate(R.id.organizerLandingFragment2));
        }
    }

    // =============================================================================================
    // Data / actions
    // =============================================================================================

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
            nameText.setText(entrantId);
        }
    }

    private void initNotificationSwitches() {
        if (TextUtils.isEmpty(entrantId)) {
            adminNotificationsSwitch.setEnabled(false);
            organizerNotificationsSwitch.setEnabled(false);
            return;
        }

        adminNotificationsSwitch.setEnabled(false);
        organizerNotificationsSwitch.setEnabled(false);

        DbManager dbManager = DbManager.getInstance();

        dbManager.getDb()
                .collection("users")
                .document(entrantId)
                .get()
                .addOnSuccessListener(doc -> {
                    // Read values from Firestore
                    Boolean notifAdmin = doc.getBoolean("notifAdmin");
                    Boolean notifOrg   = doc.getBoolean("notifOrg");

                    // If fields don't exist yet, use local defaults/prefs
                    boolean adminsOn = (notifAdmin != null)
                            ? notifAdmin
                            : getPrefs().getBoolean(KEY_NOTIFY_ADMINS, false);

                    boolean organizersOn = (notifOrg != null)
                            ? notifOrg
                            : getPrefs().getBoolean(KEY_NOTIFY_ORGANIZERS, true);

                    // Set initial state
                    adminNotificationsSwitch.setChecked(adminsOn);
                    organizerNotificationsSwitch.setChecked(organizersOn);

                    // Keep prefs in sync
                    getPrefs().edit()
                            .putBoolean(KEY_NOTIFY_ADMINS, adminsOn)
                            .putBoolean(KEY_NOTIFY_ORGANIZERS, organizersOn)
                            .apply();

                    // Now attach listeners that write back to Firestore
                    attachNotificationListeners();

                    adminNotificationsSwitch.setEnabled(true);
                    organizerNotificationsSwitch.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    // If Firestore read fails, fall back to prefs
                    boolean adminsOn = getPrefs().getBoolean(KEY_NOTIFY_ADMINS, false);
                    boolean organizersOn = getPrefs().getBoolean(KEY_NOTIFY_ORGANIZERS, true);

                    adminNotificationsSwitch.setChecked(adminsOn);
                    organizerNotificationsSwitch.setChecked(organizersOn);

                    attachNotificationListeners();

                    adminNotificationsSwitch.setEnabled(true);
                    organizerNotificationsSwitch.setEnabled(true);

                    Toast.makeText(requireContext(),
                            "Failed to load notification settings; using last saved values.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void attachNotificationListeners() {
        adminNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DbManager.getInstance()
                    .updateNotificationPrefs(entrantId, isChecked, null)
                    .addOnSuccessListener(v -> {
                        getPrefs().edit().putBoolean(KEY_NOTIFY_ADMINS, isChecked).apply();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                "Failed to update admin notifications: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        // revert switch
                        adminNotificationsSwitch.setChecked(!isChecked);
                    });
        });

        organizerNotificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            DbManager.getInstance()
                    .updateNotificationPrefs(entrantId, null, isChecked)
                    .addOnSuccessListener(v -> {
                        getPrefs().edit().putBoolean(KEY_NOTIFY_ORGANIZERS, isChecked).apply();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                "Failed to update organizer notifications: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        organizerNotificationsSwitch.setChecked(!isChecked);
                    });
        });
    }

    private void confirmAndDeleteProfile() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete your profile?")
                .setMessage("This will remove you from all waitlists/registrations and delete your account. This cannot be undone.")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton("Delete", (d, w) -> performDelete())
                .show();
    }

    private void performDelete() {
        if (TextUtils.isEmpty(entrantId)) {
            Toast.makeText(requireContext(),
                    "Could not resolve your user ID.",
                    Toast.LENGTH_LONG).show();
            return;
        }


        final Context appContext = requireContext().getApplicationContext();

        User currentUser = MyApp.getInstance().getCurrentUser();
        UserRole role;

        if (currentUser != null && currentUser.isOrganizer()) {
            // organizer: delete their events AND their account
            role = UserRole.ORGANIZER;
        } else {
            // remove them from all events and delete the profile.
            role = UserRole.ENTRANT;
        }

        DbManager.getInstance()
                .deleteProfile(entrantId, role)
                .addOnSuccessListener(v -> {
                    clearUserIdFromPrefs();

                    Toast.makeText(appContext,
                            "Profile deleted",
                            Toast.LENGTH_SHORT).show();

                    // Avoid requireActivity() here to prevent "fragment not attached" crashes
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(appContext,
                            "Delete failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }


    // =============================================================================================
    // Local storage helpers
    // =============================================================================================

    @Nullable
    private String loadUserIdFromPrefs() {
        try {
            return getPrefs().getString(KEY_USER_ID, null);
        } catch (Throwable t) {
            return null;
        }
    }

    private void clearUserIdFromPrefs() {
        try {
            getPrefs().edit().remove(KEY_USER_ID).apply();
        } catch (Throwable ignored) {}
    }

    private android.content.SharedPreferences getPrefs() {
        return requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}

