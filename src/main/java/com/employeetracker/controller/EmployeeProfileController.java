package com.employeetracker.controller;

import com.employeetracker.dto.EmployeeProfileResponse;
import com.employeetracker.dto.EmployeeProfileUpdateRequest;
import com.employeetracker.entity.User;
import com.employeetracker.exception.ResourceNotFoundException;
import com.employeetracker.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/employee")
public class EmployeeProfileController {

    private final UserRepository userRepository;

    public EmployeeProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public ResponseEntity<EmployeeProfileResponse> getProfile(Authentication authentication) {
        User user = currentUser(authentication);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping("/profile")
    public ResponseEntity<EmployeeProfileResponse> updateProfile(
            @RequestBody EmployeeProfileUpdateRequest request,
            Authentication authentication) {
        User user = currentUser(authentication);

        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getDesignation() != null) user.setDesignation(request.getDesignation());
        if (request.getMobile() != null) user.setMobile(request.getMobile());
        if (request.getAddress() != null) user.setAddress(request.getAddress());
        if (request.getJoiningDate() != null) user.setJoiningDate(request.getJoiningDate());

        userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private EmployeeProfileResponse toResponse(User user) {
        String code = "EMP" + String.format("%04d", user.getId());
        String status = user.isLoggedIn() ? "Active" : "Offline";

        return new EmployeeProfileResponse(
                user.getId(),
                code,
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                status,
                user.getDepartment(),
                user.getDesignation(),
                user.getMobile(),
                user.getAddress(),
                user.getJoiningDate()
        );
    }
}
