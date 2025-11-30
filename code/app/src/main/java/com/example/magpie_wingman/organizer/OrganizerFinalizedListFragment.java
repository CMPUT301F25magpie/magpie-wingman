package com.example.magpie_wingman.organizer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
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
import com.google.android.gms.tasks.Tasks;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OrganizerFinalizedListFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    public static OrganizerFinalizedListFragment newInstance(@NonNull String eventId) {
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        OrganizerFinalizedListFragment f = new OrganizerFinalizedListFragment();
        f.setArguments(b);
        return f;
    }

    private LinearLayout listContainer;
    private Button exportBtn;
    private ImageButton backBtn;

    /** Keeps the data we render + export */
    private final List<EntrantRow> entrants = new ArrayList<>();

    /** “Save as…” launcher for CSV export */
    private final ActivityResultLauncher<String> exportCsvLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"), uri -> {
                if (uri == null) return;
                writeCsvToUri(uri);
            });

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // This must point to the XML you pasted above
        return inflater.inflate(R.layout.fragment_organizer_finalized_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        listContainer = view.findViewById(R.id.finalized_list_container);
        exportBtn = view.findViewById(R.id.button_export_csv);
        ImageButton btnBack = view.findViewById(R.id.button_back);
        btnBack.setOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        String eventId = getArguments() != null ? getArguments().getString(ARG_EVENT_ID) : null;
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing event id", Toast.LENGTH_LONG).show();
            return;
        }

        exportBtn.setOnClickListener(v -> {
            if (entrants.isEmpty()) {
                Toast.makeText(requireContext(), "No entrants to export.", Toast.LENGTH_SHORT).show();
                return;
            }
            String filename = "finalized_list_" + eventId + ".csv";
            exportCsvLauncher.launch(filename);
        });

        // Remove any example rows from the XML and load real data
        listContainer.removeAllViews();
        loadFinalizedList(eventId);
    }

    private void loadFinalizedList(@NonNull String eventId) {
        DbManager db = DbManager.getInstance();
        db.getEventRegistered(eventId).addOnSuccessListener(userIds -> {
            entrants.clear();
            listContainer.removeAllViews();

            if (userIds == null || userIds.isEmpty()) {
                addEmptyStateRow("No entrants have enrolled yet.");
                return;
            }

            List<Task<String>> nameTasks = new ArrayList<>(userIds.size());
            for (String uid : userIds) {
                nameTasks.add(db.getUserName(uid)); // returns Task<String> displayName
            }

            Tasks.whenAllSuccess(nameTasks).addOnSuccessListener(rawNames -> {
                // Render rows to match your XML style
                for (int i = 0; i < userIds.size(); i++) {
                    String uid = userIds.get(i);
                    String name = null;
                    if (i < rawNames.size()) {
                        Object o = rawNames.get(i);
                        if (o instanceof String) name = (String) o;
                    }
                    if (name == null || name.trim().isEmpty()) name = uid; // fallback
                    entrants.add(new EntrantRow(uid, name));
                    listContainer.addView(makePersonRow(requireContext(), name));
                }
            }).addOnFailureListener(e -> {
                addEmptyStateRow("Failed to load names: " + e.getMessage());
            });

        }).addOnFailureListener(e -> {
            entrants.clear();
            listContainer.removeAllViews();
            addEmptyStateRow("Failed to load finalized list: " + e.getMessage());
        });
    }

    /** Programmatically builds a row like your example (icon + name) */
    private View makePersonRow(Context ctx, String name) {
        int padV = dp(ctx, 4);
        int iconSize = dp(ctx, 20);
        int iconMarginEnd = dp(ctx, 8);

        LinearLayout row = new LinearLayout(ctx);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, padV, 0, padV);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView iv = new ImageView(ctx);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        ivLp.setMarginEnd(iconMarginEnd);
        iv.setLayoutParams(ivLp);
        iv.setImageResource(R.drawable.ic_person);
        iv.setContentDescription("User");
        iv.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF000000)); // #000000

        TextView tv = new TextView(ctx);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(name);
        tv.setTextColor(0xFF000000); // #000000
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        row.addView(iv);
        row.addView(tv);
        return row;
    }

    private void addEmptyStateRow(String message) {
        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(message);
        tv.setTextColor(0xFFFFFFFF); // white on dark
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        // Empty-state goes *outside* the white card for contrast
        // If you prefer it inside the card, add it to listContainer instead.
        ((ViewGroup) requireView()).addView(tv);
    }

    private void writeCsvToUri(@NonNull Uri uri) {
        try (OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
            if (os == null) throw new IllegalStateException("Cannot open output stream");
            StringBuilder sb = new StringBuilder();
            sb.append("userId,name\n");
            for (EntrantRow r : entrants) {
                // rudimentary CSV escaping for commas/quotes
                sb.append(csv(r.userId)).append(",").append(csv(r.name)).append("\n");
            }
            os.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            Toast.makeText(requireContext(), "CSV exported.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String csv(String s) {
        if (s == null) return "";
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n");
        String out = s.replace("\"", "\"\"");
        return needsQuotes ? "\"" + out + "\"" : out;
    }

    private static int dp(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }

    private static final class EntrantRow {
        final String userId;
        final String name;
        EntrantRow(String userId, String name) { this.userId = userId; this.name = name; }
    }
}
