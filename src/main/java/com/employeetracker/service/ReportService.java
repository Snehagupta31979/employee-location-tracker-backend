package com.employeetracker.service;

import com.employeetracker.dto.ReportResponse;
import com.employeetracker.entity.User;
import com.employeetracker.exception.ResourceNotFoundException;
import com.employeetracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ReportService {

    private final UserRepository userRepository;
    private final LocationService locationService;
    private final StopService stopService;
    private final DistanceService distanceService;
    private final ActivityService activityService;
    private final GeofenceService geofenceService;

    public ReportService(UserRepository userRepository, LocationService locationService,
            StopService stopService, DistanceService distanceService, ActivityService activityService,GeofenceService geofenceService) {
this.userRepository = userRepository;
this.locationService = locationService;
this.stopService = stopService;
this.distanceService = distanceService;
this.activityService = activityService;
this.geofenceService = geofenceService;
}

    public ReportResponse generateReport(Long userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return ReportResponse.builder()
                .userId(userId)
                .employeeName(user.getFullName())
                .date(targetDate)
                .totalDistanceKm(distanceService.getDistanceForDateKm(userId, targetDate))
                .locations(locationService.getHistory(userId, targetDate))
                .stops(stopService.getStopsForDate(userId, targetDate))
                .trackingStartCount(activityService.countTrackingStarts(userId, targetDate))
                .trackingStopCount(activityService.countTrackingStops(userId, targetDate))
                .firstStartTime(activityService.getFirstTrackingStartTime(userId, targetDate))
                .loginTime(activityService.getLoginTime(userId, targetDate))
                .logoutTime(activityService.getLogoutTime(userId, targetDate))
                .trackingSessions(activityService.getTrackingSessions(userId, targetDate))
                .geofenceSessions(geofenceService.getSessionsForDate(userId, targetDate))
                .build();
    }
}
