package com.employeetracker.controller;

import com.employeetracker.dto.EmployeePlanRequest;
import com.employeetracker.dto.EmployeePlanResponse;
import com.employeetracker.entity.EmployeePlan;
import com.employeetracker.entity.User;
import com.employeetracker.exception.BadRequestException;
import com.employeetracker.exception.ResourceNotFoundException;
import com.employeetracker.repository.EmployeePlanRepository;
import com.employeetracker.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class EmployeePlanController {

    private final EmployeePlanRepository employeePlanRepository;
    private final UserRepository userRepository;

    public EmployeePlanController(EmployeePlanRepository employeePlanRepository,
                                   UserRepository userRepository) {
        this.employeePlanRepository = employeePlanRepository;
        this.userRepository = userRepository;
    }

    // ==========================
    // CREATE PLAN (Employee)
    // POST /api/location/plan
    // ==========================
    @PostMapping("/api/location/plan")
    public ResponseEntity<EmployeePlanResponse> createPlan(
            @RequestBody EmployeePlanRequest request,
            Authentication authentication) {

        User user = currentUser(authentication);

        if (request.getTask() == null || request.getTask().isBlank()) {
            throw new BadRequestException("Task is required");
        }

        EmployeePlan plan = new EmployeePlan();
        plan.setUserId(user.getId());
        plan.setPlanDate(LocalDate.now());
        plan.setTask(request.getTask());
        plan.setLocation(request.getLocation());
        plan.setPurpose(request.getPurpose());
        plan.setStartTime(request.getStartTime());
        plan.setEndTime(request.getEndTime());
        plan.setPriority(request.getPriority());
        plan.setStatus(request.getStatus() != null ? request.getStatus() : "PENDING");
        plan.setEdited(false);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        EmployeePlan saved = employeePlanRepository.save(plan);
        return ResponseEntity.ok(toResponse(saved, user));
    }

    // ==========================
    // UPDATE PLAN (Employee)
    // PUT /api/location/plan/{id}
    // ==========================
    @PutMapping("/api/location/plan/{id}")
    public ResponseEntity<EmployeePlanResponse> updatePlan(
            @PathVariable Long id,
            @RequestBody EmployeePlanRequest request,
            Authentication authentication) {

        User user = currentUser(authentication);

        EmployeePlan plan = employeePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found"));

        if (!plan.getUserId().equals(user.getId())) {
            throw new BadRequestException("You can only edit your own plan");
        }

        if (request.getTask() != null) plan.setTask(request.getTask());
        if (request.getLocation() != null) plan.setLocation(request.getLocation());
        if (request.getPurpose() != null) plan.setPurpose(request.getPurpose());
        if (request.getStartTime() != null) plan.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) plan.setEndTime(request.getEndTime());
        if (request.getPriority() != null) plan.setPriority(request.getPriority());
        if (request.getStatus() != null) plan.setStatus(request.getStatus());

        plan.setEdited(true);
        plan.setUpdatedAt(LocalDateTime.now());

        EmployeePlan saved = employeePlanRepository.save(plan);
        return ResponseEntity.ok(toResponse(saved, user));
    }

    // ==========================
    // GET MY TODAY PLAN(S) (Employee)
    // GET /api/location/plan/today
    // ==========================
    @GetMapping("/api/location/plan/today")
    public ResponseEntity<List<EmployeePlanResponse>> getMyTodayPlans(Authentication authentication) {
        User user = currentUser(authentication);

        List<EmployeePlan> plans = employeePlanRepository
                .findByUserIdAndPlanDateOrderByCreatedAtDesc(user.getId(), LocalDate.now());

        List<EmployeePlanResponse> response = plans.stream()
                .map(p -> toResponse(p, user))
                .toList();

        return ResponseEntity.ok(response);
    }

    // ==========================
    // GET ALL TODAY PLANS (Admin)
    // GET /api/admin/plans/today
    // ==========================
    @GetMapping("/api/admin/plans/today")
    public ResponseEntity<List<EmployeePlanResponse>> getAllTodayPlans() {
        List<EmployeePlan> plans = employeePlanRepository
                .findByPlanDateOrderByCreatedAtDesc(LocalDate.now());

        List<EmployeePlanResponse> response = plans.stream()
                .map(p -> {
                    User owner = userRepository.findById(p.getUserId()).orElse(null);
                    return toResponse(p, owner);
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private EmployeePlanResponse toResponse(EmployeePlan plan, User user) {
        return new EmployeePlanResponse(
                plan.getId(),
                plan.getUserId(),
                user != null ? user.getFullName() : "Unknown",
                plan.getPlanDate(),
                plan.getTask(),
                plan.getLocation(),
                plan.getPurpose(),
                plan.getStartTime(),
                plan.getEndTime(),
                plan.getPriority(),
                plan.getStatus(),
                plan.isEdited(),
                plan.getUpdatedAt()
        );
    }
}
