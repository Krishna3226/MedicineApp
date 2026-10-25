package com.example.medicineapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    private TextView tvDate, tvMedCount, tvTodayDoses, tvTodayStatus, tvNext, tvLatestDiary;

    private final ActivityResultLauncher<String> notificationPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) toast(R.string.perm_notif_denied);
                checkExactAlarmAccess();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        tvDate = findViewById(R.id.tvDate);
        tvMedCount = findViewById(R.id.tvMedCount);
        tvTodayDoses = findViewById(R.id.tvTodayDoses);
        tvTodayStatus = findViewById(R.id.tvTodayStatus);
        tvNext = findViewById(R.id.tvNext);
        tvLatestDiary = findViewById(R.id.tvLatestDiary);

        findViewById(R.id.btnMedicines).setOnClickListener(v ->
                startActivity(new Intent(this, MedicineActivity.class)));
        findViewById(R.id.btnHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        findViewById(R.id.btnDiary).setOnClickListener(v ->
                startActivity(new Intent(this, HealthDiaryActivity.class)));

        Utils.createChannel(this);
        if (savedInstanceState == null) requestPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }

    private void refreshDashboard() {
        tvDate.setText(new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(new Date()));
        tvMedCount.setText(getString(R.string.medicines_saved, db.getMedicineCount()));

        List<DoseItem> doses = db.getTodayDoses();
        tvTodayDoses.setText(getString(R.string.doses_today, doses.size()));

        int taken = 0, missed = 0, pending = 0;
        String nowHm = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
        DoseItem next = null;
        for (DoseItem d : doses) {
            if (Utils.STATUS_TAKEN.equals(d.status)) taken++;
            else if (Utils.STATUS_MISSED.equals(d.status)) missed++;
            else {
                pending++;
                if (next == null && d.time.compareTo(nowHm) >= 0) next = d; // list is sorted by time
            }
        }
        if (doses.isEmpty()) {
            tvTodayStatus.setText(R.string.no_doses_today);
        } else {
            tvTodayStatus.setText(getString(R.string.status_today_prefix,
                    getString(R.string.status_summary, taken, missed, pending)));
        }
        tvNext.setText(next == null ? getString(R.string.no_more_today)
                : getString(R.string.next_reminder, next.name, next.dosage, Utils.to12h(next.time)));

        HealthEntry latest = db.getLatestEntry();
        tvLatestDiary.setText(latest == null ? getString(R.string.no_diary)
                : getString(R.string.latest_diary, latest.date, latest.title));
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            checkExactAlarmAccess();
        }
    }

    private void checkExactAlarmAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !Utils.canScheduleExact(this)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.perm_exact_title)
                    .setMessage(R.string.perm_exact_msg)
                    .setNegativeButton(R.string.later, null)
                    .setPositiveButton(R.string.open_settings, (d, w) -> {
                        Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(i);
                    })
                    .show();
        }
    }
}
