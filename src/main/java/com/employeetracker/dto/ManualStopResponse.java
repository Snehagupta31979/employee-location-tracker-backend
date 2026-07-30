package com.employeetracker.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ManualStopResponse {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;
    private String address;
    private String reason;

    public ManualStopResponse(Long id, Long employeeId, String employeeName, LocalDate date,
                               LocalDateTime startTime, LocalDateTime endTime, Long durationMinutes,
                               String address, String reason) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMinutes = durationMinutes;
        this.address = address;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getDate() { return date; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Long getDurationMinutes() { return durationMinutes; }
    public String getAddress() { return address; }
    public String getReason() { return reason; }
}
