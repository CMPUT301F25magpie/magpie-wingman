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

public class SplashFragment extends Fragment {

    private static final long SPLASH_DELAY_MS = 800L; // short delay for logo

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);
        ImageView logo = view.findViewById(R.id.splashLogo);

        // Fade animation
        logo.setAlpha(0f);
        logo.animate()
                .alpha(1f)
                .setDuration(500L)
                .start();

        // Delay → then auto login check
        new Handler(Looper.getMainLooper())
                .postDelayed(() -> runAutoLogin(navController), SPLASH_DELAY_MS);
    }

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

        // Build user and store globally
        User user = User.from(doc);
        MyApp.getInstance().setCurrentUser(user);

        safeNavigateToEntrantLanding(navController);
    }

    private void safeNavigateToLogin(NavController navController) {
        try {
            navController.navigate(R.id.loginFragment);
        } catch (Exception ignored) {}
    }

    private void safeNavigateToEntrantLanding(NavController navController) {
        try {
            navController.navigate(R.id.entrantLandingFragment3);
        } catch (Exception ignored) {}
    }
}
