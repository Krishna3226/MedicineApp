package com.example.medicineapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Schedules one alarm per (medicine, reminder time). Each alarm fires once, then
 * ReminderReceiver schedules the next day's alarm. Request code = medicineId * 10 + timeIndex.
 */
public final class ReminderScheduler {

    public static final String EXTRA_MEDICINE_ID = "medicine_id";
    public static final String EXTRA_TIME_INDEX = "time_index";
    private static final int MAX_TIMES = 3;

    private ReminderScheduler() { }

    public static int requestCode(long medicineId, int timeIndex) {
        return (int) (medicineId * 10 + timeIndex);
    }

    private static PendingIntent buildIntent(Context ctx, long medicineId, int idx) {
        Intent i = new Intent(ctx, ReminderReceiver.class);
        i.setData(Uri.parse("medicine://reminder/" + medicineId + "/" + idx)); // unique identity
        i.putExtra(EXTRA_MEDICINE_ID, medicineId);
        i.putExtra(EXTRA_TIME_INDEX, idx);
        return PendingIntent.getBroadcast(ctx, requestCode(medicineId, idx), i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** Cancels old alarms for this medicine, then schedules every reminder time. */
    public static void scheduleAll(Context ctx, Medicine m) {
        cancel(ctx, m.id);
        List<String> times = m.getTimes();
        for (int i = 0; i < times.size() && i < MAX_TIMES; i++) scheduleNext(ctx, m, i);
    }

    /** Schedules the next occurrence of one reminder time (does nothing once the course is over). */
    public static void scheduleNext(Context ctx, Medicine m, int idx) {
        List<String> times = m.getTimes();
        if (idx < 0 || idx >= times.size()) return;
        long trigger = nextTriggerMillis(m, times.get(idx), System.currentTimeMillis());
        if (trigger < 0) return;

        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = buildIntent(ctx, m.id, idx);
        if (Utils.canScheduleExact(ctx)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
        } else {
            // Exact alarm access was not granted: fall back to an inexact alarm (may be delayed).
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi);
        }
    }

    /**
     * Next future moment at hh:mm that lies inside [startDate, endDate], or -1.
     * Uses wall-clock Calendar arithmetic in the current time zone, so DST and zone changes are safe.
     */
    static long nextTriggerMillis(Medicine m, String hhmm, long now) {
        Date start = Utils.parseDate(m.startDate);
        Date end = Utils.parseDate(m.endDate);
        if (start == null || end == null || hhmm == null || !hhmm.matches("\\d{2}:\\d{2}")) return -1;
        int h = Integer.parseInt(hhmm.substring(0, 2));
        int min = Integer.parseInt(hhmm.substring(3, 5));

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        setTime(startCal, h, min);

        Calendar cand = Calendar.getInstance();
        cand.setTimeInMillis(now);
        setTime(cand, h, min);                       // today at hh:mm
        if (cand.before(startCal)) cand = startCal;  // course has not started yet
        if (cand.getTimeInMillis() <= now) cand.add(Calendar.DAY_OF_MONTH, 1); // time already passed

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        if (cand.after(endCal)) return -1;           // course finished
        return cand.getTimeInMillis();
    }

    private static void setTime(Calendar c, int h, int min) {
        c.set(Calendar.HOUR_OF_DAY, h);
        c.set(Calendar.MINUTE, min);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
    }

    public static void cancel(Context ctx, long medicineId) {
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        for (int i = 0; i < MAX_TIMES; i++) {
            PendingIntent pi = buildIntent(ctx, medicineId, i);
            am.cancel(pi);
            pi.cancel();
        }
    }

    /** Used after reboot, app update and time or time-zone changes. */
    public static void rescheduleAll(Context ctx) {
        for (Medicine m : DatabaseHelper.getInstance(ctx).getAllMedicines()) scheduleAll(ctx, m);
    }
}
