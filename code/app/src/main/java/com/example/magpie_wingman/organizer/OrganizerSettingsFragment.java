package com.example.magpie_wingman.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceFragmentCompat;

import com.example.magpie_wingman.R;

public class OrganizerSettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_settings, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        ImageView btnAboutUs = view.findViewById(R.id.btn_about_us);
        Button btnLogOut = view.findViewById(R.id.button_logout);

        NavController navController = Navigation.findNavController(view);

        btnEditProfile.setOnClickListener(v -> navController.navigate(R.id.action_organizerSettingsFragment_to_organizerEditProfileFragment2));
        btnAboutUs.setOnClickListener(v -> navController.navigate(R.id.action_organizerSettingsFragment_to_organizerAboutUsFragment));
        btnLogOut.setOnClickListener(v -> navController.navigate(R.id.action_organizerSettingsFragment_to_loginFragment));
    }
}