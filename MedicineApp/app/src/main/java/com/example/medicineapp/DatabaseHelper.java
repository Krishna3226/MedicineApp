package com.example.medicineapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SQLite storage for medicines, dose history and the health diary.
 *
 * Deletion policy: deleting a medicine removes only the medicine row. History rows keep a
 * snapshot of the medicine name and dosage, so past records stay meaningful.
 * Dose policy: a dose with no history row is "Pending". A dose becomes Taken or Missed only
 * when the user taps the notification action. Nothing is marked automatically.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "medicine_app.db";
    private static final int DB_VERSION = 1;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE medicines ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "dosage TEXT NOT NULL,"
                + "frequency TEXT,"
                + "reminder_time TEXT NOT NULL,"
                + "start_date TEXT NOT NULL,"
                + "end_date TEXT NOT NULL,"
                + "notes TEXT)");
        db.execSQL("CREATE TABLE medicine_history ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "medicine_id INTEGER,"
                + "medicine_name TEXT,"
                + "dosage TEXT,"
                + "scheduled_date TEXT NOT NULL,"
                + "scheduled_time TEXT NOT NULL,"
                + "status TEXT NOT NULL,"
                + "recorded_at TEXT,"
                + "UNIQUE(medicine_id, scheduled_date, scheduled_time))");
        db.execSQL("CREATE TABLE health_diary ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "date TEXT NOT NULL,"
                + "title TEXT NOT NULL,"
                + "symptoms TEXT,"
                + "notes TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 only. Future versions must migrate with ALTER TABLE, never drop user data.
    }

    // =============== Medicines ===============

    private ContentValues toValues(Medicine m) {
        ContentValues cv = new ContentValues();
        cv.put("name", m.name);
        cv.put("dosage", m.dosage);
        cv.put("frequency", m.frequency);
        cv.put("reminder_time", m.reminderTime);
        cv.put("start_date", m.startDate);
        cv.put("end_date", m.endDate);
        cv.put("notes", m.notes);
        return cv;
    }

    private Medicine medicineFrom(Cursor c) {
        Medicine m = new Medicine();
        m.id = c.getLong(c.getColumnIndexOrThrow("id"));
        m.name = c.getString(c.getColumnIndexOrThrow("name"));
        m.dosage = c.getString(c.getColumnIndexOrThrow("dosage"));
        m.frequency = c.getString(c.getColumnIndexOrThrow("frequency"));
        m.reminderTime = c.getString(c.getColumnIndexOrThrow("reminder_time"));
        m.startDate = c.getString(c.getColumnIndexOrThrow("start_date"));
        m.endDate = c.getString(c.getColumnIndexOrThrow("end_date"));
        m.notes = c.getString(c.getColumnIndexOrThrow("notes"));
        return m;
    }

    public long addMedicine(Medicine m) {
        try {
            return getWritableDatabase().insertOrThrow("medicines", null, toValues(m));
        } catch (SQLiteException e) {
            Log.e(TAG, "addMedicine failed", e);
            return -1;
        }
    }

    public Medicine getMedicine(long id) {
        try (Cursor c = getReadableDatabase().query("medicines", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) return medicineFrom(c);
        } catch (SQLiteException e) {
            Log.e(TAG, "getMedicine failed", e);
        }
        return null;
    }

    public List<Medicine> getAllMedicines() {
        List<Medicine> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("medicines", null, null, null,
                null, null, "name COLLATE NOCASE ASC")) {
            while (c.moveToNext()) list.add(medicineFrom(c));
        } catch (SQLiteException e) {
            Log.e(TAG, "getAllMedicines failed", e);
        }
        return list;
    }

    public boolean updateMedicine(Medicine m) {
        try {
            return getWritableDatabase().update("medicines", toValues(m), "id=?",
                    new String[]{String.valueOf(m.id)}) > 0;
        } catch (SQLiteException e) {
            Log.e(TAG, "updateMedicine failed", e);
            return false;
        }
    }

    /** Deletes the medicine only. History rows are intentionally kept. */
    public void deleteMedicine(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("medicines", "id=?", new String[]{String.valueOf(id)});
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            Log.e(TAG, "deleteMedicine failed", e);
        } finally {
            db.endTransaction();
        }
    }

    public int getMedicineCount() {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM medicines", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        } catch (SQLiteException e) {
            Log.e(TAG, "getMedicineCount failed", e);
        }
        return 0;
    }

    // =============== Dose history ===============

    /** Inserts or replaces the record for this exact dose, so there is never a duplicate. */
    public boolean setDoseStatus(long medicineId, String name, String dosage,
                                 String date, String time, String status) {
        ContentValues cv = new ContentValues();
        cv.put("medicine_id", medicineId);
        cv.put("medicine_name", name);
        cv.put("dosage", dosage);
        cv.put("scheduled_date", date);
        cv.put("scheduled_time", time);
        cv.put("status", status);
        cv.put("recorded_at", new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(new Date()));
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long r = db.insertWithOnConflict("medicine_history", null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            db.setTransactionSuccessful();
            return r != -1;
        } catch (SQLiteException e) {
            Log.e(TAG, "setDoseStatus failed", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /** Returns Taken / Missed, or null when the dose is still pending. */
    public String getDoseStatus(long medicineId, String date, String time) {
        try (Cursor c = getReadableDatabase().query("medicine_history", new String[]{"status"},
                "medicine_id=? AND scheduled_date=? AND scheduled_time=?",
                new String[]{String.valueOf(medicineId), date, time}, null, null, null)) {
            if (c.moveToFirst()) return c.getString(0);
        } catch (SQLiteException e) {
            Log.e(TAG, "getDoseStatus failed", e);
        }
        return null;
    }

    public List<HistoryItem> getHistory() {
        List<HistoryItem> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("medicine_history", null, null, null,
                null, null, "id DESC")) {
            while (c.moveToNext()) {
                HistoryItem h = new HistoryItem();
                h.id = c.getLong(c.getColumnIndexOrThrow("id"));
                h.medicineId = c.getLong(c.getColumnIndexOrThrow("medicine_id"));
                h.medicineName = c.getString(c.getColumnIndexOrThrow("medicine_name"));
                h.dosage = c.getString(c.getColumnIndexOrThrow("dosage"));
                h.scheduledDate = c.getString(c.getColumnIndexOrThrow("scheduled_date"));
                h.scheduledTime = c.getString(c.getColumnIndexOrThrow("scheduled_time"));
                h.status = c.getString(c.getColumnIndexOrThrow("status"));
                list.add(h);
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "getHistory failed", e);
        }
        return list;
    }

    /** All of today's doses with their current status (Pending when no history row exists). */
    public List<DoseItem> getTodayDoses() {
        List<DoseItem> doses = new ArrayList<>();
        String today = Utils.today();
        for (Medicine m : getAllMedicines()) {
            if (!Utils.isActiveOn(m, today)) continue;
            for (String t : m.getTimes()) {
                String st = getDoseStatus(m.id, today, t);
                doses.add(new DoseItem(m.id, m.name, m.dosage, t,
                        st == null ? Utils.STATUS_PENDING : st));
            }
        }
        Collections.sort(doses, (a, b) -> a.time.compareTo(b.time));
        return doses;
    }

    // =============== Health diary ===============

    private ContentValues toValues(HealthEntry e) {
        ContentValues cv = new ContentValues();
        cv.put("date", e.date);
        cv.put("title", e.title);
        cv.put("symptoms", e.symptoms);
        cv.put("notes", e.notes);
        return cv;
    }

    private HealthEntry entryFrom(Cursor c) {
        HealthEntry e = new HealthEntry();
        e.id = c.getLong(c.getColumnIndexOrThrow("id"));
        e.date = c.getString(c.getColumnIndexOrThrow("date"));
        e.title = c.getString(c.getColumnIndexOrThrow("title"));
        e.symptoms = c.getString(c.getColumnIndexOrThrow("symptoms"));
        e.notes = c.getString(c.getColumnIndexOrThrow("notes"));
        return e;
    }

    public long addEntry(HealthEntry e) {
        try {
            return getWritableDatabase().insertOrThrow("health_diary", null, toValues(e));
        } catch (SQLiteException ex) {
            Log.e(TAG, "addEntry failed", ex);
            return -1;
        }
    }

    public HealthEntry getEntry(long id) {
        try (Cursor c = getReadableDatabase().query("health_diary", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) return entryFrom(c);
        } catch (SQLiteException e) {
            Log.e(TAG, "getEntry failed", e);
        }
        return null;
    }

    /** Newest date first. */
    public List<HealthEntry> getAllEntries() {
        List<HealthEntry> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().query("health_diary", null, null, null,
                null, null, "id DESC")) {
            while (c.moveToNext()) list.add(entryFrom(c));
        } catch (SQLiteException e) {
            Log.e(TAG, "getAllEntries failed", e);
        }
        Collections.sort(list, (a, b) -> {
            Date da = Utils.parseDate(a.date), db = Utils.parseDate(b.date);
            if (da == null || db == null) return 0;
            return db.compareTo(da);
        });
        return list;
    }

    public HealthEntry getLatestEntry() {
        List<HealthEntry> all = getAllEntries();
        return all.isEmpty() ? null : all.get(0);
    }

    public boolean updateEntry(HealthEntry e) {
        try {
            return getWritableDatabase().update("health_diary", toValues(e), "id=?",
                    new String[]{String.valueOf(e.id)}) > 0;
        } catch (SQLiteException ex) {
            Log.e(TAG, "updateEntry failed", ex);
            return false;
        }
    }

    public boolean deleteEntry(long id) {
        try {
            return getWritableDatabase().delete("health_diary", "id=?",
                    new String[]{String.valueOf(id)}) > 0;
        } catch (SQLiteException e) {
            Log.e(TAG, "deleteEntry failed", e);
            return false;
        }
    }
}
