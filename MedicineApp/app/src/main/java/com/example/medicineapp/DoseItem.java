package com.example.medicineapp;

/** One scheduled dose for today (used by the dashboard). */
public class DoseItem {
    public final long medicineId;
    public final String name;
    public final String dosage;
    public final String time;   // HH:mm
    public final String status; // Pending, Taken or Missed

    public DoseItem(long medicineId, String name, String dosage, String time, String status) {
        this.medicineId = medicineId;
        this.name = name;
        this.dosage = dosage;
        this.time = time;
        this.status = status;
    }
}
