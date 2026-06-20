package com.curasync.models;

public class Area {
    private int id;
    private String name;
    private double lat;
    private double lng;

    public Area(int id, String name, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }

    @Override
    public String toString() { return name + " (" + lat + ", " + lng + ")"; }
}
