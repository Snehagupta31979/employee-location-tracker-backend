package com.employeetracker.controller;

import com.employeetracker.dto.DailyDistanceResponse;
import com.employeetracker.dto.TimeDistributionResponse;
import com.employeetracker.entity.User;
import com.employeetracker.exception.ResourceNotFoundException;
import com.employeetracker.repository.UserRepository;
import com.employeetracker.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    public AnalyticsController(AnalyticsService analyticsService, UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    @GetMapping("/daily-distance")
    public List<DailyDistanceResponse> dailyDistance(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        Long userId = employeeId != null ? employeeId : currentUserId(authentication);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return analyticsService.getDailyDistance(userId, targetDate);
    }

    @GetMapping("/time-distribution")
    public TimeDistributionResponse timeDistribution(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        Long userId = employeeId != null ? employeeId : currentUserId(authentication);
        LocalDate targetDate = date != null ? date : LocalDate.now();
        return analyticsService.getTimeDistribution(userId, targetDate);
    }

    private Long currentUserId(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return user.getId();
    }
}