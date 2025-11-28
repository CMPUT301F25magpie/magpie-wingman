package com.example.magpie_wingman.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.VH> {

    public interface OnNotificationClickListener {
        void onClick(Notification item);
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title;
        final TextView message;
        VH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_notification_title);
            message = itemView.findViewById(R.id.text_message);
        }
    }

    private final List<Notification> items = new ArrayList<>();
    private OnNotificationClickListener clickListener;

    public void setOnNotificationClickListener(OnNotificationClickListener l) {
        this.clickListener = l;
    }

    public void submitList(List<Notification> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Notification item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.message.setText(item.getBody());
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
