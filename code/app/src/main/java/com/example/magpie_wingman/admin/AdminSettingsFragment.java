package com.example.magpie_wingman.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceFragmentCompat;

import com.example.magpie_wingman.R;

public class AdminSettingsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_settings, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = Navigation.findNavController(view);

        ImageButton backBtn = view.findViewById(R.id.button_back);
        Button btnLogOut = view.findViewById(R.id.button_logout);
        ImageView btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        btnEditProfile.setOnClickListener(v -> navController.navigate(R.id.action_adminSettingsFragment_to_adminEditProfileFragment));
        btnLogOut.setOnClickListener(v -> navController.navigate(R.id.action_adminSettingsFragment_to_loginFragment));
    }
}