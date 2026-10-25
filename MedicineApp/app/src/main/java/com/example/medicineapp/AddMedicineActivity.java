package com.example.medicineapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/** Used for both adding and editing a medicine (edit mode when EXTRA_ID is passed). */
public class AddMedicineActivity extends BaseActivity {

    public static final String EXTRA_ID = "medicine_id";

    private TextInputLayout tilName, tilDosage, tilStart, tilEnd;
    private TextInputLayout[] tilTimes;
    private EditText etName, etDosage, etStart, etEnd, etNotes;
    private EditText[] etTimes;
    private Spinner spFrequency;
    private long medicineId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);
        enableBack();

        tilName = findViewById(R.id.tilName);
        tilDosage = findViewById(R.id.tilDosage);
        tilStart = findViewById(R.id.tilStart);
        tilEnd = findViewById(R.id.tilEnd);
        tilTimes = new TextInputLayout[]{findViewById(R.id.tilTime1),
                findViewById(R.id.tilTime2), findViewById(R.id.tilTime3)};
        etName = findViewById(R.id.etName);
        etDosage = findViewById(R.id.etDosage);
        etStart = findViewById(R.id.etStart);
        etEnd = findViewById(R.id.etEnd);
        etNotes = findViewById(R.id.etNotes);
        etTimes = new EditText[]{findViewById(R.id.etTime1),
                findViewById(R.id.etTime2), findViewById(R.id.etTime3)};
        spFrequency = findViewById(R.id.spFrequency);

        ArrayAdapter<CharSequence> freqAdapter = ArrayAdapter.createFromResource(this,
                R.array.frequency_options, android.R.layout.simple_spinner_item);
        freqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrequency.setAdapter(freqAdapter);
        spFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                showTimeFields(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        for (EditText et : etTimes) et.setOnClickListener(v -> Utils.pickTime(this, (EditText) v));
        etStart.setOnClickListener(v -> Utils.pickDate(this, etStart));
        etEnd.setOnClickListener(v -> Utils.pickDate(this, etEnd));
        findViewById(R.id.btnSave).setOnClickListener(v -> save());

        medicineId = getIntent().getLongExtra(EXTRA_ID, -1);
        if (medicineId >= 0 && loadForEdit()) {
            setTitle(R.string.title_edit_medicine);
        } else {
            medicineId = -1;
            setTitle(R.string.title_add_medicine);
            etStart.setText(Utils.today());
        }
    }

    /** Only the time fields needed for the chosen frequency are shown. */
    private void showTimeFields(int count) {
        for (int i = 0; i < tilTimes.length; i++) {
            tilTimes[i].setVisibility(i < count ? View.VISIBLE : View.GONE);
        }
    }

    private boolean loadForEdit() {
        Medicine m = db.getMedicine(medicineId);
        if (m == null) return false;
        etName.setText(m.name);
        etDosage.setText(m.dosage);
        etStart.setText(m.startDate);
        etEnd.setText(m.endDate);
        etNotes.setText(m.notes);
        List<String> times = m.getTimes();
        spFrequency.setSelection(Math.max(0, Math.min(2, Math.max(m.getFrequencyCount(), times.size()) - 1)));
        for (int i = 0; i < times.size() && i < etTimes.length; i++) {
            etTimes[i].setTag(times.get(i));
            etTimes[i].setText(Utils.to12h(times.get(i)));
        }
        return true;
    }

    private void save() {
        String name = Utils.text(etName), dosage = Utils.text(etDosage);
        String start = Utils.text(etStart), end = Utils.text(etEnd);
        boolean ok = true;

        tilName.setError(null);
        tilDosage.setError(null);
        tilStart.setError(null);
        tilEnd.setError(null);
        for (TextInputLayout t : tilTimes) t.setError(null);

        if (name.isEmpty()) { tilName.setError(getString(R.string.err_required)); ok = false; }
        if (dosage.isEmpty()) { tilDosage.setError(getString(R.string.err_required)); ok = false; }

        int freq = spFrequency.getSelectedItemPosition() + 1;
        List<String> times = new ArrayList<>();
        for (int i = 0; i < freq; i++) {
            Object tag = etTimes[i].getTag();
            if (!(tag instanceof String)) {
                tilTimes[i].setError(getString(R.string.err_required));
                ok = false;
            } else if (times.contains(tag)) {
                tilTimes[i].setError(getString(R.string.err_time_duplicate));
                ok = false;
            } else {
                times.add((String) tag);
            }
        }
        Collections.sort(times);

        Date s = Utils.parseDate(start), e = Utils.parseDate(end);
        if (s == null) { tilStart.setError(getString(R.string.err_date_invalid)); ok = false; }
        if (e == null) { tilEnd.setError(getString(R.string.err_date_invalid)); ok = false; }
        if (s != null && e != null) {
            if (e.before(s)) {
                tilEnd.setError(getString(R.string.err_end_before_start));
                ok = false;
            } else if (medicineId < 0 && e.before(Utils.parseDate(Utils.today()))) {
                tilEnd.setError(getString(R.string.err_end_past));
                ok = false;
            }
        }
        if (!ok) return;

        Medicine m = new Medicine();
        m.name = name;
        m.dosage = dosage;
        m.frequency = String.valueOf(freq);
        m.reminderTime = String.join(",", times);
        m.startDate = start;
        m.endDate = end;
        m.notes = Utils.text(etNotes);

        if (medicineId >= 0) {
            m.id = medicineId;
            if (!db.updateMedicine(m)) { toast(R.string.db_error); return; }
        } else {
            m.id = db.addMedicine(m);
            if (m.id < 0) { toast(R.string.db_error); return; }
        }

        ReminderScheduler.scheduleAll(this, m);   // cancels old alarms first, then schedules new ones
        if (!Utils.canScheduleExact(this)) {
            Toast.makeText(this, R.string.exact_delayed, Toast.LENGTH_LONG).show();
        } else {
            toast(R.string.saved);
        }
        finish();
    }
}
