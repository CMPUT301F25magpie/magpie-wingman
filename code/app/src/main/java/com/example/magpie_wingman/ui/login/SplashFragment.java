package com.example.magpie_wingman.ui.login;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
/**
 * Fragment responsible for displaying the app's splash screen and performing the
 * automatic login check. It briefly shows an animated logo and meanwhile determines whether
 * a user should be logged in automatically based on the device ID/rememberMe attribute.
 *
 * <p>If a matching remembered user is found, the user is loaded and redirected to
 * the entrant landing screen. Otherwise, the fragment navigates to the login screen.</p>
 */

public class SplashFragment extends Fragment {

    private static final long SPLASH_DELAY_MS = 800L; // delay to allow logo animation and provide time for the firestore checks
    /**
     * Inflates the splash screen layout for this fragment.
     *
     * @param inflater  The LayoutInflater used to inflate the view.
     * @param container The parent view that the fragment's UI is attached to.
     * @param savedInstanceState Saved state if the fragment is being recreated.
     * @return The inflated splash screen view.
     */
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }
    /**
     * Initializes the splash screen logic by starting a fade-in animation on the logo
     * and scheduling a delayed automatic login check. After a short delay, it calls
     * {@link #runAutoLogin(NavController)} to determine the correct navigation path.
     *
     * @param view               The root view of the fragment.
     * @param savedInstanceState Previously saved state, if any.
     */
    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        ImageView logo = view.findViewById(R.id.splashLogo);

        // Trying to make it look less clunky
        logo.setAlpha(0f);
        logo.animate()
                .alpha(1f)
                .setDuration(500L)
                .start();

        // autologin check while query runs
        new Handler(Looper.getMainLooper())
                .postDelayed(() -> runAutoLogin(navController), SPLASH_DELAY_MS);
    }
    /**
     * Attempts to automatically log the user in based on the device's Android ID.
     * The method queries Firestore through {@link DbManager#findUserByDeviceId(String)}
     * to locate a user who enabled the "remember me" option.
     *
     * <p>If a matching user is found, their user document is retrieved and passed to
     * {@link #handleUserDocForAutoLogin(NavController, DocumentSnapshot)}. Otherwise,
     * the user is safely navigated to the login screen.</p>
     *
     * @param navController The NavController used to perform navigation.
     */
    private void runAutoLogin(NavController navController) {
        if (!isAdded()) return;

        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        DbManager dbManager = DbManager.getInstance();

        dbManager.findUserByDeviceId(deviceId)
                .addOnSuccessListener(userId -> {
                    if (!isAdded()) return;

                    if (userId == null) {
                        safeNavigateToLogin(navController);
                        return;
                    }

                    dbManager.getDb()
                            .collection("users")
                            .document(userId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                if (!isAdded()) return;
                                handleUserDocForAutoLogin(navController, doc);
                            })
                            .addOnFailureListener(e -> {
                                if (!isAdded()) return;
                                safeNavigateToLogin(navController);
                            });
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    safeNavigateToLogin(navController);
                });
    }

    /**
     * Processes a Firestore user document retrieved during auto-login. Determines whether
     * the user should be auto-logged in based on their "rememberMe" and "isAdmin" flags.
     *
     * <p>If the user is eligible for auto-login, their {@link User} object is constructed
     * and stored globally in {@link MyApp}. Then the user is navigated to the entrant
     * landing screen. Otherwise, they are navigated to the login screen.</p>
     *
     * @param navController The NavController used for navigation.
     * @param doc           The Firestore document representing the user.
     */
    private void handleUserDocForAutoLogin(
            NavController navController,
            DocumentSnapshot doc
    ) {
        if (doc == null || !doc.exists()) {
            safeNavigateToLogin(navController);
            return;
        }

        Boolean rememberMe = doc.getBoolean("rememberMe");
        Boolean isAdmin = doc.getBoolean("isAdmin");

        boolean remember = rememberMe != null && rememberMe;
        boolean admin = isAdmin != null && isAdmin;

        // Admins never auto-login
        if (!remember || admin) {
            safeNavigateToLogin(navController);
            return;
        }

        // Build user and store globally in Myapp for access
        User user = User.from(doc);
        MyApp.getInstance().setCurrentUser(user);

        safeNavigateToEntrantLanding(navController);
    }
    /**
     * Safely navigates the user to the login screen. Any navigation exceptions are caught
     * and ignored to prevent crashes if called when the fragment is no longer active.
     *
     * @param navController The NavController used to perform navigation.
     */
    private void safeNavigateToLogin(NavController navController) {
        try {
            navController.navigate(R.id.loginFragment);
        } catch (Exception ignored) {}
    }

    /**
     * Safely navigates the user to the entrant landing screen. Exceptions during
     * navigation are caught and ignored to prevent crashes during fragment transitions.
     *
     * @param navController The NavController used for navigation.
     */
    private void safeNavigateToEntrantLanding(NavController navController) {
        try {
            navController.navigate(R.id.entrantLandingFragment3);
        } catch (Exception ignored) {}
    }
}
