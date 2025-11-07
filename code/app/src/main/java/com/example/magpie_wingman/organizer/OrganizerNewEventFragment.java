package com.example.magpie_wingman.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors; // Import Executors

public class OrganizerNewEventFragment extends Fragment {

    // UI
    private TextInputEditText eventTitleField;
    private TextInputEditText eventAddressField;
    private TextInputEditText eventDescriptionField;
    private TextInputEditText regStartDateField;
    private TextInputEditText regStartTimeField;
    private TextInputEditText regEndDateField;
    private TextInputEditText regEndTimeField;
    private Button createButton;

    // Date/Time holders
    private Calendar regStartCalendar = Calendar.getInstance();
    private Calendar regEndCalendar = Calendar.getInstance();

    private DbManager dbManager;

    public OrganizerNewEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_new_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            dbManager = DbManager.getInstance();
        } catch (IllegalStateException e) {
            if (getContext() != null) {
                DbManager.init(getContext().getApplicationContext());
                dbManager = DbManager.getInstance();
            }
        }

        // Find views
        eventTitleField = view.findViewById(R.id.edit_text_event_title);
        eventAddressField = view.findViewById(R.id.edit_text_location_address);
        eventDescriptionField = view.findViewById(R.id.edit_text_event_description);
        regStartDateField = view.findViewById(R.id.text_reg_start_date);
        regStartTimeField = view.findViewById(R.id.text_reg_start_time);
        regEndDateField = view.findViewById(R.id.text_reg_end_date);
        regEndTimeField = view.findViewById(R.id.text_reg_end_time);
        createButton = view.findViewById(R.id.button_create);

        // Set click listeners
        regStartDateField.setOnClickListener(v -> showDatePicker(regStartDateField, regStartCalendar));
        regStartTimeField.setOnClickListener(v -> showTimePicker(regStartTimeField, regStartCalendar));
        regEndDateField.setOnClickListener(v -> showDatePicker(regEndDateField, regEndCalendar));
        regEndTimeField.setOnClickListener(v -> showTimePicker(regEndTimeField, regEndCalendar));

        createButton.setOnClickListener(v -> {
            createNewEvent();
        });
    }

    private void createNewEvent() {

        if (dbManager == null) {
            Toast.makeText(getContext(), "Database Error. Please restart.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the data from the fields
        String eventName = eventTitleField.getText().toString().trim();
        String description = eventDescriptionField.getText().toString().trim();
        Date regStartDate = regStartCalendar.getTime();
        Date regEndDate = regEndCalendar.getTime();
        String mockOrganizerId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        if (TextUtils.isEmpty(eventName) || TextUtils.isEmpty(description)) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (regStartDate.equals(regEndDate)) {
            Toast.makeText(getContext(), "Please set registration dates", Toast.LENGTH_SHORT).show();
            return;
        }

        createButton.setEnabled(false);
        createButton.setText("Creating...");



        Executors.newSingleThreadExecutor().execute(() -> {
            dbManager.createEvent(eventName, description, mockOrganizerId, regStartDate, regEndDate)
                    .addOnSuccessListener(aVoid -> {
                        // Success! Post result back to the UI thread.
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Event Created!", Toast.LENGTH_SHORT).show();
                            });
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Failure! Post result back to the UI thread.
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Log.e("NewEventFragment", "Failed to create event", e);
                                Toast.makeText(getContext(), "Error: Could not create event", Toast.LENGTH_LONG).show();
                                createButton.setEnabled(true);
                                createButton.setText("Create");
                            });
                        }
                    });
        });

    }

    private void showDatePicker(final TextInputEditText dateField, final Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (datePicker, y, m, d) -> {
            calendar.set(Calendar.YEAR, y);
            calendar.set(Calendar.MONTH, m);
            calendar.set(Calendar.DAY_OF_MONTH, d);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateField.setText(sdf.format(calendar.getTime()));
        }, year, month, day).show();
    }

    private void showTimePicker(final TextInputEditText timeField, final Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(requireContext(), (timePicker, h, m) -> {
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeField.setText(sdf.format(calendar.getTime()));
        }, hour, minute, true).show();
    }
}