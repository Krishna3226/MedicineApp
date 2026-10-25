package com.example.medicineapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.Holder> {

    public interface OnClick { void onClick(Medicine m); }

    private final List<Medicine> items = new ArrayList<>();
    private final OnClick listener;

    public MedicineAdapter(OnClick listener) { this.listener = listener; }

    public void setItems(List<Medicine> list) {
        items.clear();
        items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Medicine m = items.get(position);
        h.tvName.setText(m.name);
        h.tvInfo.setText(h.itemView.getContext().getString(R.string.dosage_line, m.dosage));
        h.tvTimes.setText(h.itemView.getContext().getString(R.string.times_line, m.getTimesDisplay()));
        h.itemView.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onClick(items.get(pos));
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvName, tvInfo, tvTimes;
        Holder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvInfo = v.findViewById(R.id.tvInfo);
            tvTimes = v.findViewById(R.id.tvTimes);
        }
    }
}
