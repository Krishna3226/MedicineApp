package com.example.medicineapp;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

/** Add a diary entry, or edit/delete an existing one (when EXTRA_ID is passed). */
public class AddHealthEntryActivity extends BaseActivity {

    public static final String EXTRA_ID = "entry_id";

    private TextInputLayout tilDate, tilTitle;
    private EditText etDate, etTitle, etSymptoms, etNotes;
    private long entryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_health_entry);
        enableBack();

        tilDate = findViewById(R.id.tilDate);
        tilTitle = findViewById(R.id.tilTitle);
        etDate = findViewById(R.id.etDate);
        etTitle = findViewById(R.id.etTitle);
        etSymptoms = findViewById(R.id.etSymptoms);
        etNotes = findViewById(R.id.etNotes);
        MaterialButton btnDelete = findViewById(R.id.btnDelete);

        etDate.setOnClickListener(v -> Utils.pickDate(this, etDate));
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        btnDelete.setOnClickListener(v -> confirmDelete());

        entryId = getIntent().getLongExtra(EXTRA_ID, -1);
        HealthEntry existing = entryId >= 0 ? db.getEntry(entryId) : null;
        if (existing != null) {
            setTitle(R.string.title_edit_entry);
            etDate.setText(existing.date);
            etTitle.setText(existing.title);
            etSymptoms.setText(existing.symptoms);
            etNotes.setText(existing.notes);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            entryId = -1;
            setTitle(R.string.title_add_entry);
            etDate.setText(Utils.today());          // default date is today
        }
    }

    private void save() {
        tilDate.setError(null);
        tilTitle.setError(null);
        String date = Utils.text(etDate), title = Utils.text(etTitle);
        boolean ok = true;
        if (Utils.parseDate(date) == null) { tilDate.setError(getString(R.string.err_date_invalid)); ok = false; }
        if (title.isEmpty()) { tilTitle.setError(getString(R.string.err_required)); ok = false; }
        if (!ok) return;

        HealthEntry e = new HealthEntry();
        e.id = entryId;
        e.date = date;
        e.title = title;
        e.symptoms = Utils.text(etSymptoms);
        e.notes = Utils.text(etNotes);

        boolean saved = entryId >= 0 ? db.updateEntry(e) : db.addEntry(e) >= 0;
        if (!saved) { toast(R.string.db_error); return; }
        toast(R.string.saved);
        finish();
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_delete_entry_title)
                .setMessage(R.string.confirm_delete_entry_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    db.deleteEntry(entryId);
                    toast(R.string.deleted);
                    finish();
                })
                .show();
    }
}
