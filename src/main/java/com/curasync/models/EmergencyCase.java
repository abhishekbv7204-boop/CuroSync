package com.curasync.models;

public class EmergencyCase implements Comparable<EmergencyCase> {
    private int id;
    private String patientName;
    private int severityLevel; // 1-10
    private String timestamp;
    private String status;

    public EmergencyCase(int id, String patientName, int severityLevel, String timestamp, String status) {
        this.id = id;
        this.patientName = patientName;
        this.severityLevel = severityLevel;
        this.timestamp = timestamp;
        this.status = status;
    }

    public int getId() { return id; }
    public String getPatientName() { return patientName; }
    public int getSeverityLevel() { return severityLevel; }
    public String getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public int compareTo(EmergencyCase other) {
        // We want Max-Heap (Highest severity first)
        // If severities are equal, compare by ID (or timestamp) to maintain FIFO for equal priorities
        if (this.severityLevel == other.severityLevel) {
            return Integer.compare(this.id, other.id);
        }
        return Integer.compare(other.severityLevel, this.severityLevel);
    }

    @Override
    public String toString() {
        return patientName + " (Severity: " + severityLevel + ")";
    }
}
