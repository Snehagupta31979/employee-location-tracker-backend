package com.employeetracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "EmployeeLocation", indexes = {
        @Index(name = "idx_location_user_time", columnList = "user_id,recorded_at")
})
public class EmployeeLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public EmployeeLocation() {
    }

    public EmployeeLocation(Long id, Long userId, Double latitude, Double longitude, Double accuracy,
                             LocalDateTime recordedAt, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.recordedAt = recordedAt;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
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

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
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
        private Double accuracy;
        private LocalDateTime recordedAt;
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

        public Builder accuracy(Double accuracy) {
            this.accuracy = accuracy;
            return this;
        }

        public Builder recordedAt(LocalDateTime recordedAt) {
            this.recordedAt = recordedAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public EmployeeLocation build() {
            return new EmployeeLocation(id, userId, latitude, longitude, accuracy, recordedAt, createdAt);
        }
    }
}
