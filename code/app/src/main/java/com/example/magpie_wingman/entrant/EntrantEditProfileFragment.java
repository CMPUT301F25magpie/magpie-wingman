package com.example.magpie_wingman.entrant;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EntrantEditProfileFragment extends Fragment {

    private TextInputEditText firstNameEt, lastNameEt, emailEt, dobEt, phoneEt, passwordEt;
    private MaterialButtonToggleGroup roleToggle;
    private MaterialButton btnEntrant, btnOrganizer, btnUpdate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // Toolbar back
        MaterialToolbar toolbar = v.findViewById(R.id.toolbar_edit_profile);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(_v ->
                    requireActivity().getOnBackPressedDispatcher().onBackPressed()
            );
        }

        // Inputs
        firstNameEt  = v.findViewById(R.id.input_first_name);
        lastNameEt   = v.findViewById(R.id.input_last_name);
        emailEt      = v.findViewById(R.id.input_email);
        dobEt        = v.findViewById(R.id.input_dob);
        phoneEt      = v.findViewById(R.id.input_phone);
        passwordEt   = v.findViewById(R.id.input_password);

        // Role segmented control
        roleToggle   = v.findViewById(R.id.toggle_role);
        btnEntrant   = v.findViewById(R.id.btn_role_entrant);
        btnOrganizer = v.findViewById(R.id.btn_role_organizer);

        // Update button
        btnUpdate    = v.findViewById(R.id.btn_update_profile);

        // Date picker
        if (dobEt != null) {
            dobEt.setOnClickListener(_v -> showDatePicker());
        }

        // Load current user data (Pls pass userID when navigating)
        // E.g:
        // Bundle args = new Bundle();
        // args.putString("userId", currentUserId);
        // NavHostFragment.findNavController(this)
        //      .navigate(R.id.action_to_entrantInvitationsFragment, args);
        String userId = getArguments() != null ? getArguments().getString("userId") : null;
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(requireContext(), "Missing userId for profile edit.", Toast.LENGTH_SHORT).show();
            btnUpdate.setEnabled(false);
            return;
        }

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
            roleToggle.check(organizer ? R.id.btn_role_organizer : R.id.btn_role_entrant);
        });

        // Save updates
        btnUpdate.setOnClickListener(_v -> {
            String first = text(firstNameEt);
            String last  = text(lastNameEt);
            String fullName = buildFullName(first, last);

            String email = text(emailEt);
            String phone = text(phoneEt);
            boolean isOrganizer = roleToggle.getCheckedButtonId() == R.id.btn_role_organizer;

            if (TextUtils.isEmpty(first)) {
                firstNameEt.setError("First name required");
                return;
            }
            if (TextUtils.isEmpty(email) || !email.contains("@")) {
                emailEt.setError("Valid email required");
                return;
            }

            btnUpdate.setEnabled(false);

            List<Task<?>> ops = new ArrayList<>();
            ops.add(dbm.updateName(userId, fullName));
            ops.add(dbm.updateEmail(userId, email));
            ops.add(dbm.updatePhoneNumber(userId, phone));
            ops.add(dbm.changeOrgPerms(userId, isOrganizer));

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

                Toast.makeText(requireContext(), "Profile updated.", Toast.LENGTH_SHORT).show();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            });
        });
    }

    private void showDatePicker() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dlg = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String mm = String.format("%02d", month + 1);
                    String dd = String.format("%02d", dayOfMonth);
                    dobEt.setText(year + "-" + mm + "-" + dd);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dlg.show();
    }

    private static String text(TextInputEditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
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