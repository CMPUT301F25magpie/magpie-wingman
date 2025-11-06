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
import com.example.magpie_wingman.data.LotteryFunction;
import com.google.android.material.appbar.MaterialToolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class OrganizerLotteryFragment extends Fragment {

    private EditText sampleInput;
    private Button selectButton;
    private String eventId;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizer_lottery, container, false);
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar_lottery);
        NavController navController = NavHostFragment.findNavController(this);
        toolbar.setNavigationOnClickListener(v -> {
            navController.navigateUp();
        });

        sampleInput = view.findViewById(R.id.lottery_sample_input);
        selectButton = view.findViewById(R.id.lottery_select_button);

        // TODO: Replace w/ however the app provides eventId
        eventId = getArguments() != null ? getArguments().getString("eventId") : "testEvent1234";

        selectButton.setOnClickListener(v -> runLottery());

        return view;
    }

    private void runLottery() {
        String input = sampleInput.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a number", Toast.LENGTH_SHORT).show();
            return;
        }

        int sampleCount;
        try {
            sampleCount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call the backend logic
        LotteryFunction.sampleEntrantsForEvent(eventId, sampleCount)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(),
                        "Successfully sampled " + sampleCount + " users!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}