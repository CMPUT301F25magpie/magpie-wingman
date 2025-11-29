package com.example.magpie_wingman.organizer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows an organizer to edit an existing event.
 * Handles loading data, updating fields (including Capacity/Limit), and saving changes.
 */
public class OrganizerEditEventFragment extends Fragment {

    // UI components
    private EditText eventTitleField;
    private EditText eventLimitField;    // Waitlist Limit
    private EditText eventCapacityField; // Max Attendees (Capacity)
    private CheckBox geoCheckBox;
    private CheckBox qrCheckBox;
    private Button uploadPosterButton;

    private EditText eventAddressField;
    private EditText eventCityField;
    private EditText eventProvinceField;
    private EditText eventDescriptionField;

    private EditText eventDateField;
    private EditText eventTimeField;
    private EditText regStartDateField;
    private EditText regEndDateField;

    private Button resetButton;
    private Button saveButton;


    private final Calendar eventCalendar = Calendar.getInstance();
    private final Calendar regStartCalendar = Calendar.getInstance();
    private final Calendar regEndCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private Uri posterImageUri;
    private String existingPosterUrl;
    private ActivityResultLauncher<String> pickImageLauncher;
    private String eventId;

    public OrganizerEditEventFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        posterImageUri = uri;
                        if (uploadPosterButton != null) {
                            uploadPosterButton.setText("Poster selected");
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_edit_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // prevent null pointer
        ImageButton backBtn = view.findViewById(R.id.button_back);
        if (backBtn != null) backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        ImageButton infoBtn = view.findViewById(R.id.button_info);
        if (infoBtn != null) {
            infoBtn.setOnClickListener(v -> Toast.makeText(getContext(), "Edit details and tap SAVE.", Toast.LENGTH_SHORT).show());
        }


        eventTitleField      = view.findViewById(R.id.edit_event_title);
        eventLimitField      = view.findViewById(R.id.edit_limit);
        eventCapacityField   = view.findViewById(R.id.edit_capacity);

        geoCheckBox          = view.findViewById(R.id.checkbox_geo);
        qrCheckBox           = view.findViewById(R.id.checkbox_qr);


        uploadPosterButton   = view.findViewById(R.id.button_upload_poster);
        resetButton          = view.findViewById(R.id.button_reset);


        saveButton = view.findViewById(R.id.button_save);
        if (saveButton == null) {
            saveButton = view.findViewById(R.id.button_create);
        }

        eventAddressField    = view.findViewById(R.id.edit_address);
        eventCityField       = view.findViewById(R.id.edit_city);
        eventProvinceField   = view.findViewById(R.id.edit_province);
        eventDescriptionField= view.findViewById(R.id.edit_description);

        eventDateField       = view.findViewById(R.id.edit_date);
        eventTimeField       = view.findViewById(R.id.edit_time);
        regStartDateField    = view.findViewById(R.id.edit_registration_start);
        regEndDateField      = view.findViewById(R.id.edit_registration_end);


        setupPickers();

        if (uploadPosterButton != null) {
            uploadPosterButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        }

        if (saveButton != null) {
            saveButton.setText("SAVE");
            saveButton.setOnClickListener(v -> saveChanges());
        }

        if (resetButton != null) {
            resetButton.setOnClickListener(v -> loadEvent(eventId));
        }

        // --- 3. LOAD DATA ---
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(getContext(), "Missing eventId", Toast.LENGTH_LONG).show();
            return;
        }

        loadEvent(eventId);
    }

    private void loadEvent(String eventId) {
        DbManager.getInstance().getDb().collection("events").document(eventId).get()
                .addOnSuccessListener(this::applyEventSnapshot)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to load event", Toast.LENGTH_SHORT).show());
    }

    private void applyEventSnapshot(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        if (eventTitleField != null) eventTitleField.setText(doc.getString("eventName"));
        if (eventDescriptionField != null) eventDescriptionField.setText(doc.getString("description"));

        Long capacity = doc.getLong("eventCapacity");
        if (eventCapacityField != null) eventCapacityField.setText(capacity != null ? String.valueOf(capacity) : "");

        Long limit = doc.getLong("waitingListLimit");
        if (eventLimitField != null) eventLimitField.setText(limit != null ? String.valueOf(limit) : "");

        // Location splitting logic...
        String location = doc.getString("eventLocation");
        if (location != null && eventAddressField != null) {
            eventAddressField.setText(location);
        }

        if (qrCheckBox != null) {
            String qrHash = doc.getString("qrCodeHash");
            qrCheckBox.setChecked(qrHash != null && !qrHash.isEmpty());
        }

        Boolean geoRequired = doc.getBoolean("geolocationRequired");
        if (geoCheckBox != null) {
            geoCheckBox.setChecked(geoRequired != null && geoRequired);
        }

        // Dates
        setCalendarFromDoc(doc, "eventStartTime", eventCalendar, eventDateField, eventTimeField);
        setCalendarFromDoc(doc, "registrationStart", regStartCalendar, regStartDateField, null);
        setCalendarFromDoc(doc, "registrationEnd", regEndCalendar, regEndDateField, null);
    }

    private void setCalendarFromDoc(DocumentSnapshot doc, String field, Calendar cal, EditText dateField, EditText timeField) {
        Object obj = doc.get(field);
        Date date = null;
        if (obj instanceof Timestamp) date = ((Timestamp) obj).toDate();
        else if (obj instanceof Date) date = (Date) obj;

        if (date != null) {
            cal.setTime(date);
            if (dateField != null) dateField.setText(dateFmt.format(date));
            if (timeField != null) timeField.setText(timeFmt.format(date));
        }
    }

    private void saveChanges() {
        if (TextUtils.isEmpty(eventId)) return;

        String title = (eventTitleField != null) ? eventTitleField.getText().toString().trim() : "";
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }


        int capacity = 0;
        if (eventCapacityField != null) {
            String s = eventCapacityField.getText().toString().trim();
            if (!s.isEmpty()) capacity = Integer.parseInt(s);
        }

        int limit = 0;
        if (eventLimitField != null) {
            String s = eventLimitField.getText().toString().trim();
            if (!s.isEmpty()) limit = Integer.parseInt(s);
        }

        String desc = (eventDescriptionField != null) ? eventDescriptionField.getText().toString().trim() : "";
        String location = (eventAddressField != null) ? eventAddressField.getText().toString().trim() : "";

        if (saveButton != null) {
            saveButton.setEnabled(false);
            saveButton.setText("Saving...");
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("eventName", title);
        updates.put("description", desc);
        updates.put("eventLocation", location);
        updates.put("eventCapacity", capacity);
        updates.put("waitingListLimit", limit);
        updates.put("eventStartTime", eventCalendar.getTime());
        updates.put("registrationStart", regStartCalendar.getTime());
        updates.put("registrationEnd", regEndCalendar.getTime());

        if (qrCheckBox != null) {
            updates.put("qrCodeHash", qrCheckBox.isChecked() ? eventId : null);
        }
        if (geoCheckBox != null) {
            updates.put("geolocationRequired", geoCheckBox.isChecked());
        }

        DbManager.getInstance().getDb().collection("events").document(eventId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (posterImageUri != null) {
                        DbManager.getInstance().uploadEventPoster(eventId, posterImageUri);
                    }
                    Toast.makeText(getContext(), "Event updated", Toast.LENGTH_SHORT).show();
                    if (getView() != null) Navigation.findNavController(getView()).navigateUp();
                })
                .addOnFailureListener(e -> {
                    if (saveButton != null) {
                        saveButton.setEnabled(true);
                        saveButton.setText("SAVE");
                    }
                    Toast.makeText(getContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                });
    }

    private void setupPickers() {
        setupDatePicker(eventDateField, eventCalendar);
        setupTimePicker(eventTimeField, eventCalendar);
        setupDatePicker(regStartDateField, regStartCalendar);
        setupDatePicker(regEndDateField, regEndCalendar);
    }

    private void setupDatePicker(EditText et, Calendar cal) {
        if (et == null) return;
        et.setFocusable(false);
        et.setClickable(true);
        et.setOnClickListener(v -> new DatePickerDialog(getContext(), (view, y, m, d) -> {
            cal.set(Calendar.YEAR, y);
            cal.set(Calendar.MONTH, m);
            cal.set(Calendar.DAY_OF_MONTH, d);
            et.setText(dateFmt.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void setupTimePicker(EditText et, Calendar cal) {
        if (et == null) return;
        et.setFocusable(false);
        et.setClickable(true);
        et.setOnClickListener(v -> new TimePickerDialog(getContext(), (view, h, m) -> {
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, m);
            et.setText(timeFmt.format(cal.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show());
    }
}