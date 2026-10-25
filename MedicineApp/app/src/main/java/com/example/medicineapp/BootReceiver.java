package com.example.medicineapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Re-creates all alarms after reboot, app update, clock change or time-zone change. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        ReminderScheduler.rescheduleAll(context);
    }
}
