package com.employeetracker.dto;

import java.time.LocalDateTime;

public class EmployeeSummaryResponse {
    private Long id;
    private String username;
    private String fullName;
    private String status; // ONLINE, MOVING, STOPPED, OFFLINE
    private Double latitude;
    private Double longitude;
    private Double todayDistanceKm;
    private LocalDateTime lastUpdated;

    public EmployeeSummaryResponse() {
    }

    public EmployeeSummaryResponse(Long id, String username, String fullName, String status, Double latitude,
                                    Double longitude, Double todayDistanceKm, LocalDateTime lastUpdated) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.todayDistanceKm = todayDistanceKm;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Double getTodayDistanceKm() {
        return todayDistanceKm;
    }

    public void setTodayDistanceKm(Double todayDistanceKm) {
        this.todayDistanceKm = todayDistanceKm;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String username;
        private String fullName;
        private String status;
        private Double latitude;
        private Double longitude;
        private Double todayDistanceKm;
        private LocalDateTime lastUpdated;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
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

        public Builder todayDistanceKm(Double todayDistanceKm) {
            this.todayDistanceKm = todayDistanceKm;
            return this;
        }

        public Builder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public EmployeeSummaryResponse build() {
            return new EmployeeSummaryResponse(id, username, fullName, status, latitude, longitude,
                    todayDistanceKm, lastUpdated);
        }
    }
}
