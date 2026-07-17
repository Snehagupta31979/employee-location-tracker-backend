package com.employeetracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "EmployeeStops", indexes = {
        @Index(name = "idx_stop_user_time", columnList = "user_id,start_time")
})
public class EmployeeStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Long durationMinutes;

    /** true while the stop is still ongoing (endTime not yet finalized) */
    @Column(name = "ongoing", nullable = false)
    private boolean ongoing;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public EmployeeStop() {
    }

    public EmployeeStop(Long id, Long userId, Double latitude, Double longitude, LocalDateTime startTime,
                         LocalDateTime endTime, Long durationMinutes, boolean ongoing, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.ongoing = ongoing;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isOngoing() {
        return ongoing;
    }

    public void setOngoing(boolean ongoing) {
        this.ongoing = ongoing;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private Double latitude;
        private Double longitude;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationMinutes;
        private boolean ongoing;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder durationMinutes(Long durationMinutes) {
            this.durationMinutes = durationMinutes;
            return this;
        }

        public Builder ongoing(boolean ongoing) {
            this.ongoing = ongoing;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public EmployeeStop build() {
            return new EmployeeStop(id, userId, latitude, longitude, startTime, endTime, durationMinutes, ongoing, createdAt);
        }
    }
}
