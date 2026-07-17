package com.employeetracker.service;

import com.employeetracker.dto.DailyDistanceResponse;
import com.employeetracker.dto.StopResponse;
import com.employeetracker.dto.TimeDistributionResponse;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class AnalyticsService {

    private final DistanceService distanceService;
    private final ActivityService activityService;
    private final StopService stopService;

    public AnalyticsService(DistanceService distanceService, ActivityService activityService, StopService stopService) {
        this.distanceService = distanceService;
        this.activityService = activityService;
        this.stopService = stopService;
    }

    public List<DailyDistanceResponse> getDailyDistance(Long userId, LocalDate referenceDate) {
        List<DailyDistanceResponse> result = new ArrayList<>();
        LocalDate monday = referenceDate.with(java.time.DayOfWeek.MONDAY);
        for (int i = 0; i < 6; i++) {
            LocalDate d = monday.plusDays(i);
            double km = distanceService.getDistanceForDateKm(userId, d);
            String label = d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            result.add(new DailyDistanceResponse(d, label, Math.round(km * 100.0) / 100.0));
        }
        return result;
    }
    public TimeDistributionResponse getTimeDistribution(Long userId, LocalDate date) {
        LocalDateTime loginTime = activityService.getLoginTime(userId, date);
        LocalDateTime logoutTime = activityService.getLogoutTime(userId, date);

        if (loginTime == null) {
            return new TimeDistributionResponse(0, 0, 0);
        }
        LocalDateTime windowEnd = logoutTime != null ? logoutTime : LocalDateTime.now();
        long dayWindowMinutes = Math.max(0, Duration.between(loginTime, windowEnd).toMinutes());

        long trackingActiveMinutes = activityService.getTrackingSessions(userId, date).stream()
                .mapToLong(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 0)
                .sum();

        long stoppedMinutes = stopService.getStopsForDate(userId, date).stream()
                .mapToLong(StopResponse::getDurationMinutes)
                .sum();

        long movingMinutes = Math.max(0, trackingActiveMinutes - stoppedMinutes);
        long offlineMinutes = Math.max(0, dayWindowMinutes - trackingActiveMinutes);

        return new TimeDistributionResponse(movingMinutes, stoppedMinutes, offlineMinutes);
    }
}