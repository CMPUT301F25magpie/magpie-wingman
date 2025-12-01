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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.VH> {

    public interface OnJoinClick {
        void onJoinClick(@NonNull Event event);
    }

    private enum ParticipationStage{
        NONE,
        WAITLIST,
        INVITED,
        REGISTRABLE,
        REGISTERED
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

        DbManager.getInstance()
                .getEventWaitlist(eventId)
                .addOnSuccessListener(users -> {
                    int count = (users != null) ? users.size() : 0;
                    if (e.getWaitingListLimit() > 0) {
                        h.waitlistCount.setText("Waiting List: " + count + "/" + e.getWaitingListLimit());
                    }else{
                        h.waitlistCount.setText("Waiting List: " + count);
                    }
                });

        h.description.setText(e.getDescription());


        // Default until Firestore returns
        applyButtonState(h, ParticipationStage.NONE);

        if (entrantId != null && !entrantId.isEmpty()
                && eventId != null && !eventId.isEmpty()) {
            loadParticipationStage(h, eventId, entrantId);
        }

        h.itemView.setOnClickListener(null);

        // Only the button opens the event detail screen
        h.joinContainer.setOnClickListener(v -> {
            if (callback != null) {
                callback.onJoinClick(e);
            }
        });
    }
    private void applyButtonState(@NonNull VH h,
                                  @NonNull ParticipationStage stage) {
        // Re-enable clicks; the hosting fragment still decides what happens
        // (typically it opens the detailed event screen).
        h.joinContainer.setEnabled(true);

        switch (stage) {
            case WAITLIST:
                // User is already on the waitlist
                h.joinText.setText("Leave");
                h.joinContainer.setBackgroundColor(Color.parseColor("#F44336")); // red
                break;

            case INVITED:
                // User has been invited → can accept
                h.joinText.setText("Accept");
                h.joinContainer.setBackgroundColor(Color.parseColor("#FF9800")); // orange
                break;

            case REGISTRABLE:
                // User is allowed to register
                h.joinText.setText("Register");
                h.joinContainer.setBackgroundColor(Color.parseColor("#4CAF50")); // green
                break;

            case REGISTERED:
                // Already registered – list button just acts as "View event"
                h.joinText.setText("View event");
                h.joinContainer.setBackgroundColor(Color.parseColor("#9E9E9E")); // grey
                break;

            case NONE:
            default:
                // Not involved yet – can join the waitlist
                h.joinText.setText("Join");
                h.joinContainer.setBackgroundColor(Color.parseColor("#4CAF50")); // green
                break;
        }
    }

    /**
     * Ask Firestore which "stage" the current entrant is at for this event and
     * update the button text/colour to match:
     *  - NONE        -> "Join"
     *  - WAITLIST    -> "Leave"
     *  - INVITED     -> "Accept"
     *  - REGISTRABLE -> "Register"
     *  - REGISTERED  -> "View event"
     */
    private void loadParticipationStage(@NonNull VH h,
                                        @NonNull String eventId,
                                        @NonNull String userId) {

        // Temporary disabled "loading" state while we query Firestore.
        h.joinContainer.setEnabled(false);
        h.joinText.setText("...");
        h.joinContainer.setBackgroundColor(Color.parseColor("#9E9E9E"));

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        DocumentReference evRef = fs.collection("events").document(eventId);

        // 1) Check if they're already REGISTERED
        evRef.collection("registered")
                .document(userId)
                .get()
                .addOnSuccessListener(regSnap -> {
                    if (!eventId.equals(h.boundEventId)) return;

                    if (regSnap.exists()) {
                        applyButtonState(h, ParticipationStage.REGISTERED);
                    } else {
                        // 2) Check REGISTRABLE
                        evRef.collection("registrable")
                                .document(userId)
                                .get()
                                .addOnSuccessListener(registrableSnap -> {
                                    if (!eventId.equals(h.boundEventId)) return;

                                    if (registrableSnap.exists()) {
                                        applyButtonState(h, ParticipationStage.REGISTRABLE);
                                    } else {
                                        // 3) Check INVITED
                                        evRef.collection("invited")
                                                .document(userId)
                                                .get()
                                                .addOnSuccessListener(invitedSnap -> {
                                                    if (!eventId.equals(h.boundEventId)) return;

                                                    if (invitedSnap.exists()) {
                                                        applyButtonState(h, ParticipationStage.INVITED);
                                                    } else {
                                                        // 4) Fall back to your existing waitlist check
                                                        DbManager.getInstance()
                                                                .isUserInWaitlist(eventId, userId)
                                                                .addOnSuccessListener(isInWaitlist -> {
                                                                    if (!eventId.equals(h.boundEventId)) return;

                                                                    if (Boolean.TRUE.equals(isInWaitlist)) {
                                                                        applyButtonState(h, ParticipationStage.WAITLIST);
                                                                    } else {
                                                                        applyButtonState(h, ParticipationStage.NONE);
                                                                    }
                                                                })
                                                                .addOnFailureListener(e ->
                                                                        applyButtonState(h, ParticipationStage.NONE));
                                                    }
                                                })
                                                .addOnFailureListener(e ->
                                                        applyButtonState(h, ParticipationStage.NONE));
                                    }
                                })
                                .addOnFailureListener(e ->
                                        applyButtonState(h, ParticipationStage.NONE));
                    }
                })
                .addOnFailureListener(e ->
                        applyButtonState(h, ParticipationStage.NONE));
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
