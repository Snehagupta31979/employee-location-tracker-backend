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
