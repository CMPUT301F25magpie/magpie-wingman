package com.example.magpie_wingman.entrant;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.example.magpie_wingman.R;

public class EntrantSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}