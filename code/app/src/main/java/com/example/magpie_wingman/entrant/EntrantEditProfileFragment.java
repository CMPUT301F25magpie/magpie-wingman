package com.example.magpie_wingman.entrant;

import android.app.DatePickerDialog;
import android.os.Bundle;
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

import com.example.magpie_wingman.MyApp;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.User;
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

/**
 * Fragment that lets an entrant view and edit their profile details.
 *
 * <p>This screen supports:
 * <ul>
 *     <li>Viewing current profile fields (name, email, phone, date of birth)</li>
 *     <li>Editing and validating profile information</li>
 *     <li>Persisting changes to Firestore via {@link DbManager}</li>
 *     <li>Optionally updating the entrant's password (if a new one is provided)</li>
 * </ul>
 *
 * <p>The fragment reads the currently logged-in {@link User} from {@link MyApp},
 * fetches additional profile data from Firestore, and then batches all update
 * operations using {@link Tasks#whenAllComplete(List)}.</p>
 */
public class EntrantEditProfileFragment extends Fragment {

    private EditText firstNameEt, lastNameEt, emailEt, dobEt, phoneEt, passwordEt;
    private Button btnUpdate;

    /**
     * Date formatter for displaying and parsing date of birth in dd/MM/yyyy format.
     */
    private final SimpleDateFormat dobFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    /**
     * Inflates the fragment layout containing the entrant profile form.
     *
     * @param inflater  layout inflater used to inflate views
     * @param container optional parent view
     * @param savedInstanceState previously saved state, if any
     * @return the inflated root view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_entrant_edit_profile, container, false);
    }

    /**
     * Initializes UI components, wires up listeners, and loads the current user's profile data.
     *
     * @param v root view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState previously saved state, if any
     */
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

        // Update button
        btnUpdate    = v.findViewById(R.id.button_update);

        // Date picker
        if (dobEt != null) {
            dobEt.setOnClickListener(_v -> showDatePicker());
        }

        // Retrieve current logged-in user
        User current = MyApp.getInstance().getCurrentUser();
        String userId = current.getUserId();
        loadUser(userId);
    }

    /**
     * Loads the current user's profile from Firestore and pre-populates the form.
     * Also attaches the click listener that performs validation and pushes updates.
     *
     * <p>This method:
     * <ul>
     *     <li>Fetches and splits the display name into first/last</li>
     *     <li>Loads email, phone, and date of birth</li>
     *     <li>Builds a batched list of update operations when the user clicks "Update"</li>
     * </ul>
     *
     * @param userId Firestore document ID of the current user
     */
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

    /**
     * Shows a {@link DatePickerDialog} to let the user select their date of birth.
     * If a valid date is already entered, the dialog initially opens on that date.
     */
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

    /**
     * Extracts and trims the text from an {@link EditText}.
     *
     * @param et the EditText to read from
     * @return trimmed text value, or an empty string if null
     */
    private static String text(EditText et) {
        if (et == null || et.getText() == null) return "";
        return et.getText().toString().trim();
    }

    /**
     * Splits a full display name into a first name and last name.
     *
     * <p>Heuristic:
     * <ul>
     *     <li>Everything before the last space = first name(s)</li>
     *     <li>Everything after the last space = last name</li>
     *     <li>If no space is present, the whole string is treated as first name</li>
     * </ul>
     *
     * @param full full name string
     * @return a 2-element array: index 0 = first name, index 1 = last name
     */
    private static String[] splitName(String full) {
        if (TextUtils.isEmpty(full)) return new String[]{"", ""};
        int lastSpace = full.lastIndexOf(' ');
        if (lastSpace <= 0) return new String[]{full, ""};
        return new String[]{full.substring(0, lastSpace), full.substring(lastSpace + 1)};
    }

    /**
     * Builds a single display name from first and last name components.
     *
     * <p>Rules:
     * <ul>
     *     <li>Concatenates first and last with a single space</li>
     *     <li>Trims leading/trailing whitespace</li>
     *     <li>Collapses multiple spaces into a single space</li>
     * </ul>
     *
     * @param first first name
     * @param last last name
     * @return a normalized full name string
     */
    private static String buildFullName(String first, String last) {
        return (first + " " + (TextUtils.isEmpty(last) ? "" : last)).trim().replaceAll("\\s+", " ");
    }
}
