package com.example.magpie_wingman.ui.login;

import android.os.Bundle;
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
import com.example.magpie_wingman.data.model.User;
import com.google.android.gms.tasks.Task;

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

        // ---- Sign up navigation (already existed) ----
        btnSignUpText.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_signUpFragment));

        // ---- REAL email/password login ----
        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(requireContext(),
                        "Please enter email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Optionally you can show a loading spinner, but at least disable the button
            loginButton.setEnabled(false);

            Task<User> task = DbManager.getInstance()
                    .loginWithEmailAndPassword(email, password);

            task.addOnSuccessListener(user -> {
                // Save globally
                MyApp.getInstance().setCurrentUser(user);

                // TODO (later): if (rememberMe.isChecked()) { persist rememberMe + deviceId }

                // Navigate to entrant landing fragment
                navController.navigate(
                        R.id.action_loginFragment_to_entrantLandingFragment3
                );

                loginButton.setEnabled(true);
            }).addOnFailureListener(e -> {
                Toast.makeText(requireContext(),
                                "Invalid login credentials",
                                Toast.LENGTH_SHORT)
                        .show();
                // For debugging if needed:
                // Log.d("LOGIN", "Login failed: " + e.getMessage());

                loginButton.setEnabled(true);
            });
        });

        // ---- Bottom test buttons (kept as-is) ----
        btnEntrant.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_entrantLandingFragment3));

        btnOrganizer.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_organizerLandingFragment2));

        btnAdmin.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_adminLandingFragment22));
    }
}