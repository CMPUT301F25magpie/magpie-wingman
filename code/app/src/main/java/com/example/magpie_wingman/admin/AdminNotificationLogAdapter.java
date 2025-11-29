package com.example.magpie_wingman.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Notification;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * RecyclerView adapter for the admin notification log.
 */
public class AdminNotificationLogAdapter extends RecyclerView.Adapter<AdminNotificationLogAdapter.VH> {

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView message;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_notification_title);
            message = itemView.findViewById(R.id.text_message);
        }
    }

    private final List<Notification> items = new ArrayList<>();

    /**
     * Replaces the adapter's data with a new list of notifications.
     */
    public void setNotifications(List<Notification> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Notification n = items.get(position);

        String title = n.getTitle();
        String body = n.getBody();

        long ts = n.getTimestamp();
        if (ts > 0) {
            String formatted = DateFormat
                    .getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(ts));
            title = formatted + " – " + title;
        }

        holder.title.setText(title);
        holder.message.setText(body);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}


