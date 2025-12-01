package com.example.magpie_wingman.organizer;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows the list of entrants who have been cancelled for this event.
 *
 * Data source: events/{eventId}/cancelled subcollection.
 */
public class CancelledListFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    private LinearLayout listContainer;

    public CancelledListFragment() {
        // Required empty public constructor
    }

    public static CancelledListFragment newInstance(String eventId) {
        CancelledListFragment f = new CancelledListFragment();
        Bundle b = new Bundle();
        b.putString(ARG_EVENT_ID, eventId);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cancelled_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.button_back);
        listContainer = view.findViewById(R.id.cancelled_list_container);

        btnBack.setOnClickListener(v ->
                Navigation.findNavController(view).navigateUp()
        );

        String eventId = (getArguments() != null)
                ? getArguments().getString(ARG_EVENT_ID)
                : null;

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(requireContext(), "Missing event id", Toast.LENGTH_LONG).show();
            return;
        }

        loadCancelledList(eventId);
    }

    private void loadCancelledList(@NonNull String eventId) {
        DbManager db = DbManager.getInstance();

        db.getEventCancelled(eventId)
                .addOnSuccessListener(userIds -> {
                    listContainer.removeAllViews();

                    if (userIds == null || userIds.isEmpty()) {
                        addEmptyStateRow("No cancelled entrants.");
                        return;
                    }

                    // Fetch names for display
                    List<Task<String>> nameTasks = new ArrayList<>(userIds.size());
                    for (String uid : userIds) {
                        nameTasks.add(db.getUserName(uid));
                    }

                    Tasks.whenAllSuccess(nameTasks)
                            .addOnSuccessListener(rawNames -> {
                                for (int i = 0; i < userIds.size(); i++) {
                                    String name = null;
                                    if (i < rawNames.size()) {
                                        Object o = rawNames.get(i);
                                        if (o instanceof String) {
                                            name = (String) o;
                                        }
                                    }
                                    if (name == null || name.trim().isEmpty()) {
                                        name = userIds.get(i);
                                    }
                                    listContainer.addView(makePersonRow(requireContext(), name));
                                }
                            })
                            .addOnFailureListener(e ->
                                    addEmptyStateRow("Failed to load names: " + e.getMessage()));
                })
                .addOnFailureListener(e -> {
                    listContainer.removeAllViews();
                    addEmptyStateRow("Failed to load cancelled entrants: " + e.getMessage());
                });
    }

    /** Build one row: person icon + name (matches mock). */
    private View makePersonRow(Context ctx, String name) {
        int padV = dp(ctx, 8);
        int iconSize = dp(ctx, 20);
        int iconMarginEnd = dp(ctx, 8);

        LinearLayout row = new LinearLayout(ctx);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, padV, 0, padV);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        ImageView iv = new ImageView(ctx);
        LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(iconSize, iconSize);
        ivLp.setMarginEnd(iconMarginEnd);
        iv.setLayoutParams(ivLp);
        iv.setImageResource(R.drawable.ic_person);
        iv.setContentDescription("User");
        iv.setImageTintList(android.content.res.ColorStateList.valueOf(0xFF000000));

        TextView tv = new TextView(ctx);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(name);
        tv.setTextColor(0xFF000000);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        row.addView(iv);
        row.addView(tv);
        return row;
    }

    private void addEmptyStateRow(String message) {
        TextView tv = new TextView(requireContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        tv.setText(message);
        tv.setTextColor(0xFF000000);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        listContainer.addView(tv);
    }

    private static int dp(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }
}
