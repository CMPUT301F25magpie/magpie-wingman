package com.example.magpie_wingman.organizer;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.magpie_wingman.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrganizerLandingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrganizerLandingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // UI elements
    private EditText eventSearchBar;

    public OrganizerLandingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrganizerLandingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OrganizerLandingFragment newInstance(String param1, String param2) {
        OrganizerLandingFragment fragment = new OrganizerLandingFragment();
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

        return inflater.inflate(R.layout.fragment_organizer_landing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        NavController navController = Navigation.findNavController(v);

        // navigation
        Button btn_CreateEvent = v.findViewById(R.id.button_new_event);
        ImageButton btn_Settings = v.findViewById((R.id.button_settings));

        btn_CreateEvent.setOnClickListener(x -> navController.navigate(R.id.action_organizerLandingFragment2_to_organizerNewEventFragment));
        btn_Settings.setOnClickListener( x -> navController.navigate(R.id.action_organizerLandingFragment2_to_organizerSettingsFragment));



    }
}