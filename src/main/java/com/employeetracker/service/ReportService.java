package com.employeetracker.service;

import com.employeetracker.dto.ReportResponse;
import com.employeetracker.dto.LocationResponse;
import com.employeetracker.dto.StopResponse;
import com.employeetracker.dto.GeofenceSessionResponse;
import com.employeetracker.entity.User;
import com.employeetracker.exception.ResourceNotFoundException;
import com.employeetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public ReportResponse generateReport(Long userId, LocalDate fromDate, LocalDate toDate) {
        LocalDate start = fromDate != null ? fromDate : LocalDate.now();
        LocalDate end = toDate != null ? toDate : LocalDate.now();
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        double totalDistance = 0;
        List<LocationResponse> allLocations = new ArrayList<>();
        List<StopResponse> allStops = new ArrayList<>();
        List<StopResponse> allSessions = new ArrayList<>();
        List<GeofenceSessionResponse> allGeofenceSessions = new ArrayList<>();
        int totalStarts = 0;
        int totalStops = 0;
        LocalDateTime earliestFirstStart = null;
        LocalDateTime earliestLogin = null;
        LocalDateTime latestLogout = null;

        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            totalDistance += distanceService.getDistanceForDateKm(userId, cursor);
            allLocations.addAll(locationService.getHistory(userId, cursor));
            allStops.addAll(stopService.getStopsForDate(userId, cursor));
            totalStarts += activityService.countTrackingStarts(userId, cursor);
            totalStops += activityService.countTrackingStops(userId, cursor);

            LocalDateTime dayFirstStart = activityService.getFirstTrackingStartTime(userId, cursor);
            if (dayFirstStart != null && (earliestFirstStart == null || dayFirstStart.isBefore(earliestFirstStart))) {
                earliestFirstStart = dayFirstStart;
            }

            LocalDateTime dayLogin = activityService.getLoginTime(userId, cursor);
            if (dayLogin != null && (earliestLogin == null || dayLogin.isBefore(earliestLogin))) {
                earliestLogin = dayLogin;
            }

            LocalDateTime dayLogout = activityService.getLogoutTime(userId, cursor);
            if (dayLogout != null && (latestLogout == null || dayLogout.isAfter(latestLogout))) {
                latestLogout = dayLogout;
            }

            allSessions.addAll(activityService.getTrackingSessions(userId, cursor));
            allGeofenceSessions.addAll(geofenceService.getSessionsForDate(userId, cursor));

            cursor = cursor.plusDays(1);
        }

        return ReportResponse.builder()
                .userId(userId)
                .employeeName(user.getFullName())
                .date(start)
                .totalDistanceKm(totalDistance)
                .locations(allLocations)
                .stops(allStops)
                .trackingStartCount(totalStarts)
                .trackingStopCount(totalStops)
                .firstStartTime(earliestFirstStart)
                .loginTime(earliestLogin)
                .logoutTime(latestLogout)
                .trackingSessions(allSessions)
                .geofenceSessions(allGeofenceSessions)
                .build();
    }
}
