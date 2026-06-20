package com.curasync.models;

import java.sql.Date;

public class Appointment {
    private int id;
    private int patientId;
    private int doctorId;
    private Date appointmentDate;
    private String timeSlot;
    private String status;

    public Appointment(int id, int patientId, int doctorId, Date appointmentDate, String timeSlot, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.timeSlot = timeSlot;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    
    public Date getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(Date appointmentDate) { this.appointmentDate = appointmentDate; }
    
    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
