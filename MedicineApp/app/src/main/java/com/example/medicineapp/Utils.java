package com.example.medicineapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.widget.EditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class Utils {

    public static final String CHANNEL_ID = "medicine_reminders";
    public static final String STATUS_TAKEN = "Taken";
    public static final String STATUS_MISSED = "Missed";
    public static final String STATUS_PENDING = "Pending";

    private static final String DATE_PATTERN = "dd/MM/yyyy";

    private Utils() { }

    // ---------- dates ----------
    public static String formatDate(Date d) {
        return new SimpleDateFormat(DATE_PATTERN, Locale.US).format(d);
    }

    public static String today() {
        return formatDate(new Date());
    }

    /** Strict parse; returns null when the text is not a valid dd/MM/yyyy date. */
    public static Date parseDate(String s) {
        if (s == null) return null;
        try {
            SimpleDateFormat f = new SimpleDateFormat(DATE_PATTERN, Locale.US);
            f.setLenient(false);
            return f.parse(s.trim());
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isActiveOn(Medicine m, String date) {
        Date d = parseDate(date), s = parseDate(m.startDate), e = parseDate(m.endDate);
        if (d == null || s == null || e == null) return false;
        return !d.before(s) && !d.after(e);
    }

    // ---------- times ----------
    public static String formatTime24(int hour, int minute) {
        return String.format(Locale.US, "%02d:%02d", hour, minute);
    }

    public static String to12h(String hhmm) {
        try {
            Date d = new SimpleDateFormat("HH:mm", Locale.US).parse(hhmm);
            return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(d);
        } catch (Exception e) {
            return hhmm;
        }
    }

    // ---------- pickers ----------
    public static void pickDate(Context c, final EditText target) {
        Calendar cal = Calendar.getInstance();
        Date existing = parseDate(target.getText().toString());
        if (existing != null) cal.setTime(existing);
        new DatePickerDialog(c, (view, y, m, d) ->
                target.setText(String.format(Locale.US, "%02d/%02d/%04d", d, m + 1, y)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    /** Shows a time picker. The 24-hour value is kept in the view tag, the 12-hour text is displayed. */
    public static void pickTime(Context c, final EditText target) {
        int h = 8, m = 0;
        Object tag = target.getTag();
        if (tag instanceof String && ((String) tag).matches("\\d{2}:\\d{2}")) {
            h = Integer.parseInt(((String) tag).substring(0, 2));
            m = Integer.parseInt(((String) tag).substring(3, 5));
        }
        new TimePickerDialog(c, (view, hh, mm) -> {
            String s24 = formatTime24(hh, mm);
            target.setTag(s24);
            target.setText(to12h(s24));
        }, h, m, false).show();
    }

    public static String text(EditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    // ---------- system ----------
    public static void createChannel(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID,
                    c.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription(c.getString(R.string.channel_desc));
            NotificationManager nm = c.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    public static boolean canScheduleExact(Context c) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        return am != null && am.canScheduleExactAlarms();
    }
}
