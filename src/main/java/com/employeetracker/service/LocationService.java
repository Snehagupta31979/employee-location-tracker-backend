package com.employeetracker.service;

import com.employeetracker.config.TrackingProperties;
import com.employeetracker.dto.LocationRequest;
import com.employeetracker.dto.LocationResponse;
import com.employeetracker.entity.EmployeeActivity;
import com.employeetracker.entity.EmployeeLocation;
import com.employeetracker.entity.EmployeeStop;
import com.employeetracker.entity.User;
import com.employeetracker.exception.ResourceNotFoundException;
import com.employeetracker.repository.EmployeeLocationRepository;
import com.employeetracker.repository.EmployeeStopRepository;
import com.employeetracker.repository.UserRepository;
import com.employeetracker.util.HaversineUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    private final EmployeeLocationRepository locationRepository;
    private final EmployeeStopRepository stopRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final StopDetectionService stopDetectionService;
    private final DistanceService distanceService;
    private final TrackingProperties trackingProperties;
    private final GeofenceService geofenceService;

    public LocationService(EmployeeLocationRepository locationRepository, EmployeeStopRepository stopRepository,
            UserRepository userRepository, ActivityService activityService,
            StopDetectionService stopDetectionService, DistanceService distanceService,
            TrackingProperties trackingProperties, GeofenceService geofenceService) {
this.locationRepository = locationRepository;
this.stopRepository = stopRepository;
this.userRepository = userRepository;
this.activityService = activityService;
this.stopDetectionService = stopDetectionService;
this.distanceService = distanceService;
this.trackingProperties = trackingProperties;
this.geofenceService = geofenceService;
}

    @Transactional
    public LocationResponse saveLocation(Long userId, LocationRequest request) {
        EmployeeLocation location = EmployeeLocation.builder()
                .userId(userId)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracy(request.getAccuracy())
                .recordedAt(LocalDateTime.now())
                .build();

        location = locationRepository.save(location);

        stopDetectionService.processNewLocation(userId, location);
        geofenceService.evaluate(userId, location.getLatitude(), location.getLongitude());

        activityService.log(userId, EmployeeActivity.ActivityType.LOCATION_UPDATE,
                String.format("Location updated: %.6f, %.6f", location.getLatitude(), location.getLongitude()));

        return toResponse(location, userId);
    }

    public LocationResponse getCurrentLocation(Long userId) {
        EmployeeLocation location = locationRepository.findFirstByUserIdOrderByRecordedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No location recorded yet for this employee"));
        return toResponse(location, userId);
    }

    public List<LocationResponse> getHistory(Long userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.atTime(LocalTime.MAX);

        return locationRepository.findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, start, end)
                .stream()
                .map(loc -> toResponse(loc, userId))
                .toList();
    }

    public double getTodayDistanceKm(Long userId) {
        return distanceService.getTodayDistanceKm(userId);
    }

    /**
     * Determines the employee's current tracking status:
     * OFFLINE  - no recent location update
     * STOPPED  - currently within an ongoing detected stop
     * MOVING   - recent movement beyond the stop radius
     * ONLINE   - recent update, not clearly moving or stopped
     */
    public String getStatus(Long userId) {
    	User user = userRepository.findById(userId).orElse(null);
        if (user == null || !user.isLoggedIn()) {
            return "OFFLINE";
        }

        Optional<EmployeeLocation> latestOpt = locationRepository.findFirstByUserIdOrderByRecordedAtDesc(userId);
    	
        if (latestOpt.isEmpty()) {
            return "OFFLINE";
        }

        EmployeeLocation latest = latestOpt.get();
        long minutesSinceUpdate = java.time.Duration.between(latest.getRecordedAt(), LocalDateTime.now()).toMinutes();

        if (minutesSinceUpdate > trackingProperties.getOnline().getThresholdMinutes()) {
            return "OFFLINE";
        }

        Optional<EmployeeStop> ongoingStop =
                stopRepository.findFirstByUserIdAndOngoingTrueOrderByStartTimeDesc(userId);
        if (ongoingStop.isPresent()) {
            return "STOPPED";
        }

        // Check movement between the last two points
        List<EmployeeLocation> recent = locationRepository.findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                userId, LocalDateTime.now().minusMinutes(30), LocalDateTime.now());
        if (recent.size() >= 2) {
            EmployeeLocation prev = recent.get(recent.size() - 2);
            double distance = HaversineUtil.distanceInMeters(
                    prev.getLatitude(), prev.getLongitude(), latest.getLatitude(), latest.getLongitude());
            if (distance > trackingProperties.getStop().getRadiusMeters()) {
                return "MOVING";
            }
        }

        return "ONLINE";
    }

    private LocationResponse toResponse(EmployeeLocation location, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        return LocationResponse.builder()
                .id(location.getId())
                .userId(userId)
                .employeeName(user.getFullName())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .accuracy(location.getAccuracy())
                .recordedAt(location.getRecordedAt())
                .status(getStatus(userId))
                .build();
    }
}
