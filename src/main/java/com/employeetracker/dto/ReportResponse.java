package com.employeetracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReportResponse {

    private Long userId;
    private String employeeName;
    private LocalDate date;
    private double totalDistanceKm;
    private List<LocationResponse> locations;
    private List<StopResponse> stops;
    private int trackingStartCount;
    private int trackingStopCount;
    private LocalDateTime firstStartTime;
    private LocalDateTime loginTime;
    private LocalDateTime logoutTime;
    private List<StopResponse> trackingSessions;
    private List<GeofenceSessionResponse> geofenceSessions;

    public ReportResponse(Long userId, String employeeName, LocalDate date, double totalDistanceKm,
                           List<LocationResponse> locations, List<StopResponse> stops,
                           int trackingStartCount, int trackingStopCount, LocalDateTime firstStartTime,
                           LocalDateTime loginTime, LocalDateTime logoutTime,
                           List<StopResponse> trackingSessions,
                           List<GeofenceSessionResponse> geofenceSessions) {
        this.userId = userId;
        this.employeeName = employeeName;
        this.date = date;
        this.totalDistanceKm = totalDistanceKm;
        this.locations = locations;
        this.stops = stops;
        this.trackingStartCount = trackingStartCount;
        this.trackingStopCount = trackingStopCount;
        this.firstStartTime = firstStartTime;
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.trackingSessions = trackingSessions;
        this.geofenceSessions = geofenceSessions;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public double getTotalDistanceKm() { return totalDistanceKm; }
    public void setTotalDistanceKm(double totalDistanceKm) { this.totalDistanceKm = totalDistanceKm; }
    public List<LocationResponse> getLocations() { return locations; }
    public void setLocations(List<LocationResponse> locations) { this.locations = locations; }
    public List<StopResponse> getStops() { return stops; }
    public void setStops(List<StopResponse> stops) { this.stops = stops; }
    public int getTrackingStartCount() { return trackingStartCount; }
    public void setTrackingStartCount(int trackingStartCount) { this.trackingStartCount = trackingStartCount; }
    public int getTrackingStopCount() { return trackingStopCount; }
    public void setTrackingStopCount(int trackingStopCount) { this.trackingStopCount = trackingStopCount; }
    public LocalDateTime getFirstStartTime() { return firstStartTime; }
    public void setFirstStartTime(LocalDateTime firstStartTime) { this.firstStartTime = firstStartTime; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }
    public List<StopResponse> getTrackingSessions() { return trackingSessions; }
    public void setTrackingSessions(List<StopResponse> trackingSessions) { this.trackingSessions = trackingSessions; }
    public List<GeofenceSessionResponse> getGeofenceSessions() { return geofenceSessions; }
    public void setGeofenceSessions(List<GeofenceSessionResponse> geofenceSessions) { this.geofenceSessions = geofenceSessions; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long userId;
        private String employeeName;
        private LocalDate date;
        private double totalDistanceKm;
        private List<LocationResponse> locations;
        private List<StopResponse> stops;
        private int trackingStartCount;
        private int trackingStopCount;
        private LocalDateTime firstStartTime;
        private LocalDateTime loginTime;
        private LocalDateTime logoutTime;
        private List<StopResponse> trackingSessions;
        private List<GeofenceSessionResponse> geofenceSessions;

        public Builder userId(Long v) { this.userId = v; return this; }
        public Builder employeeName(String v) { this.employeeName = v; return this; }
        public Builder date(LocalDate v) { this.date = v; return this; }
        public Builder totalDistanceKm(double v) { this.totalDistanceKm = v; return this; }
        public Builder locations(List<LocationResponse> v) { this.locations = v; return this; }
        public Builder stops(List<StopResponse> v) { this.stops = v; return this; }
        public Builder trackingStartCount(int v) { this.trackingStartCount = v; return this; }
        public Builder trackingStopCount(int v) { this.trackingStopCount = v; return this; }
        public Builder firstStartTime(LocalDateTime v) { this.firstStartTime = v; return this; }
        public Builder loginTime(LocalDateTime v) { this.loginTime = v; return this; }
        public Builder logoutTime(LocalDateTime v) { this.logoutTime = v; return this; }
        public Builder trackingSessions(List<StopResponse> v) { this.trackingSessions = v; return this; }
        public Builder geofenceSessions(List<GeofenceSessionResponse> v) { this.geofenceSessions = v; return this; }

        public ReportResponse build() {
            return new ReportResponse(userId, employeeName, date, totalDistanceKm, locations, stops,
                    trackingStartCount, trackingStopCount, firstStartTime, loginTime, logoutTime,
                    trackingSessions, geofenceSessions);
        }
    }
}