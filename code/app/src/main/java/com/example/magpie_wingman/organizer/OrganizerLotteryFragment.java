package com.example.magpie_wingman.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.magpie_wingman.R;

public class OrganizerLotteryFragment extends Fragment {
    private EditText enterMaximum;
    private Button buttonSample;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_lottery, container, false);
        enterMaximum = view.findViewById(R.id.inputSampleSize);
        buttonSample = view.findViewById(R.id.buttonSampleEntrants);
        String eventId = getArguments() != null ? getArguments().getString("EVENT_ID") : "testEvent123";

        buttonSample.setOnClickListener(v -> {
            String input = enterMaximum.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(getContext(), "Enter number to sample", Toast.LENGTH_SHORT).show();
                return;
            }

            int sampleSize = Integer.parseInt(input);

            // call backend method here (replace with actual function name)
            try {
                // example placeholder until confirmed
                // LotteryHelper.createRegistrableUsers(eventId, sampleSize);
                Toast.makeText(getContext(), "Sampling started for " + sampleSize + " users", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }
}