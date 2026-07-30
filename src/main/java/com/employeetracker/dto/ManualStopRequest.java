package com.employeetracker.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ManualStopRequest {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String address;
    private String reason;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
