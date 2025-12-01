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
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment responsible for editing an existing event.
 * Loads existing data (including Geo requirement) and updates Firestore.
 */
public class OrganizerEditEventFragment extends Fragment {

    // UI Components
    private EditText eventTitleField, eventCapacityField, eventLimitField;
    private CheckBox geoCheckBox, qrCheckBox;
    private Button uploadPosterButton;

    private EditText eventAddressField, eventCityField, eventProvinceField, eventDescriptionField;
    private EditText eventDateField, eventTimeField, regStartDateField, regEndDateField;

    private Button resetButton, saveButton;

    // Date/Time Helpers
    private final Calendar eventCalendar = Calendar.getInstance();
    private final Calendar regStartCalendar = Calendar.getInstance();
    private final Calendar regEndCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private Uri posterImageUri;
    private String existingPosterUrl;
    private ActivityResultLauncher<String> pickImageLauncher;
    private String eventId;

    public OrganizerEditEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        posterImageUri = uri;
                        if (uploadPosterButton != null) uploadPosterButton.setText("Poster selected");
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

        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        // Bind Views
        eventTitleField = view.findViewById(R.id.edit_event_title);

        // Ensure ID matches XML
        eventCapacityField = view.findViewById(R.id.edit_capacity);
        eventLimitField = view.findViewById(R.id.edit_limit);

        geoCheckBox = view.findViewById(R.id.checkbox_geo);
        qrCheckBox = view.findViewById(R.id.checkbox_qr);
        uploadPosterButton = view.findViewById(R.id.button_upload_poster);

        eventAddressField = view.findViewById(R.id.edit_address);
        eventCityField = view.findViewById(R.id.edit_city);
        eventProvinceField = view.findViewById(R.id.edit_province);
        eventDescriptionField = view.findViewById(R.id.edit_description);

        eventDateField = view.findViewById(R.id.edit_date);
        eventTimeField = view.findViewById(R.id.edit_time);
        regStartDateField = view.findViewById(R.id.edit_registration_start);
        regEndDateField = view.findViewById(R.id.edit_registration_end);

        resetButton = view.findViewById(R.id.button_reset);
        saveButton = view.findViewById(R.id.button_create);
        saveButton.setText("SAVE"); // Re-purpose create button as Save

        setupPickers();
        uploadPosterButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        if (getArguments() != null) eventId = getArguments().getString("eventId");

        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(getContext(), "Missing eventId", Toast.LENGTH_LONG).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        loadEvent(eventId);
        resetButton.setOnClickListener(v -> loadEvent(eventId));
        saveButton.setOnClickListener(v -> saveChanges());
    }

    /**
     * Fetches event details from Firestore to pre-fill the edit fields.
     *
     * @param eventId The ID of the event to load from Firestore.
     */
    private void loadEvent(String eventId) {
        DbManager.getInstance().getDb().collection("events").document(eventId).get()
                .addOnSuccessListener(this::applyEventSnapshot)
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    /**
     * Parses the Firestore document and updates the UI fields.
     * Checks the {@code geolocationRequired} field to toggle the Geo checkbox.
     *
     * @param doc The Firestore document snapshot containing event data.
     */
    private void applyEventSnapshot(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        String title = doc.getString("eventName");
        String description = doc.getString("description");
        String location = doc.getString("eventLocation");

        Long capacity = doc.getLong("eventCapacity");
        Long waitLimit = doc.getLong("waitingListLimit");

        String qrHash = doc.getString("qrCodeHash");
        Boolean geoReq = doc.getBoolean("geolocationRequired"); // Retrieve Geo setting
        existingPosterUrl = doc.getString("eventPosterURL");

        if (title != null) eventTitleField.setText(title);
        if (description != null) eventDescriptionField.setText(description);

        // Set Numeric Fields
        if (waitLimit != null) eventLimitField.setText(String.valueOf(waitLimit));
        if (capacity != null) eventCapacityField.setText(String.valueOf(capacity));

        // Location Parsing (Address vs City)
        if (location != null) {
            String addr = location;
            String city = "";
            int comma = location.indexOf(',');
            if (comma >= 0) {
                addr = location.substring(0, comma).trim();
                city = location.substring(comma + 1).trim();
            }
            eventAddressField.setText(addr);
            eventCityField.setText(city);
        }

        // Set Checkboxes
        if (geoCheckBox != null) geoCheckBox.setChecked(Boolean.TRUE.equals(geoReq));
        if (qrCheckBox != null) qrCheckBox.setChecked(qrHash != null && !qrHash.isEmpty());

        // Set Poster Button
        if (existingPosterUrl != null && !existingPosterUrl.isEmpty()) {
            uploadPosterButton.setText("Change poster");
        } else {
            uploadPosterButton.setText("+ Upload Poster");
        }

        // Set Dates
        setDateField(doc.get("eventStartTime"), eventCalendar, eventDateField, eventTimeField);
        setDateField(doc.get("registrationStart"), regStartCalendar, regStartDateField, null);
        setDateField(doc.get("registrationEnd"), regEndCalendar, regEndDateField, null);
    }

    private void setDateField(Object ts, Calendar cal, EditText dateField, EditText timeField) {
        Date d = null;
        if (ts instanceof Timestamp) d = ((Timestamp) ts).toDate();
        else if (ts instanceof Date) d = (Date) ts;

        if (d != null) {
            cal.setTime(d);
            dateField.setText(dateFmt.format(d));
            if (timeField != null) timeField.setText(timeFmt.format(d));
        }
    }

    /**
     * Saves changes to Firestore, including Geo requirement and registration dates.
     * Performs basic validation and shows error messages via Toast when needed.
     */
    private void saveChanges() {
        if (TextUtils.isEmpty(eventId)) return;

        String title = eventTitleField.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String desc = eventDescriptionField.getText().toString().trim();
        String limitStr = eventLimitField.getText().toString().trim();
        String capStr = eventCapacityField.getText().toString().trim();

        // Safe Parsing
        int waitlistLimit = limitStr.isEmpty() ? 0 : Integer.parseInt(limitStr);
        int capacity = capStr.isEmpty() ? 100 : Integer.parseInt(capStr);

        String address = eventAddressField.getText().toString().trim();
        String city = eventCityField.getText().toString().trim();
        String combinedLocation = !city.isEmpty() ? address + ", " + city : address;

        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("eventName", title);
        updates.put("description", desc);
        updates.put("eventLocation", combinedLocation);
        updates.put("waitingListLimit", waitlistLimit);
        updates.put("eventCapacity", capacity);
        updates.put("eventStartTime", eventCalendar.getTime());
        updates.put("registrationStart", regStartCalendar.getTime());
        updates.put("registrationEnd", regEndCalendar.getTime());

        // Save Geo Setting (Important for Entrant logic)
        if (geoCheckBox != null) {
            updates.put("geolocationRequired", geoCheckBox.isChecked());
        }

        if (qrCheckBox.isChecked()) {
            updates.put("qrCodeHash", eventId);
        } else {
            updates.put("qrCodeHash", null);
        }

        DbManager.getInstance().getDb().collection("events").document(eventId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (posterImageUri != null) DbManager.getInstance().uploadEventPoster(eventId, posterImageUri);
                    if (getContext() != null) Toast.makeText(getContext(), "Event updated", Toast.LENGTH_SHORT).show();
                    if (getView() != null) Navigation.findNavController(getView()).navigateUp();
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    saveButton.setEnabled(true);
                    saveButton.setText("SAVE");
                });
    }

    private void setupPickers() {
        makeReadOnly(eventDateField); makeReadOnly(eventTimeField);
        makeReadOnly(regStartDateField); makeReadOnly(regEndDateField);

        eventDateField.setOnClickListener(v -> showDate(eventCalendar, eventDateField));
        eventTimeField.setOnClickListener(v -> showTime(eventCalendar, eventTimeField));
        regStartDateField.setOnClickListener(v -> showDate(regStartCalendar, regStartDateField));
        regEndDateField.setOnClickListener(v -> showDate(regEndCalendar, regEndDateField));
    }

    private void makeReadOnly(EditText et) {
        et.setFocusable(false);
        et.setClickable(true);
    }

    private void showDate(Calendar cal, EditText et) {
        new DatePickerDialog(getContext(), (v, y, m, d) -> {
            cal.set(Calendar.YEAR, y); cal.set(Calendar.MONTH, m); cal.set(Calendar.DAY_OF_MONTH, d);
            et.setText(dateFmt.format(cal.getTime()));
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTime(Calendar cal, EditText et) {
        new TimePickerDialog(getContext(), (v, h, m) -> {
            cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, m);
            et.setText(timeFmt.format(cal.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }
}