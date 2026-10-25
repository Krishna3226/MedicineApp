package com.example.medicineapp;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationManagerCompat;

/** Handles the Taken / Missed buttons on the reminder notification (no screen is opened). */
public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String ACTION = "com.example.medicineapp.SET_DOSE_STATUS";
    private static final String X_ID = "id", X_NAME = "name", X_DOSAGE = "dosage",
            X_DATE = "date", X_TIME = "time", X_STATUS = "status", X_NOTIF = "notif";

    public static PendingIntent pending(Context c, Medicine m, int idx, String date,
                                        String time, String status) {
        Intent i = new Intent(c, NotificationActionReceiver.class);
        i.setAction(ACTION);
        i.setData(Uri.parse("medicine://dose/" + m.id + "/" + date.replace('/', '-') + "/"
                + time.replace(":", "") + "/" + status));
        i.putExtra(X_ID, m.id);
        i.putExtra(X_NAME, m.name);
        i.putExtra(X_DOSAGE, m.dosage);
        i.putExtra(X_DATE, date);
        i.putExtra(X_TIME, time);
        i.putExtra(X_STATUS, status);
        i.putExtra(X_NOTIF, ReminderScheduler.requestCode(m.id, idx));
        int code = ReminderScheduler.requestCode(m.id, idx) * 2
                + (Utils.STATUS_TAKEN.equals(status) ? 0 : 1);
        return PendingIntent.getBroadcast(c, code, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(X_ID, -1);
        String date = intent.getStringExtra(X_DATE);
        String time = intent.getStringExtra(X_TIME);
        String status = intent.getStringExtra(X_STATUS);
        if (id < 0 || date == null || time == null || status == null) return;

        DatabaseHelper.getInstance(context).setDoseStatus(id,
                intent.getStringExtra(X_NAME), intent.getStringExtra(X_DOSAGE), date, time, status);
        NotificationManagerCompat.from(context).cancel(intent.getIntExtra(X_NOTIF, 0));
    }
}
