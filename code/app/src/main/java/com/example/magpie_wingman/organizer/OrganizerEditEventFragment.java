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
import android.widget.ImageView;
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
 *
 * Expects "eventId" in the fragment arguments bundle.
 * Loads the event from Firestore, pre-fills the form, and saves updates back.
 */
public class OrganizerEditEventFragment extends Fragment {

    // UI components (must match your XML ids)
    private EditText eventTitleField;
    private EditText eventLimitField;
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

    // Date/time handling
    private final Calendar eventCalendar = Calendar.getInstance();
    private final Calendar regStartCalendar = Calendar.getInstance();
    private final Calendar regEndCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFmt =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFmt =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    // Poster
    private Uri posterImageUri;           // new selected image (if any)
    private String existingPosterUrl;     // existing URL from Firestore (for info only)
    private ActivityResultLauncher<String> pickImageLauncher;

    // Event id to edit
    private String eventId;

    public OrganizerEditEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // image picker for replacing poster
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_edit_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Header buttons ---
        ImageButton backBtn = view.findViewById(R.id.button_back);
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        ImageButton infoBtn = view.findViewById(R.id.button_info);
        if (infoBtn != null) {
            infoBtn.setOnClickListener(v ->
                    Toast.makeText(getContext(),
                            "Edit your event details and tap SAVE.",
                            Toast.LENGTH_SHORT).show()
            );
        }

        // --- Bind all views ---
        eventTitleField      = view.findViewById(R.id.edit_event_title);
        eventLimitField      = view.findViewById(R.id.edit_limit);
        geoCheckBox          = view.findViewById(R.id.checkbox_geo);
        qrCheckBox           = view.findViewById(R.id.checkbox_qr);
        uploadPosterButton   = view.findViewById(R.id.button_upload_poster);

        eventAddressField    = view.findViewById(R.id.edit_address);
        eventCityField       = view.findViewById(R.id.edit_city);
        eventProvinceField   = view.findViewById(R.id.edit_province);
        eventDescriptionField= view.findViewById(R.id.edit_description);

        eventDateField       = view.findViewById(R.id.edit_date);
        eventTimeField       = view.findViewById(R.id.edit_time);
        regStartDateField    = view.findViewById(R.id.edit_registration_start);
        regEndDateField      = view.findViewById(R.id.edit_registration_end);

        resetButton          = view.findViewById(R.id.button_reset);
        saveButton           = view.findViewById(R.id.button_create);

        // Change label from CREATE -> SAVE for this screen
        saveButton.setText("SAVE");

        // Date/time pickers
        setupPickers();

        // Poster picker
        uploadPosterButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Get eventId from arguments
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(getContext(),
                    "Missing eventId for editing",
                    Toast.LENGTH_LONG).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        // Load event and prefill fields
        loadEvent(eventId);

        // Buttons
        resetButton.setOnClickListener(v -> loadEvent(eventId));   // reload from Firestore
        saveButton.setOnClickListener(v -> saveChanges());
    }

    /**
     * Loads the event document from Firestore and fills the form.
     */
    private void loadEvent(String eventId) {
        DbManager.getInstance().getDb()
                .collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(this::applyEventSnapshot)
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Failed to load event: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Apply Firestore snapshot into UI fields + calendars.
     */
    private void applyEventSnapshot(DocumentSnapshot doc) {
        if (!doc.exists()) {
            if (getContext() != null) {
                Toast.makeText(getContext(),
                        "Event not found.",
                        Toast.LENGTH_LONG).show();
            }
            return;
        }

        String title       = doc.getString("eventName");
        String description = doc.getString("description");
        String location    = doc.getString("eventLocation");
        Long   capacity    = doc.getLong("eventCapacity");
        Long   waitLimit   = doc.getLong("waitingListLimit");
        String qrHash      = doc.getString("qrCodeHash");
        existingPosterUrl  = doc.getString("eventPosterURL");

        // Title
        if (title != null) eventTitleField.setText(title);
        else eventTitleField.setText("");

        // Description
        if (description != null) eventDescriptionField.setText(description);
        else eventDescriptionField.setText("");

        // Capacity / waitlist limit
        if (waitLimit != null) {
            eventLimitField.setText(String.valueOf(waitLimit));
        } else {
            eventLimitField.setText("");
        }

        // Location: we originally stored "address, city". Try to split.
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
        } else {
            eventAddressField.setText("");
            eventCityField.setText("");
        }
        // Province wasn't stored separately; leave empty
        eventProvinceField.setText("");

        // QR checkbox
        qrCheckBox.setChecked(qrHash != null && !qrHash.isEmpty());

        // Poster button text
        if (existingPosterUrl != null && !existingPosterUrl.isEmpty()) {
            uploadPosterButton.setText("Change poster");
        } else {
            uploadPosterButton.setText("+ Upload Poster");
        }

        // Event start time
        Object startObj = doc.get("eventStartTime");
        Date startDate = null;
        if (startObj instanceof Timestamp) {
            startDate = ((Timestamp) startObj).toDate();
        } else if (startObj instanceof Date) {
            startDate = (Date) startObj;
        }
        if (startDate != null) {
            eventCalendar.setTime(startDate);
            eventDateField.setText(dateFmt.format(startDate));
            eventTimeField.setText(timeFmt.format(startDate));
        } else {
            eventDateField.setText("");
            eventTimeField.setText("");
        }

        // Registration start
        Object rsObj = doc.get("registrationStart");
        Date rsDate = null;
        if (rsObj instanceof Timestamp) {
            rsDate = ((Timestamp) rsObj).toDate();
        } else if (rsObj instanceof Date) {
            rsDate = (Date) rsObj;
        }
        if (rsDate != null) {
            regStartCalendar.setTime(rsDate);
            regStartDateField.setText(dateFmt.format(rsDate));
        } else {
            regStartDateField.setText("");
        }

        // Registration end
        Object reObj = doc.get("registrationEnd");
        Date reDate = null;
        if (reObj instanceof Timestamp) {
            reDate = ((Timestamp) reObj).toDate();
        } else if (reObj instanceof Date) {
            reDate = (Date) reObj;
        }
        if (reDate != null) {
            regEndCalendar.setTime(reDate);
            regEndDateField.setText(dateFmt.format(reDate));
        } else {
            regEndDateField.setText("");
        }
    }

    /**
     * Saves edited values back into the same event document.
     */
    private void saveChanges() {
        if (TextUtils.isEmpty(eventId)) return;

        String title = eventTitleField.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(getContext(),
                    "Title is required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String desc = eventDescriptionField.getText().toString().trim();
        String limitStr = eventLimitField.getText().toString().trim();
        int waitlistLimit = limitStr.isEmpty() ? 0 : Integer.parseInt(limitStr);

        String address = eventAddressField.getText().toString().trim();
        String city = eventCityField.getText().toString().trim();
        String province = eventProvinceField.getText().toString().trim();

        String combinedLocation;
        if (!city.isEmpty()) {
            combinedLocation = address + ", " + city;
        } else {
            combinedLocation = address;
        }
        // Province not yet stored in schema; you can append if desired:
        // if (!province.isEmpty()) combinedLocation += ", " + province;

        // Use an arbitrary capacity for now (schema already uses eventCapacity = 100 in create)
        // If you later add a capacity field to the UI, wire it in here.
        int capacity = 100;

        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("eventName", title);
        updates.put("description", desc);
        updates.put("eventLocation", combinedLocation);
        updates.put("eventCapacity", capacity);
        updates.put("waitingListLimit", waitlistLimit);
        updates.put("eventStartTime", eventCalendar.getTime());
        updates.put("registrationStart", regStartCalendar.getTime());
        updates.put("registrationEnd", regEndCalendar.getTime());

        if (qrCheckBox.isChecked()) {
            updates.put("qrCodeHash", eventId);
        } else {
            // remove QR or set null; here we clear it
            updates.put("qrCodeHash", null);
        }

        Task<Void> updateTask = DbManager.getInstance()
                .getDb()
                .collection("events")
                .document(eventId)
                .set(updates, SetOptions.merge());

        updateTask
                .addOnSuccessListener(aVoid -> {
                    // If a new poster was chosen, upload it
                    if (posterImageUri != null) {
                        DbManager.getInstance().uploadEventPoster(eventId, posterImageUri);
                    }

                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Event updated",
                                Toast.LENGTH_SHORT).show();
                    }
                    if (getView() != null) {
                        Navigation.findNavController(getView()).navigateUp();
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(),
                                "Failed to save changes: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                    saveButton.setEnabled(true);
                    saveButton.setText("SAVE");
                });
    }

    // ------------------------------------------------------------
    // Date/time pickers + helpers
    // ------------------------------------------------------------

    private void setupPickers() {
        makeReadOnly(eventDateField);
        makeReadOnly(eventTimeField);
        makeReadOnly(regStartDateField);
        makeReadOnly(regEndDateField);

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
        new DatePickerDialog(getContext(), (picker, y, m, d) -> {
            cal.set(Calendar.YEAR, y);
            cal.set(Calendar.MONTH, m);
            cal.set(Calendar.DAY_OF_MONTH, d);
            et.setText(dateFmt.format(cal.getTime()));
        }, cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTime(Calendar cal, EditText et) {
        new TimePickerDialog(getContext(), (picker, h, min) -> {
            cal.set(Calendar.HOUR_OF_DAY, h);
            cal.set(Calendar.MINUTE, min);
            et.setText(timeFmt.format(cal.getTime()));
        }, cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true).show();
    }
}
