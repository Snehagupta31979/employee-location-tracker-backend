package com.employeetracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EmployeePlanResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate planDate;
    private String task;
    private String location;
    private String purpose;
    private LocalTime startTime;
    private LocalTime endTime;
    private String priority;
    private String status;
    private boolean edited;
    private LocalDateTime updatedAt;

    public EmployeePlanResponse(Long id, Long employeeId, String employeeName, LocalDate planDate,
                                 String task, String location, String purpose, LocalTime startTime,
                                 LocalTime endTime, String priority, String status, boolean edited,
                                 LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.planDate = planDate;
        this.task = task;
        this.location = location;
        this.purpose = purpose;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.status = status;
        this.edited = edited;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getPlanDate() { return planDate; }
    public String getTask() { return task; }
    public String getLocation() { return location; }
    public String getPurpose() { return purpose; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public boolean isEdited() { return edited; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
