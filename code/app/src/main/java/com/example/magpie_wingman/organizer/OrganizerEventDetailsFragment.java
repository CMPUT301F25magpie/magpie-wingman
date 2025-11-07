package com.example.magpie_wingman.organizer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.magpie_wingman.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OrganizerEventDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrganizerEventDetailsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    private String eventId;

    public OrganizerEventDetailsFragment() { }

    public static OrganizerEventDetailsFragment newInstance(String eventId) {
        OrganizerEventDetailsFragment fragment = new OrganizerEventDetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_event_details, container, false);
    }
}