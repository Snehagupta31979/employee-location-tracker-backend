package com.employeetracker.controller;

import com.employeetracker.dto.ManualStopRequest;
import com.employeetracker.dto.ManualStopResponse;
import com.employeetracker.entity.ManualStopEntry;
import com.employeetracker.entity.User;
import com.employeetracker.exception.BadRequestException;
import com.employeetracker.exception.ResourceNotFoundException;
import com.employeetracker.repository.ManualStopEntryRepository;
import com.employeetracker.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class ManualStopController {

    private final ManualStopEntryRepository manualStopEntryRepository;
    private final UserRepository userRepository;

    public ManualStopController(ManualStopEntryRepository manualStopEntryRepository,
                                 UserRepository userRepository) {
        this.manualStopEntryRepository = manualStopEntryRepository;
        this.userRepository = userRepository;
    }

    // ==========================
    // ADD MANUAL STOP (Employee)
    // POST /api/location/stops/manual
    // ==========================
    @PostMapping("/api/location/stops/manual")
    public ResponseEntity<ManualStopResponse> addManualStop(
            @Valid @RequestBody ManualStopRequest request,
            Authentication authentication) {

        User user = currentUser(authentication);

        if (request.getDate() == null || request.getStartTime() == null || request.getEndTime() == null) {
            throw new BadRequestException("date, startTime and endTime are required");
        }

        LocalDateTime startDateTime = LocalDateTime.of(request.getDate(), request.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(request.getDate(), request.getEndTime());

        if (!endDateTime.isAfter(startDateTime)) {
            throw new BadRequestException("endTime must be after startTime");
        }

        long minutes = Duration.between(startDateTime, endDateTime).toMinutes();

        ManualStopEntry entry = new ManualStopEntry();
        entry.setUserId(user.getId());
        entry.setStopDate(request.getDate());
        entry.setStartTime(startDateTime);
        entry.setEndTime(endDateTime);
        entry.setDurationMinutes(minutes);
        entry.setAddress(request.getAddress());
        entry.setReason(request.getReason());
        entry.setCreatedAt(LocalDateTime.now());

        ManualStopEntry saved = manualStopEntryRepository.save(entry);

        return ResponseEntity.ok(toResponse(saved, user));
    }

    // ==========================
    // GET MY MANUAL STOPS (Employee)
    // GET /api/location/stops/manual
    // ==========================
    @GetMapping("/api/location/stops/manual")
    public ResponseEntity<List<ManualStopResponse>> getMyManualStops(Authentication authentication) {
        User user = currentUser(authentication);

        List<ManualStopEntry> entries =
                manualStopEntryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        List<ManualStopResponse> response = entries.stream()
                .map(e -> toResponse(e, user))
                .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================
    // UPDATE MANUAL STOP (Employee)
    // PUT /api/location/stops/manual/{id}
    // ==========================
    @PutMapping("/api/location/stops/manual/{id}")
    public ResponseEntity<ManualStopResponse> updateManualStop(
            @PathVariable Long id,
            @Valid @RequestBody ManualStopRequest request,
            Authentication authentication) {

        User user = currentUser(authentication);

        ManualStopEntry entry = manualStopEntryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manual stop not found"));

        if (!entry.getUserId().equals(user.getId())) {
            throw new BadRequestException("You can only edit your own manual stops");
        }

        if (request.getDate() != null) {
            entry.setStopDate(request.getDate());
        }
        if (request.getDate() != null && request.getStartTime() != null) {
            entry.setStartTime(LocalDateTime.of(request.getDate(), request.getStartTime()));
        }
        if (request.getDate() != null && request.getEndTime() != null) {
            entry.setEndTime(LocalDateTime.of(request.getDate(), request.getEndTime()));
        }
        if (entry.getStartTime() != null && entry.getEndTime() != null
                && entry.getEndTime().isAfter(entry.getStartTime())) {
            entry.setDurationMinutes(
                    Duration.between(entry.getStartTime(), entry.getEndTime()).toMinutes());
        }
        if (request.getAddress() != null) {
            entry.setAddress(request.getAddress());
        }
        if (request.getReason() != null) {
            entry.setReason(request.getReason());
        }

        ManualStopEntry saved = manualStopEntryRepository.save(entry);

        return ResponseEntity.ok(toResponse(saved, user));
    }

    // ==========================
    // GET ALL MANUAL STOPS (Admin)
    // GET /api/admin/stops/manual
    // ==========================
    @GetMapping("/api/admin/stops/manual")
    public ResponseEntity<List<ManualStopResponse>> getAllManualStops() {
        List<ManualStopEntry> entries = manualStopEntryRepository.findAllByOrderByCreatedAtDesc();

        List<ManualStopResponse> response = entries.stream()
                .map(e -> {
                    User owner = userRepository.findById(e.getUserId()).orElse(null);
                    return toResponse(e, owner);
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private ManualStopResponse toResponse(ManualStopEntry entry, User user) {
        return new ManualStopResponse(
                entry.getId(),
                entry.getUserId(),
                user != null ? user.getFullName() : "Unknown",
                entry.getStopDate(),
                entry.getStartTime(),
                entry.getEndTime(),
                entry.getDurationMinutes(),
                entry.getAddress(),
                entry.getReason()
        );
    }
}
