package com.employeetracker.dto;

public class GeofenceResponse {
    private Long id;
    private String name;
    private String type;
    private double latitude;
    private double longitude;
    private double radiusMeters;

    public GeofenceResponse(Long id, String name, String type, double latitude, double longitude, double radiusMeters) {
        this.id = id; this.name = name; this.type = type;
        this.latitude = latitude; this.longitude = longitude; this.radiusMeters = radiusMeters;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getRadiusMeters() { return radiusMeters; }
}