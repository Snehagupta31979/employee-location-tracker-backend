package com.employeetracker.dto;

import java.time.LocalDate;

public class DailyDistanceResponse {
    private LocalDate date;
    private String label;
    private double distanceKm;

    public DailyDistanceResponse(LocalDate date, String label, double distanceKm) {
        this.date = date; this.label = label; this.distanceKm = distanceKm;
    }

    public LocalDate getDate() { return date; }
    public String getLabel() { return label; }
    public double getDistanceKm() { return distanceKm; }
}