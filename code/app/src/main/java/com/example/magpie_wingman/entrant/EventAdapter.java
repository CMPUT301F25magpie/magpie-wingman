package com.example.magpie_wingman.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    // Using a single date format, as agreed
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private List<Event> eventList;
    private OnEventListener eventListener;

    public interface OnEventListener {
        void onEventClick(int position);
    }

    public EventAdapter(List<Event> eventList, OnEventListener eventListener) {
        this.eventList = eventList;
        this.eventListener = eventListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_event, parent, false);
        return new EventViewHolder(view, eventListener);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.eventName.setText(event.getEventName());
        holder.eventLocation.setText(event.getEventLocation());

        // Use the correct getter: getEventDescription()
        holder.eventDescription.setText(event.getEventDescription());

        // FIX: Use the existing Date object, prioritize eventDate if present
        Date dateToDisplay = event.getEventDate();
        if (dateToDisplay == null) {
            // If eventDate is null, use registrationStart (or just show TBD)
            dateToDisplay = event.getRegistrationStart();
        }

        if (dateToDisplay != null) {
            String dateString = dateFormat.format(dateToDisplay);
            holder.eventDate.setText(dateString);
        } else {
            holder.eventDate.setText("Date TBD");
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView eventPoster;
        TextView eventName;
        TextView eventDate;
        TextView eventLocation;
        TextView eventDescription;
        OnEventListener eventListener;

        public EventViewHolder(@NonNull View itemView, OnEventListener eventListener) {
            super(itemView);
            eventPoster = itemView.findViewById(R.id.image_view_event_poster);
            eventName = itemView.findViewById(R.id.text_view_event_name);
            eventDate = itemView.findViewById(R.id.text_view_event_date);
            eventLocation = itemView.findViewById(R.id.text_view_event_location);
            eventDescription = itemView.findViewById(R.id.text_view_event_description);
            this.eventListener = eventListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            eventListener.onEventClick(getAdapterPosition());
        }
    }
}