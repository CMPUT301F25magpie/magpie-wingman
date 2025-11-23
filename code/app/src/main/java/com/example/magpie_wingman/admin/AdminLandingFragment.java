package com.example.magpie_wingman.admin;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.magpie_wingman.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminLandingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminLandingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public AdminLandingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminLandingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminLandingFragment newInstance(String param1, String param2) {
        AdminLandingFragment fragment = new AdminLandingFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_landing, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View v,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);


        NavController navController = Navigation.findNavController(v);
        Button btn_AdminEvents = v.findViewById(R.id.button_events);
        Button btn_AdminProfiles = v.findViewById(R.id.button_profiles);
        Button btn_AdminImages = v.findViewById(R.id.button_images);
        Button btn_AdminNotifications = v.findViewById(R.id.button_notifications);
        ImageButton btn_AdminSettings = v.findViewById(R.id.button_settings);

        btn_AdminEvents.setOnClickListener( x -> navController.navigate(R.id.action_adminLandingFragment2_to_adminEventsFragment));
        btn_AdminProfiles.setOnClickListener( x -> navController.navigate(R.id.action_adminLandingFragment2_to_adminProfilesFragment));
        btn_AdminImages.setOnClickListener( x -> navController.navigate(R.id.action_adminLandingFragment2_to_adminImagesFragment));
        btn_AdminNotifications.setOnClickListener(x -> navController.navigate(R.id.action_adminLandingFragment2_to_adminNotificationLogFragment));
        btn_AdminSettings.setOnClickListener(x -> navController.navigate(R.id.action_adminLandingFragment2_to_adminSettingsFragment));


    }
}