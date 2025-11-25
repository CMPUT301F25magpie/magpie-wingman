package com.example.magpie_wingman.entrant;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.navigation.fragment.NavHostFragment;

import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.User;

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

    // Pref keys for local persistence of switch states and user id
    private static final String PREFS = "app_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_NOTIFY_ADMINS = "notify_admins";
    private static final String KEY_NOTIFY_ORGANIZERS = "notify_organizers";

    private String entrantId;

    // UI
    private TextView nameText;
    private Switch adminNotificationsSwitch;
    private Switch organizerNotificationsSwitch;
    private Button logoutButton;
    private Button deleteButton;



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

        User currentUser = MyApp.getInstance().getCurrentUser();
        if (currentUser != null) {
            entrantId = currentUser.getUserId();
        } else {
            entrantId = null;
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
        View view = inflater.inflate(R.layout.fragment_entrant_settings, container, false);

        // Bind views
        ImageButton back = view.findViewById(R.id.button_back);
        ImageView logo = view.findViewById(R.id.image_logo);
        nameText = view.findViewById(R.id.text_user_name);

        adminNotificationsSwitch = view.findViewById(R.id.switch_admin_notifications);
        organizerNotificationsSwitch = view.findViewById(R.id.switch_organizer_notifications);

        View rowEditProfile = view.findViewById(R.id.row_edit_profile);
        View rowAboutUs = view.findViewById(R.id.row_about_us);

        ImageView iconEditProfile = view.findViewById(R.id.btn_edit_profile);
        ImageView iconAboutUs = view.findViewById(R.id.btn_about_us);

        logoutButton = view.findViewById(R.id.button_logout);
        deleteButton = view.findViewById(R.id.button_delete_account);

        // Back button
        back.setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        // Load/display user name
        bindUserName();

        // Initialize switches
        initNotificationSwitches();

        // ========= Nav Controllers =========

        // --- Edit Profile ---
        View.OnClickListener editProfileListener =v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_entrantSettingsFragment_to_entrantEditProfileFragment);
        };

        View.OnClickListener aboutUsListener = v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_entrantSettingsFragment_to_entrantAboutUsFragment);
        };

        // Row + chevron icon both trigger navigation
        rowEditProfile.setOnClickListener(editProfileListener);
        iconEditProfile.setOnClickListener(editProfileListener);

        rowAboutUs.setOnClickListener(aboutUsListener);
        iconAboutUs.setOnClickListener(aboutUsListener);




        // Log out; clear local user id + navigate to login
        logoutButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_entrantSettingsFragment_to_loginFragment);
        });

        // Delete account (preexisting DbManager method)
        deleteButton.setOnClickListener(v -> confirmAndDeleteProfile());

        return view;
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

    // ============================ Delete profile flow =======================================
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
     * Calls the preexisting DbManager method  and finishes the Activity on success.
     * Clears userid to avoid leftover sessions.
     */
    private void performDelete() {
        if (TextUtils.isEmpty(entrantId)) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Could not resolve your user ID.", Toast.LENGTH_LONG).show();
            }
            return;
        }

        DbManager.getInstance()
                .deleteEntrant(entrantId)
                .addOnSuccessListener(v -> {
                    if (!isAdded()) {
                        return;
                    }
                    Toast.makeText(requireContext(), "Profile deleted", Toast.LENGTH_SHORT).show();

                    // Navigate back to login, same as logout
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_entrantSettingsFragment_to_loginFragment);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) {
                        return;
                    }
                    Toast.makeText(
                            requireContext(),
                            "Delete failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    // helpers

    /** Convenience accessor for the app's SharedPreferences. */
    private SharedPreferences getPrefs() {
        return requireContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
