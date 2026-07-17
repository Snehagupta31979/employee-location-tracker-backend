package com.employeetracker.dto;

import java.time.LocalDateTime;

public class LocationResponse {
    private Long id;
    private Long userId;
    private String employeeName;
    private Double latitude;
    private Double longitude;
    private Double accuracy;
    private LocalDateTime recordedAt;
    private String status; // ONLINE, MOVING, STOPPED, OFFLINE

    public LocationResponse() {
    }

    public LocationResponse(Long id, Long userId, String employeeName, Double latitude, Double longitude,
                             Double accuracy, LocalDateTime recordedAt, String status) {
        this.id = id;
        this.userId = userId;
        this.employeeName = employeeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
        this.recordedAt = recordedAt;
        this.status = status;
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

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private String employeeName;
        private Double latitude;
        private Double longitude;
        private Double accuracy;
        private LocalDateTime recordedAt;
        private String status;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder employeeName(String employeeName) {
            this.employeeName = employeeName;
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

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public LocationResponse build() {
            return new LocationResponse(id, userId, employeeName, latitude, longitude, accuracy, recordedAt, status);
        }
    }
}
