package com.employeetracker.dto;

import java.time.LocalDateTime;

public class GeofenceSessionResponse {
    private String geofenceName;
    private String geofenceType;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Long durationMinutes;
    private boolean ongoing;

    public GeofenceSessionResponse(String geofenceName, String geofenceType, LocalDateTime entryTime,
                                    LocalDateTime exitTime, Long durationMinutes, boolean ongoing) {
        this.geofenceName = geofenceName; this.geofenceType = geofenceType;
        this.entryTime = entryTime; this.exitTime = exitTime;
        this.durationMinutes = durationMinutes; this.ongoing = ongoing;
    }

    public String getGeofenceName() { return geofenceName; }
    public String getGeofenceType() { return geofenceType; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public Long getDurationMinutes() { return durationMinutes; }
    public boolean isOngoing() { return ongoing; }
}