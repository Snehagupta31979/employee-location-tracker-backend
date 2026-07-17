package com.employeetracker.service;

import com.employeetracker.dto.ActivityResponse;
import com.employeetracker.entity.EmployeeActivity;
import com.employeetracker.repository.EmployeeActivityRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class ActivityService {

    private final EmployeeActivityRepository activityRepository;

    public ActivityService(EmployeeActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public void log(Long userId, EmployeeActivity.ActivityType type, String description) {
        EmployeeActivity activity = EmployeeActivity.builder()
                .userId(userId)
                .activityType(type)
                .description(description)
                .activityTime(LocalDateTime.now())
                .build();
        activityRepository.save(activity);
    }

    public List<ActivityResponse> getTodayActivities(Long userId) {
        return getActivitiesForDate(userId, LocalDate.now());
    }

    public List<ActivityResponse> getActivitiesForDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        return activityRepository.findByUserIdAndActivityTimeBetweenOrderByActivityTimeAsc(userId, start, end)
                .stream()
                .map(a -> ActivityResponse.builder()
                        .id(a.getId())
                        .activityType(a.getActivityType().name())
                        .description(a.getDescription())
                        .activityTime(a.getActivityTime())
                        .build())
                .toList();
    }
    public int countTrackingStarts(Long userId, LocalDate date) {
        return (int) getActivitiesForDate(userId, date).stream()
                .filter(a -> "TRACKING_START".equals(a.getActivityType()))
                .count();
    }

    public int countTrackingStops(Long userId, LocalDate date) {
        return (int) getActivitiesForDate(userId, date).stream()
                .filter(a -> "TRACKING_END".equals(a.getActivityType()))
                .count();
    }

    public LocalDateTime getFirstTrackingStartTime(Long userId, LocalDate date) {
        return getActivitiesForDate(userId, date).stream()
                .filter(a -> "TRACKING_START".equals(a.getActivityType()))
                .map(ActivityResponse::getActivityTime)
                .findFirst()
                .orElse(null);
    }
    public LocalDateTime getLoginTime(Long userId, LocalDate date) {
        List<ActivityResponse> logins = getActivitiesForDate(userId, date).stream()
                .filter(a -> "LOGIN".equals(a.getActivityType()))
                .toList();
        return logins.isEmpty() ? null : logins.get(logins.size() - 1).getActivityTime();
    }

    public LocalDateTime getLogoutTime(Long userId, LocalDate date) {
        List<ActivityResponse> logouts = getActivitiesForDate(userId, date).stream()
                .filter(a -> "LOGOUT".equals(a.getActivityType()))
                .toList();
        return logouts.isEmpty() ? null : logouts.get(logouts.size() - 1).getActivityTime();
    }

    public List<com.employeetracker.dto.StopResponse> getTrackingSessions(Long userId, LocalDate date) {
        List<ActivityResponse> events = getActivitiesForDate(userId, date).stream()
                .filter(a -> "TRACKING_START".equals(a.getActivityType()) || "TRACKING_END".equals(a.getActivityType()))
                .toList();

        List<com.employeetracker.dto.StopResponse> sessions = new java.util.ArrayList<>();
        LocalDateTime openStart = null;
        for (ActivityResponse a : events) {
            if ("TRACKING_START".equals(a.getActivityType())) {
                openStart = a.getActivityTime();
            } else if (openStart != null) {
                long minutes = java.time.Duration.between(openStart, a.getActivityTime()).toMinutes();
                sessions.add(com.employeetracker.dto.StopResponse.builder()
                        .startTime(openStart)
                        .endTime(a.getActivityTime())
                        .durationMinutes(minutes)
                        .ongoing(false)
                        .build());
                openStart = null;
            }
        }
        if (openStart != null) {
            sessions.add(com.employeetracker.dto.StopResponse.builder()
                    .startTime(openStart)
                    .endTime(null)
                    .durationMinutes(java.time.Duration.between(openStart, LocalDateTime.now()).toMinutes())
                    .ongoing(true)
                    .build());
        }
        return sessions;
    }
}
