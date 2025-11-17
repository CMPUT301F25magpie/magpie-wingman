package com.example.magpie_wingman.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

/**
 * Binds event cards with per-row Join/Leave. Works in Entrant LIST mode.
 * Pass the current entrantId so the adapter can call DbManager.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

    public interface OnEventClick {
        void onEventClick(@NonNull Event event);
    }

    private final List<Event> events;
    private final String entrantId;         // needed for Join/Leave
    private final OnEventClick clicker;     // optional row click (details screen)

    public EventAdapter(@NonNull List<Event> events,
                        @NonNull String entrantId,
                        OnEventClick clicker) {
        this.events = events;
        this.entrantId = entrantId;
        this.clicker = clicker;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_event, parent, false);
        return new VH(row);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
//        Event e = events.get(position);
//
//        // --- Bind basic fields ---
//        h.name.setText(safe(e.getEventName()));
//        h.date.setText(safe(e.getEventStartTime().toString()));
//        h.location.setText(safe(e.getEventLocation()));
//        h.description.setText(safe(e.getDescription()));
//
//
//        // --- Initialize join/leave UI state per-row ---
//        h.btnJoinLeave.setEnabled(false);
//        h.progress.setVisibility(View.VISIBLE);
//
//        final String eventId = e.getEventId(); // ensure your Event exposes this
//        DbManager.getInstance()
//                .isUserInWaitlist(eventId, entrantId)
//                .addOnSuccessListener(inWaitlist -> {
//                    boolean onList = inWaitlist != null && inWaitlist;
//                    h.setWaitlistState(onList);
//                })
//                .addOnFailureListener(err -> {
//                    // default to "Join" on error
//                    h.setWaitlistState(false);
//                });
//
//        // --- Click handlers ---
//        h.itemView.setOnClickListener(v -> {
//            if (clicker != null) clicker.onEventClick(e);
//        });

//        h.btnJoinLeave.setOnClickListener(v -> {
//            int pos = h.getBindingAdapterPosition();
//            if (pos == RecyclerView.NO_POSITION) return;
//
//            h.btnJoinLeave.setEnabled(false);
//            h.progress.setVisibility(View.VISIBLE);
//
//            if (h.isOnWaitlist) {
//                DbManager.getInstance()
//                        .cancelWaitlist(eventId, entrantId)
//                        .addOnSuccessListener(x -> {
//                            h.setWaitlistState(false);
//                            Toast.makeText(v.getContext(), "Left waitlist", Toast.LENGTH_SHORT).show();
//                        })
//                        .addOnFailureListener(e1 -> {
//                            Toast.makeText(v.getContext(), "Leave failed: " + e1.getMessage(), Toast.LENGTH_LONG).show();
//                            h.resetEnabled();
//                        });
//            } else {
//                DbManager.getInstance()
//                        .addUserToWaitlist(eventId, entrantId)
//                        .addOnSuccessListener(x -> {
//                            h.setWaitlistState(true);
//                            Toast.makeText(v.getContext(), "Joined waitlist", Toast.LENGTH_SHORT).show();
//                        })
//                        .addOnFailureListener(e1 -> {
//                            Toast.makeText(v.getContext(), "Join failed: " + e1.getMessage(), Toast.LENGTH_LONG).show();
//                            h.resetEnabled();
//                        });
//            }
//        });
    }

    @Override public int getItemCount() { return events.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ShapeableImageView poster;
        TextView name, date, location, description;
        Button btnJoinLeave;
        ProgressBar progress;
        boolean isOnWaitlist = false;
//
        VH(@NonNull View itemView) {
            super(itemView);
            poster      = itemView.findViewById(R.id.image_view_event_poster);
            name        = itemView.findViewById(R.id.text_view_event_name);
            date        = itemView.findViewById(R.id.text_view_event_date);
            location    = itemView.findViewById(R.id.text_view_event_location);
            description = itemView.findViewById(R.id.text_view_event_description);
//            btnJoinLeave= itemView.findViewById(R.id.btnJoinLeave);
//            progress    = itemView.findViewById(R.id.progressJoin);
        }
//
//        void setWaitlistState(boolean onList) {
//            isOnWaitlist = onList;
//            btnJoinLeave.setText(onList
//                    ? R.string.leave_waitlist
//                    : R.string.join_waitlist);
//            resetEnabled();
//        }
//
//        void resetEnabled() {
//            progress.setVisibility(View.GONE);
//            btnJoinLeave.setEnabled(true);
//        }
    }

    private static String safe(String s) { return s == null ? "" : s; }
}
