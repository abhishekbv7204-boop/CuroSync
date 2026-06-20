package com.curasync.models;

public class Doctor {
    private int id;
    private String name;
    private String specialization;
    private String contact;
    private boolean availabilityStatus;
    private int hospitalId;

    public Doctor() {}

    public Doctor(int id, String name, String specialization, String contact, boolean availabilityStatus) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.contact = contact;
        this.availabilityStatus = availabilityStatus;
    }

    public int getHospitalId() { return hospitalId; }
    public void setHospitalId(int hospitalId) { this.hospitalId = hospitalId; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    
    public boolean isAvailable() { return availabilityStatus; }
    public void setAvailabilityStatus(boolean availabilityStatus) { this.availabilityStatus = availabilityStatus; }

    @Override
    public String toString() {
        return name + " - " + specialization;
    }
}
