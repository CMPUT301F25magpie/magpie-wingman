package com.example.magpie_wingman.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class OrganizerNewEventFragment extends Fragment {

    // UI Components
    private TextInputEditText eventTitleField;
    private TextInputEditText eventAddressField;
    private TextInputEditText eventDescriptionField;

    // Registration Date/Time fields
    private TextInputEditText regStartDateField;
    private TextInputEditText regStartTimeField;
    private TextInputEditText regEndDateField;
    private TextInputEditText regEndTimeField;

    private Button createButton;

    // Date/Time holders
    private Calendar regStartCalendar = Calendar.getInstance();
    private Calendar regEndCalendar = Calendar.getInstance();

    public OrganizerNewEventFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_organizer_new_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find all the views
        eventTitleField = view.findViewById(R.id.edit_text_event_title);
        eventAddressField = view.findViewById(R.id.edit_text_location_address);
        eventDescriptionField = view.findViewById(R.id.edit_text_event_description);

        regStartDateField = view.findViewById(R.id.text_reg_start_date);
        regStartTimeField = view.findViewById(R.id.text_reg_start_time);
        regEndDateField = view.findViewById(R.id.text_reg_end_date);
        regEndTimeField = view.findViewById(R.id.text_reg_end_time);

        createButton = view.findViewById(R.id.button_create);

        // Set click listeners for the registration fields
        regStartDateField.setOnClickListener(v ->
                showDatePicker(regStartDateField, regStartCalendar)
        );
        regStartTimeField.setOnClickListener(v ->
                showTimePicker(regStartTimeField, regStartCalendar)
        );
        regEndDateField.setOnClickListener(v ->
                showDatePicker(regEndDateField, regEndCalendar)
        );
        regEndTimeField.setOnClickListener(v ->
                showTimePicker(regEndTimeField, regEndCalendar)
        );

        createButton.setOnClickListener(v -> {
            // Here is where you would get all the data and save to Firebase
            String eventName = eventTitleField.getText().toString();

            // Example:
            Toast.makeText(getContext(), "Creating event: " + eventName, Toast.LENGTH_SHORT).show();

            // You would get the final timestamps:
            // long startTime = regStartCalendar.getTimeInMillis();
            // long endTime = regEndCalendar.getTimeInMillis();
            // ...and then save them.
        });
    }

    /**
     * Shows a DatePicker dialog and updates the provided EditText.
     * @param dateField The EditText to display the formatted date.
     * @param calendar  The Calendar instance to store the selected date.
     */
    private void showDatePicker(final TextInputEditText dateField, final Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(getContext(), (datePicker, y, m, d) -> {
            calendar.set(Calendar.YEAR, y);
            calendar.set(Calendar.MONTH, m);
            calendar.set(Calendar.DAY_OF_MONTH, d);

            // Format date as "yyyy-MM-dd" to match mockup
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            dateField.setText(sdf.format(calendar.getTime()));
        }, year, month, day).show();
    }

    /**
     * Shows a TimePicker dialog and updates the provided EditText.
     * @param timeField The EditText to display the formatted time.
     * @param calendar  The Calendar instance to store the selected time.
     */
    private void showTimePicker(final TextInputEditText timeField, final Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        new TimePickerDialog(getContext(), (timePicker, h, m) -> {
            calendar.set(Calendar.HOUR_OF_DAY, h);
            calendar.set(Calendar.MINUTE, m);

            // Format time as "HH:mm" (24-hour) to match mockup
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timeField.setText(sdf.format(calendar.getTime()));
        }, hour, minute, true).show(); // true for 24-hour format
    }
}