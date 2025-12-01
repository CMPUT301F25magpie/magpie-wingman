package com.example.magpie_wingman.ui.login;

import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.google.firebase.firestore.FirebaseFirestore;
/**
 * Fragment responsible for handling user login via email and password.
 * Displays the login UI, validates user input, attempts authentication
 * through {@link DbManager}, and navigates to the appropriate landing
 * screen based on the user's role (entrant or admin).
 *
 * <p>This fragment also supports a "Remember Me" feature, storing the
 * device's Android ID and preference flag for future automatic login.</p>
 */
public class LoginFragment extends Fragment {


    /**
     * Inflates the login screen layout for this fragment.
     *
     * @param inflater  The LayoutInflater used to inflate views in the fragment.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Saved state for the fragment, if available.
     * @return The inflated login fragment view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    /**
     * Called after the fragment's view has been created. Initializes UI elements,
     * configures navigation to the sign-up screen, and sets up the login button
     * logic. Handles authentication flow, admin vs entrant navigation, and
     * updates the user's remember-me and device ID settings in Firestore.
     *
     * @param view               The root view returned by {@link #onCreateView}.
     * @param savedInstanceState Saved state for the fragment, if available.
     */
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        // UI references
        EditText emailInput    = view.findViewById(R.id.username);   // email field
        EditText passwordInput = view.findViewById(R.id.password);
        CheckBox rememberMe    = view.findViewById(R.id.rememberMe);
        Button loginButton     = view.findViewById(R.id.login);

        TextView btnSignUpText = view.findViewById(R.id.signUpText);

        DbManager dbManager = DbManager.getInstance();
        FirebaseFirestore db = dbManager.getDb();

        // Sign up nav
        btnSignUpText.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_signUpFragment));

        // email/password login using DbManager.loginWithEmailAndPassword
        loginButton.setOnClickListener(v -> {
            String email    = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(requireContext(),
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            loginButton.setEnabled(false);

            dbManager.loginWithEmailAndPassword(email, password)
                    .addOnSuccessListener(user -> {
                        // We have a valid User object
                        MyApp.getInstance().setCurrentUser(user);
                        String userId = user.getUserId();

                        // get user document to check isAdmin and use rememberMe/deviceId
                        db.collection("users")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    loginButton.setEnabled(true);

                                    if (doc == null || !doc.exists()) {
                                        // Fallback: treat as normal entrant
                                        navController.navigate(
                                                R.id.action_loginFragment_to_entrantLandingFragment3
                                        );
                                        return;
                                    }

                                    Boolean adminField = doc.getBoolean("isAdmin");
                                    boolean isAdmin = adminField != null && adminField;

                                    // Current device ID
                                    String deviceId = Settings.Secure.getString(
                                            requireContext().getContentResolver(),
                                            Settings.Secure.ANDROID_ID
                                    );

                                    if (isAdmin) {
                                        // Admins never auto-login for safety, must input employee credentials
                                        // that's admin@magpie.com and Admin123
                                        dbManager.updateRememberMe(userId, false);

                                        db.collection("users")
                                                .document(userId)
                                                .update("deviceId", deviceId);

                                        navController.navigate(
                                                R.id.action_loginFragment_to_adminLandingFragment22
                                        );
                                    } else {

                                        boolean remember = rememberMe.isChecked();


                                        dbManager.updateRememberMe(userId, remember);


                                        db.collection("users")
                                                .document(userId)
                                                .update("deviceId", deviceId);

                                        navController.navigate(
                                                R.id.action_loginFragment_to_entrantLandingFragment3
                                        );
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    loginButton.setEnabled(true);

                                    Toast.makeText(requireContext(),
                                            "Login failed. Please try again.",
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        loginButton.setEnabled(true);
                        Toast.makeText(requireContext(),
                                        "Invalid login credentials",
                                        Toast.LENGTH_SHORT)
                                .show();
                    });
        });

    }
}
