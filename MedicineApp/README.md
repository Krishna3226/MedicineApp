# Medicine Reminder & Health Diary (Android, Java + XML)

Package `com.example.medicineapp` · minSdk 24 · compile/target 34 · Java 11 · SQLite · AlarmManager

## 1. Build the APK (about 5 minutes)
1. Install **Android Studio** (Hedgehog or newer). It bundles the JDK 17 that AGP 8.2.2 needs.
2. **File > Open** and pick the `MedicineApp` folder. Let Gradle sync finish (needs internet the first time).
   If Studio asks to install SDK Platform 34, click *Install*.
3. **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Click *locate* in the popup. The file is `app/build/outputs/apk/debug/app-debug.apk`.
   (It is signed with the automatic debug key, so it installs on any phone.)

## 2. Install on your phone
- Send `app-debug.apk` to the phone (USB, Drive, WhatsApp, email).
- Tap it. If asked, allow **Install unknown apps** for the app you opened it from.
- Or: enable USB debugging, plug in, and press **Run** in Android Studio.

## 3. Permissions (grant on first launch)
| Permission | Why | How to grant |
|---|---|---|
| Notifications (Android 13+) | show reminders | tap **Allow** in the popup, or Settings > Apps > Medicine Reminder > Notifications |
| Alarms & reminders (Android 12+) | on-time reminders | tap **Open settings** in the dialog and switch it on. Without it, reminders still work but may be late |
| Receive boot completed | re-create alarms after restart | automatic |

Battery tip: on Xiaomi/Oppo/Vivo/Samsung set battery to *Unrestricted* for the app, or the OS may delay alarms.

## 4. Rules built into the app
- Dates `dd/MM/yyyy`. Times stored `HH:mm` (24h), shown in 12h.
- Medicine times stored as `"08:00,20:00"`. Alarm request code = `medicineId*10 + timeIndex`.
- Dose with no history row = **Pending**. It becomes Taken/Missed only when you tap the notification button. Nothing is auto-marked.
- Duplicate protection: `UNIQUE(medicine_id, scheduled_date, scheduled_time)` on history.
- Deleting a medicine cancels its alarms and removes the medicine row. Its history is kept (name and dosage are saved in each history row).
- Past reminder times roll to the next day. Time-zone, clock change, reboot and app update trigger a reschedule.

## 5. Test checklist
1. Gradle sync + build succeed. 2. App opens on the splash, then the dashboard.
3. Add a medicine with one time 2 minutes from now. 4. It appears in the list and details are correct.
5. Edit it (change the time) and save. 6. Notification arrives with **Taken** / **Missed**.
7. Tap Taken: History shows it, dashboard counts update. 8. Add a medicine with 3 times: all three fire.
9. Delete a medicine: confirm dialog, no more reminders, history stays.
10. Add, edit, delete a diary entry. 11. Close and reopen the app: data is still there.
12. Restart the phone: reminders still fire. 13. Deny notifications: app does not crash.
14. Test on Android 7/8 and a recent version.

## 6. Project report (short)
**Objective:** an offline app that reminds users to take medicines, records Taken/Missed, and keeps a private health diary.
**Modules:** Splash, Dashboard, Medicine CRUD, Reminder engine (AlarmManager + receivers), History, Health Diary.
**Tech:** Java, XML, Material Components, RecyclerView, SQLite, AlarmManager, NotificationCompat.
**Database:**
- `medicines(id, name, dosage, frequency, reminder_time, start_date, end_date, notes)`
- `medicine_history(id, medicine_id, medicine_name, dosage, scheduled_date, scheduled_time, status, recorded_at)` with a UNIQUE dose key
- `health_diary(id, date, title, symptoms, notes)`

## 7. Viva notes
- **Activity**: one screen with a lifecycle. `BaseActivity` holds shared DB access and back navigation.
- **Intent**: message to start an Activity or pass data (`putExtra(EXTRA_ID, id)`). Explicit intents name the class.
- **RecyclerView**: reuses row views. Needs LayoutManager + Adapter + ViewHolder.
- **SQLite / SQLiteOpenHelper**: `onCreate` builds tables, `onUpgrade` handles version changes. `ContentValues` + `?` args prevent SQL injection.
- **CRUD**: Create `insert`, Read `query`, Update `update`, Delete `delete`.
- **AlarmManager**: runs code at a clock time even when the app is closed. We use `setExactAndAllowWhileIdle`.
- **PendingIntent**: a token that lets the system fire our Intent later. Unique request code + data URI per dose avoids overwriting. `FLAG_IMMUTABLE` for security.
- **BroadcastReceiver**: `ReminderReceiver` shows the notification, `NotificationActionReceiver` saves Taken/Missed, `BootReceiver` restores alarms.
- **Notification channel**: required from Android 8 to control importance/sound.
- **Why re-schedule each day?** One-shot exact alarms are more reliable than repeating ones and let us stop at the end date.
