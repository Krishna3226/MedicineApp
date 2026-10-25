package com.example.medicineapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HealthDiaryActivity extends BaseActivity {

    private HealthEntryAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_diary);
        setTitle(R.string.title_diary);
        enableBack();

        tvEmpty = findViewById(R.id.tvEmpty);
        RecyclerView rv = findViewById(R.id.rvEntries);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HealthEntryAdapter(entry -> {
            Intent i = new Intent(this, AddHealthEntryActivity.class);
            i.putExtra(AddHealthEntryActivity.EXTRA_ID, entry.id);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        findViewById(R.id.btnAddEntry).setOnClickListener(v ->
                startActivity(new Intent(this, AddHealthEntryActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<HealthEntry> list = db.getAllEntries();
        adapter.setItems(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
