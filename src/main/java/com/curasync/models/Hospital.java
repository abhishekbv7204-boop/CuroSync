package com.curasync.models;

public class Hospital {
    private int id;
    private String name;
    private int locationNode;
    private String contact;
    private double lat;
    private double lng;
    private String specializations;
    private String address;
    private String district;

    public Hospital(int id, String name, int locationNode, String contact, double lat, double lng) {
        this(id, name, locationNode, contact, lat, lng, "General Medicine", "India", "Karnataka");
    }

    public Hospital(int id, String name, int locationNode, String contact, double lat, double lng, String specializations, String address, String district) {
        this.id = id;
        this.name = name;
        this.locationNode = locationNode;
        this.contact = contact;
        this.lat = lat;
        this.lng = lng;
        this.specializations = specializations;
        this.address = address;
        this.district = district;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getLocationNode() { return locationNode; }
    public String getContact() { return contact; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getSpecializations() { return specializations; }
    public String getAddress() { return address; }
    public String getDistrict() { return district; }

    @Override
    public String toString() {
        return name;
    }
}

