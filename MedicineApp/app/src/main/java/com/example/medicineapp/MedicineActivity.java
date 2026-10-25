package com.example.medicineapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicineActivity extends BaseActivity {

    private MedicineAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);
        setTitle(R.string.title_medicines);
        enableBack();

        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.rvMedicines);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineAdapter(m -> {
            Intent i = new Intent(this, MedicineDetailsActivity.class);
            i.putExtra(AddMedicineActivity.EXTRA_ID, m.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        findViewById(R.id.btnAddMedicine).setOnClickListener(v ->
                startActivity(new Intent(this, AddMedicineActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Medicine> list = db.getAllMedicines();
        adapter.setItems(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
