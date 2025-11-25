package com.example.magpie_wingman.entrant;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.DbManager;
import com.example.magpie_wingman.data.model.Event;
import com.google.android.material.imageview.ShapeableImageView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

    public interface OnEventClick {
        void onEventClick(@NonNull Event event);
    }

    private final List<Event> events;
    private final String entrantId;
    private final OnEventClick clicker;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd - hh:mm a", Locale.getDefault());

    public EventAdapter(@NonNull List<Event> events, @NonNull String entrantId, OnEventClick clicker) {
        this.events = events;
        this.entrantId = entrantId;
        this.clicker = clicker;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_event, parent, false);
        return new VH(row);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Event e = events.get(position);

        h.name.setText(e.getEventName());
        h.location.setText(e.getEventLocation() != null ? e.getEventLocation() : "TBD");

        if (e.getEventStartTime() != null) {
            h.date.setText(dateFormat.format(e.getEventStartTime()));
        } else {
            h.date.setText("Date TBD");
        }

        h.description.setText(e.getDescription());
        h.waitlistCount.setText("Waiting List: " + e.getWaitlistCount());

        DbManager.getInstance().isUserInWaitlist(e.getEventId(), entrantId)
                .addOnSuccessListener(isInWaitlist -> {
                    if (isInWaitlist) {
                        setButtonState(h, true);
                    } else {
                        setButtonState(h, false);
                    }
                });

        h.itemView.setOnClickListener(v -> {
            if (clicker != null) clicker.onEventClick(e);
        });

        h.joinContainer.setOnClickListener(v -> {
            h.joinContainer.setEnabled(false);
            DbManager.getInstance().isUserInWaitlist(e.getEventId(), entrantId)
                    .addOnSuccessListener(isInWaitlist -> {
                        if (isInWaitlist) {
                            DbManager.getInstance().cancelWaitlist(e.getEventId(), entrantId).addOnSuccessListener(aVoid -> {
                                setButtonState(h, false);
                                Toast.makeText(v.getContext(), "Left Waitlist", Toast.LENGTH_SHORT).show();
                                h.joinContainer.setEnabled(true);
                            });
                        } else {
                            DbManager.getInstance().addUserToWaitlist(e.getEventId(), entrantId).addOnSuccessListener(aVoid -> {
                                setButtonState(h, true);
                                Toast.makeText(v.getContext(), "Joined Waitlist", Toast.LENGTH_SHORT).show();
                                h.joinContainer.setEnabled(true);
                            });
                        }
                    });
        });
    }

    private void setButtonState(VH h, boolean isJoined) {
        if (isJoined) {
            h.joinText.setText("Joined");
            h.joinContainer.setBackgroundResource(R.drawable.rounded_join_button);
        } else {
            h.joinText.setText("Join");
            h.joinContainer.setBackgroundColor(Color.parseColor("#888888"));
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ShapeableImageView poster;
        TextView name, date, location, description, waitlistCount, joinText;
        LinearLayout joinContainer;

        VH(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.image_view_event_poster);
            name = itemView.findViewById(R.id.text_view_event_name);
            date = itemView.findViewById(R.id.text_view_event_date);
            location = itemView.findViewById(R.id.text_view_event_location);
            description = itemView.findViewById(R.id.text_view_event_description);
            waitlistCount = itemView.findViewById(R.id.text_view_waitlist);
            joinContainer = itemView.findViewById(R.id.join_button_container);
            joinText = itemView.findViewById(R.id.join_text);
        }
    }
}
