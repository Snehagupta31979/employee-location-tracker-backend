package com.employeetracker.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "Geofences")
public class Geofence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GeofenceType type;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "radius_meters", nullable = false)
    private double radiusMeters;

    @Column(nullable = false)
    private boolean active = true;

    public enum GeofenceType { OFFICE, CLIENT }

    public Geofence() {}

    public Geofence(Long id, String name, GeofenceType type, double latitude, double longitude,
                     double radiusMeters, boolean active) {
        this.id = id; this.name = name; this.type = type;
        this.latitude = latitude; this.longitude = longitude;
        this.radiusMeters = radiusMeters; this.active = active;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public GeofenceType getType() { return type; }
    public void setType(GeofenceType type) { this.type = type; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public double getRadiusMeters() { return radiusMeters; }
    public void setRadiusMeters(double radiusMeters) { this.radiusMeters = radiusMeters; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}