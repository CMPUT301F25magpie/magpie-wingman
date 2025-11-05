package com.example.magpie_wingman.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Event; // Import our new model

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnEventListener eventListener;

    // Interface for click events
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
        holder.eventDate.setText(event.getEventDate());
        holder.eventLocation.setText(event.getEventLocation());
        holder.eventDescription.setText(event.getDescription()); // <-- UPDATED
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * The ViewHolder class
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView eventPoster;
        TextView eventName;
        TextView eventDate;
        TextView eventLocation;
        TextView eventDescription; // <-- UPDATED
        OnEventListener eventListener;

        public EventViewHolder(@NonNull View itemView, OnEventListener eventListener) {
            super(itemView);
            eventPoster = itemView.findViewById(R.id.image_view_event_poster);
            eventName = itemView.findViewById(R.id.text_view_event_name);
            eventDate = itemView.findViewById(R.id.text_view_event_date);
            eventLocation = itemView.findViewById(R.id.text_view_event_location);
            eventDescription = itemView.findViewById(R.id.text_view_event_description); // <-- UPDATED
            this.eventListener = eventListener;

            // Set the click listener on the entire item
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            eventListener.onEventClick(getAdapterPosition());
        }
    }
}