package com.example.medicineapp;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

/** Fires at a scheduled time: shows the notification, then schedules the next day's alarm. */
public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(ReminderScheduler.EXTRA_MEDICINE_ID, -1);
        int idx = intent.getIntExtra(ReminderScheduler.EXTRA_TIME_INDEX, -1);
        if (id < 0 || idx < 0) return;

        DatabaseHelper db = DatabaseHelper.getInstance(context);
        Medicine m = db.getMedicine(id);
        if (m == null) {                       // medicine was deleted
            ReminderScheduler.cancel(context, id);
            return;
        }
        List<String> times = m.getTimes();
        if (idx >= times.size()) return;

        String time = times.get(idx);
        String today = Utils.today();
        if (Utils.isActiveOn(m, today) && db.getDoseStatus(id, today, time) == null) {
            showNotification(context, m, idx, today, time);
        }
        ReminderScheduler.scheduleNext(context, m, idx);
    }

    @SuppressLint("MissingPermission")
    private void showNotification(Context ctx, Medicine m, int idx, String date, String time) {
        NotificationManagerCompat nm = NotificationManagerCompat.from(ctx);
        if (!nm.areNotificationsEnabled()) return;   // user denied notifications
        Utils.createChannel(ctx);

        Intent open = new Intent(ctx, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent content = PendingIntent.getActivity(ctx, ReminderScheduler.requestCode(m.id, idx),
                open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, Utils.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medicine)
                .setContentTitle(ctx.getString(R.string.notif_title, m.name))
                .setContentText(ctx.getString(R.string.notif_text, m.dosage, Utils.to12h(time)))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(content)
                .addAction(0, ctx.getString(R.string.action_taken),
                        NotificationActionReceiver.pending(ctx, m, idx, date, time, Utils.STATUS_TAKEN))
                .addAction(0, ctx.getString(R.string.action_missed),
                        NotificationActionReceiver.pending(ctx, m, idx, date, time, Utils.STATUS_MISSED));

        nm.notify(ReminderScheduler.requestCode(m.id, idx), b.build());
    }
}
