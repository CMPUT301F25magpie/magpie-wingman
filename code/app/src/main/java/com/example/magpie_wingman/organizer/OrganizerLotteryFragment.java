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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.NotificationFunction;
import com.example.magpie_wingman.data.LotteryFunction;

/**
 * US 02.05.02 andUS 02.05.01
 */
public class OrganizerLotteryFragment extends Fragment {

    private EditText sampleInput;
    private Button selectButton;
    private String eventId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_lottery, container, false);

        sampleInput = view.findViewById(R.id.lottery_sample_input);
        selectButton = view.findViewById(R.id.lottery_select_button);

        // Toolbar back navigation
        NavController navController = NavHostFragment.findNavController(this);
        View toolbar = view.findViewById(R.id.toolbar_lottery);
        if (toolbar != null) toolbar.setOnClickListener(v -> navController.navigateUp());

        // Get eventId from navigation args
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        selectButton.setOnClickListener(v -> runLottery());

        return view;
    }

    private void runLottery() {
        String input = sampleInput.getText().toString().trim();

        if (input.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_empty_number, Toast.LENGTH_SHORT).show();
            return;
        }

        int sampleCount;
        try {
            sampleCount = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), R.string.error_invalid_number, Toast.LENGTH_SHORT).show();
            return;
        }

        if (sampleCount <= 0) {
            Toast.makeText(getContext(), R.string.error_non_positive_number, Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1 – Run the random selection (US 02.05.02)
        LotteryFunction.sampleEntrantsForEvent(eventId, sampleCount)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            getContext(),
                            getString(R.string.msg_sample_success, sampleCount),
                            Toast.LENGTH_SHORT
                    ).show();

                    // Step 2 – Notify winners (US 02.05.01)
                    NotificationFunction notifier = new NotificationFunction();
                    String message = getString(R.string.msg_selected_notification);
                    notifier.notifyEntrants(eventId, "registrable", message)
                            .addOnSuccessListener(unused -> Toast.makeText(
                                    getContext(),
                                    R.string.msg_notify_success,
                                    Toast.LENGTH_SHORT
                            ).show())
                            .addOnFailureListener(e -> Toast.makeText(
                                    getContext(),
                                    getString(R.string.error_notify_failed, e.getMessage()),
                                    Toast.LENGTH_LONG
                            ).show());
                })
                .addOnFailureListener(e -> Toast.makeText(
                        getContext(),
                        getString(R.string.error_lottery_failed, e.getMessage()),
                        Toast.LENGTH_LONG
                ).show());
    }
}
