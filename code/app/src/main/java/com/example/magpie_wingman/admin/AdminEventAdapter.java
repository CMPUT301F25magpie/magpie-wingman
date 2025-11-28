package com.example.magpie_wingman.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Event;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * RecyclerView adapter that shows a simple list of events for the admin screen.
 * Each row displays basic event info and an "X" button to remove the event.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    /**
     * Listener used to tell the fragment when the remove button is clicked.
     */
    public interface OnEventRemoveClickListener {
        /**
         * Called when the remove button is tapped for a row.
         *
         * @param position index of the event in the adapter.
         */
        void onRemoveEventClicked(int position);
    }

    private final List<Event> events;
    private final OnEventRemoveClickListener listener;
    private final DateFormat dateFormat =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

    /**
     * Creates a new adapter.
     *
     * @param events   list of events to show.
     * @param listener callback used when the user taps the remove button.
     */
    public AdminEventAdapter(@NonNull List<Event> events,
                             @NonNull OnEventRemoveClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    /**
     * Inflates the row layout for each event.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                // Make sure this matches the filename of the XML you sent
                .inflate(R.layout.list_item_admin_event, parent, false);
        return new EventViewHolder(v);
    }

    /**
     * Fills a row with event information and wires up the remove button.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event e = events.get(position);

        // Title
        String title = e.getEventName() != null ? e.getEventName() : "Untitled event";
        holder.titleText.setText(title);

        // Date / time line (null-safe)
        Date start = e.getEventStartTime();
        Date end = e.getEventEndTime();
        if (start != null && end != null) {
            holder.detailsText.setText(
                    dateFormat.format(start) + " - " + dateFormat.format(end)
            );
        } else if (start != null) {
            holder.detailsText.setText(dateFormat.format(start));
        } else if (end != null) {
            holder.detailsText.setText(dateFormat.format(end));
        } else {
            holder.detailsText.setText("");
        }

        // Location
        holder.locationText.setText(
                e.getEventLocation() != null ? e.getEventLocation() : ""
        );

        // Description
        holder.descriptionText.setText(
                e.getDescription() != null ? e.getDescription() : ""
        );

        // Waitlist count
        int waitlistCount = e.getWaitlistCount();
        holder.waitlistText.setText("waiting list: " + waitlistCount);

        // Poster image
        String posterUrl = e.getEventPosterURL();
        if (posterUrl != null && !posterUrl.trim().isEmpty()) {
            holder.posterImage.setVisibility(View.VISIBLE);
            Glide.with(holder.posterImage.getContext())
                    .load(posterUrl)
                    .centerCrop()
                    .into(holder.posterImage);
        } else {
            // Either hide the image or show a default placeholder so recycled views don't show old posters
            holder.posterImage.setVisibility(View.VISIBLE); // or GONE if you prefer
            holder.posterImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Remove button
        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    listener.onRemoveEventClicked(adapterPos);
                }
            }
        });
    }

    /**
     * Returns how many events are currently in the list.
     */
    @Override
    public int getItemCount() {
        return events.size();
    }

    /**
     * Simple holder that stores references to the views in one row.
     * IDs here must match list_item_admin_event.xml.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        final ImageView posterImage;
        final TextView titleText;
        final TextView detailsText;
        final TextView locationText;
        final TextView descriptionText;
        final TextView waitlistText;
        final ImageButton removeButton;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            posterImage = itemView.findViewById(R.id.image_event_poster);
            titleText = itemView.findViewById(R.id.text_event_title);
            detailsText = itemView.findViewById(R.id.text_event_details);
            locationText = itemView.findViewById(R.id.text_event_location);
            descriptionText = itemView.findViewById(R.id.text_event_description);
            waitlistText = itemView.findViewById(R.id.text_event_waitlist);
            removeButton = itemView.findViewById(R.id.button_remove);
        }
    }
}
