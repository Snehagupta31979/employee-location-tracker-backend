package com.employeetracker.dto;

public class TimeDistributionResponse {
    private long movingMinutes;
    private long stoppedMinutes;
    private long offlineMinutes;

    public TimeDistributionResponse(long movingMinutes, long stoppedMinutes, long offlineMinutes) {
        this.movingMinutes = movingMinutes;
        this.stoppedMinutes = stoppedMinutes;
        this.offlineMinutes = offlineMinutes;
    }

    public long getMovingMinutes() { return movingMinutes; }
    public long getStoppedMinutes() { return stoppedMinutes; }
    public long getOfflineMinutes() { return offlineMinutes; }
}