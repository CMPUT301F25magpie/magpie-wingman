package com.example.magpie_wingman.organizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.NotificationFunction;

/**
 * US 02.05.01, US 02.07.01, US 02.07.02 and US 02.07.03 (first three done)
 */
public class OrganizerNotifyFragment extends Fragment {

    private EditText titleInput, messageInput;
    private CheckBox waitlistCheck, registrableCheck, cancelledCheck;
    private String eventId;

    private final NotificationFunction notificationFunction = new NotificationFunction();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_organizer_notify, container, false);

        NavController navController = NavHostFragment.findNavController(this);
        View toolbar = view.findViewById(R.id.button_back);
        if (toolbar != null) toolbar.setOnClickListener(v -> navController.navigateUp());

        titleInput = view.findViewById(R.id.edit_notification_title);
        messageInput = view.findViewById(R.id.edit_notification_message);
        waitlistCheck = view.findViewById(R.id.checkbox_waiting);
        registrableCheck = view.findViewById(R.id.checkbox_selected);
        cancelledCheck = view.findViewById(R.id.checkbox_cancelled);
        Button sendButton = view.findViewById(R.id.button_send_notification);

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        sendButton.setOnClickListener(v -> sendNotifications());
        return view;
    }

    private void sendNotifications() {
        String title = titleInput.getText().toString().trim();
        String message = messageInput.getText().toString().trim();

        if (title.isEmpty() || message.isEmpty()) {
            Toast.makeText(getContext(),
                    R.string.error_empty_title_or_message,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!waitlistCheck.isChecked() && !registrableCheck.isChecked() && !cancelledCheck.isChecked()) {
            Toast.makeText(getContext(),
                    R.string.error_no_group_selected,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (waitlistCheck.isChecked()) {
            notifyGroup("waitlist", title, message);
        }

        if (registrableCheck.isChecked()) {
            notifyGroup("registrable", title, message);
        }

        if (cancelledCheck.isChecked()) {
            notifyGroup("cancelled", title, message);
        }
    }

    private void notifyGroup(String subcollection, String title, String message) {
        String fullMessage = title + ": " + message;

        notificationFunction.notifyEntrants(eventId, subcollection, fullMessage)
                .addOnSuccessListener(aVoid -> Toast.makeText(
                        getContext(),
                        getString(R.string.msg_notification_sent, subcollection),
                        Toast.LENGTH_SHORT
                ).show())
                .addOnFailureListener(e -> Toast.makeText(
                        getContext(),
                        getString(R.string.error_notification_failed, subcollection, e.getMessage()),
                        Toast.LENGTH_LONG
                ).show());
    }
}