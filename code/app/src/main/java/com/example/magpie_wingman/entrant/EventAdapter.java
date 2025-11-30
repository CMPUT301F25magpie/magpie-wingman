package com.example.magpie_wingman.entrant;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import com.bumptech.glide.Glide;
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

    public interface OnJoinClick {
        void onJoinClick(@NonNull Event event);
    }

    private final List<Event> events;
    private final String entrantId;
    private final OnJoinClick callback;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("MMM dd - hh:mm a", Locale.getDefault());

    public EventAdapter(@NonNull List<Event> events,
                        @NonNull String entrantId,
                        @NonNull OnJoinClick callback) {
        this.events = events;
        this.entrantId = entrantId;
        this.callback = callback;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_event, parent, false);
        return new VH(row);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Event e = events.get(position);
        String eventId = e.getEventId();
        h.boundEventId = eventId;
        String posterUrl = e.getEventPosterURL();
        if (posterUrl != null && !posterUrl.trim().isEmpty()) {
            h.poster.setVisibility(View.VISIBLE);
            Glide.with(h.poster.getContext())
                    .load(posterUrl)
                    .centerCrop()
                    .into(h.poster);
        } else {
            h.poster.setVisibility(View.GONE);
        }
        h.name.setText(e.getEventName());
        h.location.setText(e.getEventLocation() != null ? e.getEventLocation() : "TBD");

        if (e.getEventStartTime() != null) {
            h.date.setText(dateFormat.format(e.getEventStartTime()));
        } else {
            h.date.setText("Date TBD");
        }

        h.description.setText(e.getDescription());
        if (e.getWaitingListLimit() > 0) {
            h.waitlistCount.setText("Waiting List: " + e.getWaitlistCount() + "/" + e.getWaitingListLimit());
        }else{
            h.waitlistCount.setText("Waiting List: " + e.getWaitlistCount());
        }

        // Default until Firestore returns
        setButtonState(h, false);

        // Check if user already on the waitlist
        if (entrantId != null && !entrantId.isEmpty() && eventId != null) {
            DbManager.getInstance()
                    .isUserInWaitlist(eventId, entrantId)
                    .addOnSuccessListener(isInWaitlist -> {
                        // Check if holder got bound to diff event
                        if (!eventId.equals(h.boundEventId)) {
                            return;
                        }
                        setButtonState(h, Boolean.TRUE.equals(isInWaitlist));
                    });
        }

        h.itemView.setOnClickListener(null);

        // Only the button opens the event detail screen
        h.joinContainer.setOnClickListener(v -> {
            if (callback != null) {
                callback.onJoinClick(e);
            }
        });
    }

    private void setButtonState(@NonNull VH h, boolean isJoined) {
        if (isJoined) {
            h.joinText.setText("Leave");
            h.joinContainer.setBackgroundColor(Color.parseColor("#F44336")); // red
        } else {
            h.joinText.setText("Join");
            h.joinContainer.setBackgroundColor(Color.parseColor("#4CAF50")); // green
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView name, date, location, description, waitlistCount, joinText;
        String boundEventId;
        LinearLayout joinContainer;

        VH(@NonNull View itemView) {
            super(itemView);
            poster        = itemView.findViewById(R.id.image_view_event_poster);
            name          = itemView.findViewById(R.id.text_view_event_name);
            date          = itemView.findViewById(R.id.text_view_event_date);
            location      = itemView.findViewById(R.id.text_view_event_location);
            description   = itemView.findViewById(R.id.text_view_event_description);
            waitlistCount = itemView.findViewById(R.id.text_view_waitlist);
            joinContainer = itemView.findViewById(R.id.join_button_container);
            joinText      = itemView.findViewById(R.id.join_text);
        }
    }
}
