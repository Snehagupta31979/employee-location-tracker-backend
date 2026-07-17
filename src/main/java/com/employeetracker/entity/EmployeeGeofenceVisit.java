package com.employeetracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "EmployeeGeofenceVisits", indexes = {
        @Index(name = "idx_geo_visit_user_time", columnList = "user_id,entry_time")
})
public class EmployeeGeofenceVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "geofence_id", nullable = false)
    private Long geofenceId;

    @Column(name = "geofence_name", nullable = false, length = 150)
    private String geofenceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "geofence_type", nullable = false, length = 20)
    private Geofence.GeofenceType geofenceType;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "duration_minutes")
    private Long durationMinutes;

    @Column(nullable = false)
    private boolean ongoing;

    public EmployeeGeofenceVisit() {}

    public EmployeeGeofenceVisit(Long id, Long userId, Long geofenceId, String geofenceName,
                                  Geofence.GeofenceType geofenceType, LocalDateTime entryTime,
                                  LocalDateTime exitTime, Long durationMinutes, boolean ongoing) {
        this.id = id; this.userId = userId; this.geofenceId = geofenceId; this.geofenceName = geofenceName;
        this.geofenceType = geofenceType; this.entryTime = entryTime; this.exitTime = exitTime;
        this.durationMinutes = durationMinutes; this.ongoing = ongoing;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getGeofenceId() { return geofenceId; }
    public void setGeofenceId(Long geofenceId) { this.geofenceId = geofenceId; }
    public String getGeofenceName() { return geofenceName; }
    public void setGeofenceName(String geofenceName) { this.geofenceName = geofenceName; }
    public Geofence.GeofenceType getGeofenceType() { return geofenceType; }
    public void setGeofenceType(Geofence.GeofenceType geofenceType) { this.geofenceType = geofenceType; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }
    public boolean isOngoing() { return ongoing; }
    public void setOngoing(boolean ongoing) { this.ongoing = ongoing; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id, userId, geofenceId;
        private String geofenceName;
        private Geofence.GeofenceType geofenceType;
        private LocalDateTime entryTime, exitTime;
        private Long durationMinutes;
        private boolean ongoing;

        public Builder id(Long v) { this.id = v; return this; }
        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder geofenceId(Long v) { this.geofenceId = v; return this; }
        public Builder geofenceName(String v) { this.geofenceName = v; return this; }
        public Builder geofenceType(Geofence.GeofenceType v) { this.geofenceType = v; return this; }
        public Builder entryTime(LocalDateTime v) { this.entryTime = v; return this; }
        public Builder exitTime(LocalDateTime v) { this.exitTime = v; return this; }
        public Builder durationMinutes(Long v) { this.durationMinutes = v; return this; }
        public Builder ongoing(boolean v) { this.ongoing = v; return this; }

        public EmployeeGeofenceVisit build() {
            return new EmployeeGeofenceVisit(id, userId, geofenceId, geofenceName, geofenceType,
                    entryTime, exitTime, durationMinutes, ongoing);
        }
    }
}