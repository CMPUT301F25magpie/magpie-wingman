package com.example.magpie_wingman.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.magpie_wingman.R;

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

        Button btnEntrant = view.findViewById(R.id.btn_test_entrant);
        Button btnOrganizer = view.findViewById(R.id.btn_test_organizer);
        Button btnAdmin = view.findViewById(R.id.btn_test_admin);

        btnEntrant.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_entrantLandingFragment3));

        btnOrganizer.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_organizerLandingFragment2));

        btnAdmin.setOnClickListener(v ->
                navController.navigate(R.id.action_loginFragment_to_adminLandingFragment22));
    }
}