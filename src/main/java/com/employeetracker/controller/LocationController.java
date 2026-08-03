package com.employeetracker.controller;

import com.employeetracker.dto.ActivityResponse;
import com.employeetracker.dto.LocationRequest;
import com.employeetracker.dto.LocationResponse;
import com.employeetracker.dto.StopResponse;
import com.employeetracker.entity.User;
import com.employeetracker.exception.ResourceNotFoundException;
import com.employeetracker.repository.UserRepository;
import com.employeetracker.service.ActivityService;
import com.employeetracker.service.LocationService;
import com.employeetracker.service.StopService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;
    private final StopService stopService;
    private final ActivityService activityService;
    private final UserRepository userRepository;

    public LocationController(LocationService locationService, StopService stopService,
                               ActivityService activityService, UserRepository userRepository) {
        this.locationService = locationService;
        this.stopService = stopService;
        this.activityService = activityService;
        this.userRepository = userRepository;
    }

    @PostMapping("/save")
    public ResponseEntity<LocationResponse> saveLocation(@Valid @RequestBody LocationRequest request,
                                                            Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(locationService.saveLocation(userId, request));
    }

    @GetMapping("/current")
    public ResponseEntity<LocationResponse> getCurrentLocation(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(locationService.getCurrentLocation(userId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<LocationResponse>> getHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(locationService.getHistory(userId, date));
    }

    @GetMapping("/distance")
    public ResponseEntity<Map<String, Object>> getTodayDistance(Authentication authentication) {
        Long userId = currentUserId(authentication);
        double distanceKm = locationService.getTodayDistanceKm(userId);
        return ResponseEntity.ok(Map.of("distanceKm", distanceKm));
    }

    @GetMapping("/stops")
    public ResponseEntity<List<StopResponse>> getTodayStops(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(stopService.getTodayStops(userId));
    }
    @GetMapping("/stops/all")
    public ResponseEntity<List<Map<String, Object>>> getAllMyStopsToday(Authentication authentication) {
        Long userId = currentUserId(authentication);
        LocalDate today = LocalDate.now();

        List<com.employeetracker.dto.StopResponse> sessions = activityService.getTrackingSessions(userId, today);
        List<com.employeetracker.dto.LocationResponse> locations = locationService.getHistory(userId, today);
        List<com.employeetracker.dto.StopResponse> physicalStops = stopService.getTodayStops(userId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (com.employeetracker.dto.StopResponse session : sessions) {
            if (session.getStartTime() == null) continue;

            com.employeetracker.dto.LocationResponse nearest = null;
            long bestDiff = Long.MAX_VALUE;
            for (com.employeetracker.dto.LocationResponse loc : locations) {
                if (loc.getRecordedAt() == null) continue;
                long diff = Math.abs(Duration.between(session.getStartTime(), loc.getRecordedAt()).toSeconds());
                if (diff < bestDiff) {
                    bestDiff = diff;
                    nearest = loc;
                }
            }

            Map<String, Object> entry = new java.util.HashMap<>();
            entry.put("startTime", session.getStartTime());
            entry.put("endTime", session.getEndTime());
            entry.put("ongoing", session.isOngoing());
            entry.put("latitude", nearest != null ? nearest.getLatitude() : null);
            entry.put("longitude", nearest != null ? nearest.getLongitude() : null);
            result.add(entry);
        }

        for (com.employeetracker.dto.StopResponse stop : physicalStops) {
            if (stop.getStartTime() == null) continue;

            boolean overlaps = sessions.stream().anyMatch(s ->
                    s.getStartTime() != null &&
                    Math.abs(Duration.between(s.getStartTime(), stop.getStartTime()).toMinutes()) < 1
            );
            if (overlaps) continue;

            Map<String, Object> entry = new java.util.HashMap<>();
            entry.put("startTime", stop.getStartTime());
            entry.put("endTime", stop.getEndTime());
            entry.put("ongoing", stop.isOngoing());
            entry.put("latitude", stop.getLatitude());
            entry.put("longitude", stop.getLongitude());
            entry.put("durationMinutes", stop.getDurationMinutes());
            result.add(entry);
        }

        result.sort((a, b) -> {
            LocalDateTime ta = (LocalDateTime) a.get("startTime");
            LocalDateTime tb = (LocalDateTime) b.get("startTime");
            return ta.compareTo(tb);
        });

        return ResponseEntity.ok(result);
    }

    @GetMapping("/activities")
    public ResponseEntity<List<ActivityResponse>> getTodayActivities(Authentication authentication) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.ok(activityService.getTodayActivities(userId));
    }
    @PostMapping("/tracking/start")
    public ResponseEntity<Map<String, Object>> startTracking(Authentication authentication) {
        Long userId = currentUserId(authentication);
        activityService.log(userId, com.employeetracker.entity.EmployeeActivity.ActivityType.TRACKING_START,
                "Employee started location tracking");
        return ResponseEntity.ok(Map.of("status", "STARTED"));
    }

    @PostMapping("/tracking/stop")
    public ResponseEntity<Map<String, Object>> stopTracking(Authentication authentication) {
        Long userId = currentUserId(authentication);
        activityService.log(userId, com.employeetracker.entity.EmployeeActivity.ActivityType.TRACKING_END,
                "Employee stopped location tracking");
        return ResponseEntity.ok(Map.of("status", "STOPPED"));
    }

    private Long currentUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}
