package com.example.magpie_wingman.ui.login;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SignUpFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        EditText firstNameField = view.findViewById(R.id.firstName);
        EditText lastNameField  = view.findViewById(R.id.lastName);
        EditText emailField     = view.findViewById(R.id.email);
        EditText dobField       = view.findViewById(R.id.dateOfBirth);
        EditText phoneField     = view.findViewById(R.id.phoneNumber);
        EditText passField      = view.findViewById(R.id.password);

        Button registerButton   = view.findViewById(R.id.registerButton);
        TextView signInText     = view.findViewById(R.id.signInText);

        // Navigate back to Login
        signInText.setOnClickListener(v ->
                navController.navigate(R.id.action_signUpFragment_to_loginFragment));

        registerButton.setOnClickListener(v -> {
            String firstName = firstNameField.getText().toString().trim();
            String lastName  = lastNameField.getText().toString().trim();
            String email     = emailField.getText().toString().trim();
            String dobText   = dobField.getText().toString().trim();
            String phone     = phoneField.getText().toString().trim();
            String password  = passField.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse DOB if provided
            Date dob = null;
            if (!TextUtils.isEmpty(dobText)) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                sdf.setLenient(false);
                try {
                    dob = sdf.parse(dobText);
                } catch (ParseException e) {
                    Toast.makeText(requireContext(),
                            "Please enter DOB as DD/MM/YYYY",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }


            final Date finalDob = dob;
            String fullName = firstName + " " + lastName;

            registerButton.setEnabled(false);

            DbManager db = DbManager.getInstance();

            // Check email uniqueness
            db.isEmailInUse(email)
                    .addOnSuccessListener(inUse -> {
                        if (inUse) {
                            Toast.makeText(requireContext(),
                                    "Email already in use",
                                    Toast.LENGTH_SHORT).show();
                            registerButton.setEnabled(true);
                            return;
                        }

                        // Email is free then create user
                        db.createUser(fullName, email, phone, password)
                                .addOnSuccessListener(unused -> {

                                    // Add DOB if provided
                                    if (finalDob != null) {
                                        db.updateDOBByEmail(email, finalDob)
                                                .addOnSuccessListener(unused2 -> {
                                                    Toast.makeText(requireContext(),
                                                            "Account created!",
                                                            Toast.LENGTH_SHORT).show();
                                                    navController.navigate(
                                                            R.id.action_signUpFragment_to_loginFragment);
                                                    registerButton.setEnabled(true);
                                                });
                                    } else {
                                        Toast.makeText(requireContext(),
                                                "Account created!",
                                                Toast.LENGTH_SHORT).show();
                                        navController.navigate(
                                                R.id.action_signUpFragment_to_loginFragment);
                                        registerButton.setEnabled(true);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(),
                                            "Error creating user",
                                            Toast.LENGTH_SHORT).show();
                                    registerButton.setEnabled(true);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(),
                                "Error checking email",
                                Toast.LENGTH_SHORT).show();
                        registerButton.setEnabled(true);
                    });

        }); // end click listener
    } // end onViewCreated
}
