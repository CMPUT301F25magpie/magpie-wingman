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

public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        // ---- UI references from fragment_login.xml ----
        EditText emailInput    = view.findViewById(R.id.username);   // email field
        EditText passwordInput = view.findViewById(R.id.password);
        CheckBox rememberMe    = view.findViewById(R.id.rememberMe);
        Button loginButton     = view.findViewById(R.id.login);

        Button btnEntrant      = view.findViewById(R.id.btn_test_entrant);
        Button btnOrganizer    = view.findViewById(R.id.btn_test_organizer);
        Button btnAdmin        = view.findViewById(R.id.btn_test_admin);
        TextView btnSignUpText = view.findViewById(R.id.signUpText);

        DbManager dbManager = DbManager.getInstance();
        FirebaseFirestore db = dbManager.getDb();

        // ---- Sign up navigation ----
        btnSignUpText.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_signUpFragment));

        // ---- REAL email/password login using DbManager.loginWithEmailAndPassword ----
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

                        // Now fetch the user document to check isAdmin and handle rememberMe/deviceId
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
                                        // Admins: never auto-login for safety
                                        dbManager.updateRememberMe(userId, false);
                                        // (Optional) still keep deviceId current, but it won't be used for auto-login
                                        db.collection("users")
                                                .document(userId)
                                                .update("deviceId", deviceId);

                                        navController.navigate(
                                                R.id.action_loginFragment_to_adminLandingFragment22
                                        );
                                    } else {
                                        // Normal user/organizer: Option 2 behavior
                                        boolean remember = rememberMe.isChecked();

                                        // Use helper for rememberMe
                                        dbManager.updateRememberMe(userId, remember);

                                        // Tie auto-login to THIS device
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
                                    // If doc fetch fails, just show an error and stay on login
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

        // ---- Bottom test buttons (unchanged) ----
        btnEntrant.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_entrantLandingFragment3));

        btnOrganizer.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_organizerLandingFragment2));

        btnAdmin.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_adminLandingFragment22));
    }
}
