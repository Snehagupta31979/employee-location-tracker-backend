package com.employeetracker.dto;

import java.time.LocalDateTime;

public class StopResponse {
    private Long id;
    private Double latitude;
    private Double longitude;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;
    private boolean ongoing;

    public StopResponse() {
    }

    public StopResponse(Long id, Double latitude, Double longitude, LocalDateTime startTime, LocalDateTime endTime,
                         Long durationMinutes, boolean ongoing) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.ongoing = ongoing;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Double latitude;
        private Double longitude;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationMinutes;
        private boolean ongoing;

        public Builder id(Long id) {
            this.id = id;
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

        public StopResponse build() {
            return new StopResponse(id, latitude, longitude, startTime, endTime, durationMinutes, ongoing);
        }
    }
}
