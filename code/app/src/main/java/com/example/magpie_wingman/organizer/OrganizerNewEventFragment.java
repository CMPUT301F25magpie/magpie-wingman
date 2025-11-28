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

import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.User;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrganizerNewEventFragment extends Fragment {

    private EditText eventTitleField, eventLimitField, eventAddressField, eventCityField, eventProvinceField, eventDescriptionField;
    private EditText eventDateField, eventTimeField, regStartDateField, regEndDateField;
    private CheckBox qrCheckBox;
    private Button createButton, uploadPosterButton;

    User currentUser = MyApp.getInstance().getCurrentUser();
    private final Calendar eventCalendar = Calendar.getInstance();
    private final Calendar regStartCalendar = Calendar.getInstance();
    private final Calendar regEndCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());

    private Uri posterImageUri;
    private ActivityResultLauncher<String> pickImageLauncher;

    public OrganizerNewEventFragment() { }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                posterImageUri = uri;
                if (uploadPosterButton != null) uploadPosterButton.setText("Poster selected");
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_new_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventTitleField = view.findViewById(R.id.edit_event_title);
        eventLimitField = view.findViewById(R.id.edit_limit);
        eventAddressField = view.findViewById(R.id.edit_address);
        eventCityField = view.findViewById(R.id.edit_city);
        eventProvinceField = view.findViewById(R.id.edit_province);
        eventDescriptionField = view.findViewById(R.id.edit_description);
        eventDateField = view.findViewById(R.id.edit_date);
        eventTimeField = view.findViewById(R.id.edit_time);
        regStartDateField = view.findViewById(R.id.edit_registration_start);
        regEndDateField = view.findViewById(R.id.edit_registration_end);
        qrCheckBox = view.findViewById(R.id.checkbox_qr);
        createButton = view.findViewById(R.id.button_create);
        uploadPosterButton = view.findViewById(R.id.button_upload_poster);
        ImageButton backBtn = view.findViewById(R.id.button_back);

        setupPickers();
        backBtn.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());
        uploadPosterButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        createButton.setOnClickListener(v -> saveEvent(view));
    }

    private void saveEvent(View view) {
        String title = eventTitleField.getText().toString().trim();
        String desc = eventDescriptionField.getText().toString().trim();
        String limitStr = eventLimitField.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        int waitlistLimit = limitStr.isEmpty() ? 0 : Integer.parseInt(limitStr);
        int capacity = 100;
        String location = eventAddressField.getText().toString() + ", " + eventCityField.getText().toString();
        String organizerId = (currentUser != null) ? currentUser.getUserId() : "unknown";

        createButton.setEnabled(false);
        createButton.setText("Creating...");

        new Thread(() -> {
            try {
                com.google.android.gms.tasks.Task<Void> task = DbManager.getInstance().createEvent(
                        title, desc, organizerId, regStartCalendar.getTime(), regEndCalendar.getTime()
                );
                if (getActivity() != null) {
                    task.addOnSuccessListener(getActivity(), aVoid -> {
                        findLastEventAndFillDetails(title, organizerId, location, capacity, waitlistLimit, eventCalendar.getTime(), posterImageUri);
                        Toast.makeText(getContext(), "Event Created!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(view).navigateUp();
                    }).addOnFailureListener(getActivity(), e -> {
                        createButton.setEnabled(true);
                        createButton.setText("CREATE");
                    });
                }
            } catch (Exception e) {
                // ignore
            }
        }).start();
    }

    private void findLastEventAndFillDetails(String title, String orgId, String loc, int cap, int wlLimit, Date start, @Nullable Uri posterUri) {
        DbManager.getInstance().getDb().collection("events")
                .whereEqualTo("eventName", title).whereEqualTo("organizerId", orgId).limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        String eventId = snapshots.getDocuments().get(0).getId();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("eventLocation", loc);
                        updates.put("eventCapacity", cap);
                        updates.put("eventStartTime", start);
                        updates.put("waitlistCount", 0);
                        updates.put("waitingListLimit", wlLimit);
                        if (qrCheckBox.isChecked()) updates.put("qrCodeHash", eventId);

                        DbManager.getInstance().getDb().collection("events").document(eventId).set(updates, SetOptions.merge());

                        if (posterUri != null) DbManager.getInstance().uploadEventPoster(eventId, posterUri);
                    }
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
    private void makeReadOnly(EditText et) { et.setFocusable(false); et.setClickable(true); }
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