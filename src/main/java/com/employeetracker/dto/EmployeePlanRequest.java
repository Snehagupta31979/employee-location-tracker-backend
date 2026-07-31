package com.employeetracker.dto;

import java.time.LocalTime;

public class EmployeePlanRequest {
    private String task;
    private String location;
    private String purpose;
    private LocalTime startTime;
    private LocalTime endTime;
    private String priority;
    private String status;

    public String getTask() { return task; }
    public void setTask(String task) { this.task = task; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
