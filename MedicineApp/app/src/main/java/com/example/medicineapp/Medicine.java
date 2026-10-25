package com.example.medicineapp;

import java.util.ArrayList;
import java.util.List;

/** A medicine and its schedule. reminderTime holds 24-hour times joined by commas, e.g. "08:00,20:00". */
public class Medicine {
    public long id;
    public String name;
    public String dosage;
    public String frequency;     // "1", "2" or "3"
    public String reminderTime;  // "HH:mm,HH:mm"
    public String startDate;     // dd/MM/yyyy
    public String endDate;       // dd/MM/yyyy
    public String notes;

    public List<String> getTimes() {
        List<String> times = new ArrayList<>();
        if (reminderTime != null) {
            for (String t : reminderTime.split(",")) {
                if (!t.trim().isEmpty()) times.add(t.trim());
            }
        }
        return times;
    }

    public String getTimesDisplay() {
        StringBuilder sb = new StringBuilder();
        for (String t : getTimes()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(Utils.to12h(t));
        }
        return sb.toString();
    }

    public int getFrequencyCount() {
        try {
            return Math.max(1, Math.min(3, Integer.parseInt(frequency)));
        } catch (Exception e) {
            return 1;
        }
    }
}
