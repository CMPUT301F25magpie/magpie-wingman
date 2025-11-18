package com.example.magpie_wingman.entrant;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EntrantEditProfileFragment extends Fragment {

    private EditText firstNameEt, lastNameEt, emailEt, dobEt, phoneEt, passwordEt;
    private Button btnEntrant, btnOrganizer, btnUpdate;

    // Tracks which role is currently selected
    private boolean isOrganizerSelected = false;

    private final SimpleDateFormat dobFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Header back button
        ImageButton backBtn = v.findViewById(R.id.button_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(_v ->
                    requireActivity().getOnBackPressedDispatcher().onBackPressed()
            );
        }

        // Inputs
        firstNameEt  = v.findViewById(R.id.edit_first_name);
        lastNameEt   = v.findViewById(R.id.edit_last_name);
        emailEt      = v.findViewById(R.id.edit_email);
        dobEt        = v.findViewById(R.id.edit_dob);
        phoneEt      = v.findViewById(R.id.edit_phone);
        passwordEt   = v.findViewById(R.id.edit_password);

        // Role buttons
        btnEntrant   = v.findViewById(R.id.button_entrant);
        btnOrganizer = v.findViewById(R.id.button_organizer);

        // Update button
        btnUpdate    = v.findViewById(R.id.button_update);

        // Date picker
        if (dobEt != null) {
            dobEt.setOnClickListener(_v -> showDatePicker());
        }

        // Role button click listeners
        if (btnEntrant != null) {
            btnEntrant.setOnClickListener(_v -> setRole(false));
        }
        if (btnOrganizer != null) {
            btnOrganizer.setOnClickListener(_v -> setRole(true));
        }

        // Retrieve deviceId
        String deviceId = Settings.Secure.getString(
                requireContext().getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        DbManager.getInstance().findUserByDeviceId(deviceId)
                .addOnSuccessListener(this::loadUser);
    }

    // Loads profile data
    private void loadUser(String userId) {
        DbManager dbm = DbManager.getInstance();

        dbm.getUserName(userId).addOnSuccessListener(name -> {
            String safe = name != null ? name.trim() : "";
            String[] split = splitName(safe);
            firstNameEt.setText(split[0]);
            lastNameEt.setText(split[1]);
        });

        dbm.getUserEmail(userId).addOnSuccessListener(email -> {
            if (email != null) emailEt.setText(email);
        });

        dbm.getUserPhone(userId).addOnSuccessListener(phone -> {
            if (phone != null) phoneEt.setText(phone);
        });

        dbm.getIsOrganizer(userId).addOnSuccessListener(isOrg -> {
            // default to Entrant if null
            boolean organizer = isOrg != null && isOrg;
            setRole(organizer);
        });

        // Load DOB
        dbm.getDb()
                .collection("users").document(userId)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc != null && doc.exists()) {
                        Date dob = doc.getDate("dateOfBirth");
                        if (dob != null) {
                            dobEt.setText(dobFmt.format(dob));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(), "Failed to load DOB: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

        // Save updates
        btnUpdate.setOnClickListener(_v -> {
            String first = text(firstNameEt);
            String last  = text(lastNameEt);
            String fullName = buildFullName(first, last);

            String email = text(emailEt);
            String phone = text(phoneEt);
            String newPassword = text(passwordEt);
            boolean isOrganizer = isOrganizerSelected;

            if (TextUtils.isEmpty(first)) {
                firstNameEt.setError("First name required");
                return;
            }
            if (TextUtils.isEmpty(email) || !email.contains("@")) {
                emailEt.setError("Valid email required");
                return;
            }

            String dobStr = text(dobEt);
            Date dobDate = null;
            if (!TextUtils.isEmpty(dobStr)) {
                try {
                    dobFmt.setLenient(false);
                    dobDate = dobFmt.parse(dobStr);
                } catch (ParseException e) {
                    dobEt.setError("Use format dd/MM/yyyy");
                    return;
                }
            }

            btnUpdate.setEnabled(false);

            List<Task<?>> ops = new ArrayList<>();
            ops.add(dbm.updateName(userId, fullName));
            ops.add(dbm.updateEmail(userId, email));
            ops.add(dbm.updatePhoneNumber(userId, phone));
            ops.add(dbm.changeOrgPerms(userId, isOrganizer));

            if (dobDate != null) {
                ops.add(dbm.updateDOB(userId, dobDate));
            }

            if (!TextUtils.isEmpty(newPassword)) {
                ops.add(dbm.updatePassword(userId, newPassword));
            }

            Tasks.whenAllComplete(ops).addOnCompleteListener(t -> {
                btnUpdate.setEnabled(true);

                // Show first failure if any
                for (Task<?> each : ops) {
                    if (!each.isSuccessful()) {
                        Exception e = each.getException();
                        Toast.makeText(requireContext(),
                                "Update failed: " + (e != null ? e.getMessage() : "unknown error"),
                                Toast.LENGTH_LONG).show();
                        return;
                        }
                    }

                passwordEt.setText("");     // Clear password field after successful update
                Toast.makeText(requireContext(), "Profile updated.", Toast.LENGTH_SHORT).show();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();

        // If birthdate existed, open picker on that date
        String existing = text(dobEt);
        if (!TextUtils.isEmpty(existing)) {
            try {
                dobFmt.setLenient(false);
                Date parsed = dobFmt.parse(existing);
                if (parsed != null) c.setTime(parsed);
            } catch (ParseException ignored) {}
        }

        DatePickerDialog dlg = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String dd = String.format(Locale.getDefault(), "%02d", dayOfMonth);
                    String mm = String.format(Locale.getDefault(), "%02d", month + 1);
                    dobEt.setText(dd + "/" + mm + "/" + year);  // dd/MM/yyyy
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dlg.show();
    }

    private void setRole(boolean organizer) {
        isOrganizerSelected = organizer;

        if (btnEntrant == null || btnOrganizer == null) return;

        if (organizer) {
            btnOrganizer.setBackgroundResource(R.drawable.blue_button_bg);
            btnEntrant.setBackgroundResource(R.drawable.white_button_bg);
        } else {
            btnEntrant.setBackgroundResource(R.drawable.blue_button_bg);
            btnOrganizer.setBackgroundResource(R.drawable.white_button_bg);
        }
    }

    private static String text(EditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    private static String[] splitName(String full) {
        if (TextUtils.isEmpty(full)) return new String[]{"", ""};
        int lastSpace = full.lastIndexOf(' ');
        if (lastSpace <= 0) return new String[]{full, ""};
        return new String[]{full.substring(0, lastSpace), full.substring(lastSpace + 1)};
    }

    private static String buildFullName(String first, String last) {
        return (first + " " + (TextUtils.isEmpty(last) ? "" : last)).trim().replaceAll("\\s+", " ");
    }
}