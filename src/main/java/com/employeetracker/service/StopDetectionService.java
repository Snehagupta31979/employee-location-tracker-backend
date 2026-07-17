package com.employeetracker.service;

import com.employeetracker.config.TrackingProperties;
import com.employeetracker.entity.EmployeeActivity;
import com.employeetracker.entity.EmployeeLocation;
import com.employeetracker.entity.EmployeeStop;
import com.employeetracker.repository.EmployeeLocationRepository;
import com.employeetracker.repository.EmployeeStopRepository;
import com.employeetracker.util.HaversineUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Detects stops: an employee is considered "stopped" when they remain within
 * tracking.stop.radius-meters of a point for at least tracking.stop.duration-minutes.
 */
@Service
public class StopDetectionService {

    private final EmployeeLocationRepository locationRepository;
    private final EmployeeStopRepository stopRepository;
    private final ActivityService activityService;
    private final TrackingProperties trackingProperties;

    public StopDetectionService(EmployeeLocationRepository locationRepository, EmployeeStopRepository stopRepository,
                                 ActivityService activityService, TrackingProperties trackingProperties) {
        this.locationRepository = locationRepository;
        this.stopRepository = stopRepository;
        this.activityService = activityService;
        this.trackingProperties = trackingProperties;
    }

    @Transactional
    public void processNewLocation(Long userId, EmployeeLocation newLocation) {
        double radius = trackingProperties.getStop().getRadiusMeters();
        int durationMinutes = trackingProperties.getStop().getDurationMinutes();

        Optional<EmployeeStop> ongoingOpt = stopRepository.findFirstByUserIdAndOngoingTrueOrderByStartTimeDesc(userId);

        if (ongoingOpt.isPresent()) {
            EmployeeStop ongoing = ongoingOpt.get();
            double distance = HaversineUtil.distanceInMeters(
                    ongoing.getLatitude(), ongoing.getLongitude(),
                    newLocation.getLatitude(), newLocation.getLongitude());

            if (distance <= radius) {
                // Still within the stop radius - extend the stop
                ongoing.setEndTime(newLocation.getRecordedAt());
                ongoing.setDurationMinutes(
                        Duration.between(ongoing.getStartTime(), newLocation.getRecordedAt()).toMinutes());
                stopRepository.save(ongoing);
            } else {
                // Employee has moved away - finalize the stop
                ongoing.setOngoing(false);
                stopRepository.save(ongoing);
                activityService.log(userId, EmployeeActivity.ActivityType.STOP_END,
                        String.format("Stop ended after %d minute(s)", ongoing.getDurationMinutes() == null ? 0 : ongoing.getDurationMinutes()));
            }
            return;
        }

        // No ongoing stop - look back over the recent window to see if the employee
        // has been dwelling within the stop radius long enough to register a new stop.
        LocalDateTime windowStart = newLocation.getRecordedAt().minusMinutes(durationMinutes);
        List<EmployeeLocation> recent = locationRepository
                .findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, windowStart, newLocation.getRecordedAt());

        if (recent.isEmpty()) {
            return;
        }

        boolean allWithinRadius = recent.stream().allMatch(loc ->
                HaversineUtil.distanceInMeters(loc.getLatitude(), loc.getLongitude(),
                        newLocation.getLatitude(), newLocation.getLongitude()) <= radius);

        EmployeeLocation earliest = recent.get(0);
        long spanMinutes = Duration.between(earliest.getRecordedAt(), newLocation.getRecordedAt()).toMinutes();

        if (allWithinRadius && spanMinutes >= durationMinutes) {
            EmployeeStop stop = EmployeeStop.builder()
                    .userId(userId)
                    .latitude(newLocation.getLatitude())
                    .longitude(newLocation.getLongitude())
                    .startTime(earliest.getRecordedAt())
                    .endTime(newLocation.getRecordedAt())
                    .durationMinutes(spanMinutes)
                    .ongoing(true)
                    .build();
            stopRepository.save(stop);
            activityService.log(userId, EmployeeActivity.ActivityType.STOP_START,
                    String.format("Employee stopped moving (within %.0fm radius)", radius));
        }
    }
}
