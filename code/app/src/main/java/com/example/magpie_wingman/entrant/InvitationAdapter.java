package com.example.magpie_wingman.entrant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.magpie_wingman.R;
import com.example.magpie_wingman.data.model.Invitation;

import java.util.ArrayList;
import java.util.List;

public class InvitationAdapter extends RecyclerView.Adapter<InvitationAdapter.VH> {

    public interface OnActionListener {
        void onAccept(Invitation inv, int position);
        void onDecline(Invitation inv, int position);
    }

    private final List<Invitation> items = new ArrayList<>();
    private final OnActionListener listener;

    public InvitationAdapter(OnActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Invitation> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_invitation, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Invitation inv = items.get(pos);
        h.title.setText(inv.getEventName());
        h.datetime.setText(inv.getDatetime());
        h.location.setText(inv.getLocation());
        h.description.setText(inv.getDescription());

        h.btnAccept.setOnClickListener(v ->
                { if (listener != null) listener.onAccept(inv, h.getBindingAdapterPosition()); }
        );
        h.btnDecline.setOnClickListener(v ->
                { if (listener != null) listener.onDecline(inv, h.getBindingAdapterPosition()); }
        );
    }

    @Override
    public int getItemCount() { return items.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, datetime, location, description;
        ImageButton btnAccept, btnDecline;

        VH(@NonNull View v) {
            super(v);
            poster = v.findViewById(R.id.image_poster);
            title = v.findViewById(R.id.text_title);
            datetime = v.findViewById(R.id.text_datetime);
            location = v.findViewById(R.id.text_location);
            description = v.findViewById(R.id.text_description);
            btnAccept = v.findViewById(R.id.btn_accept);
            btnDecline = v.findViewById(R.id.btn_decline);
        }
    }
}