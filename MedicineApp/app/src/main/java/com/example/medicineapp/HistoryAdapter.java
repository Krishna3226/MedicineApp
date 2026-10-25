package com.example.medicineapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {

    private final List<HistoryItem> items = new ArrayList<>();

    public void setItems(List<HistoryItem> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Context c = h.itemView.getContext();
        HistoryItem item = items.get(position);
        String dosage = item.dosage == null || item.dosage.isEmpty() ? "-" : item.dosage;
        h.tvName.setText(item.medicineName);
        h.tvDetails.setText(c.getString(R.string.history_details, dosage,
                item.scheduledDate, Utils.to12h(item.scheduledTime)));
        h.tvStatus.setText(item.status);
        int color = Utils.STATUS_TAKEN.equals(item.status) ? R.color.taken
                : Utils.STATUS_MISSED.equals(item.status) ? R.color.missed : R.color.pending;
        h.tvStatus.setTextColor(ContextCompat.getColor(c, color));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvName, tvDetails, tvStatus;
        Holder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvDetails = v.findViewById(R.id.tvDetails);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}
