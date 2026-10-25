package com.example.medicineapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HealthEntryAdapter extends RecyclerView.Adapter<HealthEntryAdapter.Holder> {

    public interface OnClick { void onClick(HealthEntry e); }

    private final List<HealthEntry> items = new ArrayList<>();
    private final OnClick listener;

    public HealthEntryAdapter(OnClick listener) { this.listener = listener; }

    public void setItems(List<HealthEntry> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_entry, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        HealthEntry e = items.get(position);
        h.tvTitle.setText(e.title);
        h.tvDate.setText(e.date);
        h.tvSymptoms.setText(e.symptoms);
        h.tvSymptoms.setVisibility(e.symptoms == null || e.symptoms.isEmpty() ? View.GONE : View.VISIBLE);
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onClick(items.get(pos));
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvDate, tvSymptoms;
        Holder(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDate = v.findViewById(R.id.tvDate);
            tvSymptoms = v.findViewById(R.id.tvSymptoms);
        }
    }
}
