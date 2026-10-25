package com.example.medicineapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.app.NotificationManagerCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MedicineDetailsActivity extends BaseActivity {

    private long medicineId;
    private Medicine medicine;
    private TextView tvName, tvDosage, tvFrequency, tvTimes, tvCourse, tvNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_details);
        setTitle(R.string.title_medicine_details);
        enableBack();

        medicineId = getIntent().getLongExtra(AddMedicineActivity.EXTRA_ID, -1);
        tvName = findViewById(R.id.tvName);
        tvDosage = findViewById(R.id.tvDosage);
        tvFrequency = findViewById(R.id.tvFrequency);
        tvTimes = findViewById(R.id.tvTimes);
        tvCourse = findViewById(R.id.tvCourse);
        tvNotes = findViewById(R.id.tvNotes);

        findViewById(R.id.btnEdit).setOnClickListener(v -> {
            Intent i = new Intent(this, AddMedicineActivity.class);
            i.putExtra(AddMedicineActivity.EXTRA_ID, medicineId);
            startActivity(i);
        });
        findViewById(R.id.btnDelete).setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        medicine = medicineId >= 0 ? db.getMedicine(medicineId) : null;
        if (medicine == null) {           // missing or already deleted
            finish();
            return;
        }
        String[] freqNames = getResources().getStringArray(R.array.frequency_options);
        int fi = Math.max(0, Math.min(freqNames.length - 1, medicine.getFrequencyCount() - 1));
        tvName.setText(medicine.name);
        tvDosage.setText(getString(R.string.dosage_line, medicine.dosage));
        tvFrequency.setText(getString(R.string.frequency_line, freqNames[fi]));
        tvTimes.setText(getString(R.string.times_line, medicine.getTimesDisplay()));
        tvCourse.setText(getString(R.string.course_line, medicine.startDate, medicine.endDate));
        tvNotes.setText(getString(R.string.notes_line,
                medicine.notes == null || medicine.notes.isEmpty() ? getString(R.string.none) : medicine.notes));
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete_medicine_title)
                .setMessage(R.string.confirm_delete_medicine_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    ReminderScheduler.cancel(this, medicineId);          // 1. cancel alarms
                    for (int i = 0; i < 3; i++) {                        //    and shown notifications
                        NotificationManagerCompat.from(this)
                                .cancel(ReminderScheduler.requestCode(medicineId, i));
                    }
                    db.deleteMedicine(medicineId);                       // 2. delete record (history kept)
                    toast(R.string.deleted);
                    finish();
                })
                .show();
    }
}
