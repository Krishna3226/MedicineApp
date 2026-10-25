package com.example.medicineapp;

public class HistoryItem {
    public long id;
    public long medicineId;
    public String medicineName;
    public String dosage;
    public String scheduledDate;
    public String scheduledTime; // HH:mm
    public String status;        // Taken or Missed
}
