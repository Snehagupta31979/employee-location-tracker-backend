package com.employeetracker.controller;

import com.employeetracker.dto.LoginRequest;
import com.employeetracker.dto.RegisterRequest;
import com.employeetracker.dto.UserInfoResponse;
import com.employeetracker.entity.EmployeeActivity;
import com.employeetracker.entity.User;
import com.employeetracker.exception.BadRequestException;
import com.employeetracker.exception.ResourceNotFoundException;
import com.employeetracker.repository.UserRepository;
import com.employeetracker.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                           ActivityService activityService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<UserInfoResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists. Please choose another.");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(User.Role.EMPLOYEE)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        User saved = userRepository.save(newUser);
        return ResponseEntity.ok(toUserInfo(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<UserInfoResponse> login(@Valid @RequestBody LoginRequest request,
                                                    HttpServletRequest httpRequest,
                                                    HttpServletResponse httpResponse) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLastLoginAt(LocalDateTime.now());
        user.setLoggedIn(true);
        userRepository.save(user);

        activityService.log(user.getId(), EmployeeActivity.ActivityType.LOGIN, "Employee logged in");

        return ResponseEntity.ok(toUserInfo(user));
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(toUserInfo(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Authentication authentication) {

    	if (authentication != null) {
            userRepository.findByUsername(authentication.getName())
                    .ifPresent(user -> {
                        user.setLoggedIn(false);
                        userRepository.save(user);
                        activityService.log(
                                user.getId(),
                                EmployeeActivity.ActivityType.LOGOUT,
                                "Employee logged out");
                    });
        }

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\"");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        return ResponseEntity.ok().build();
    }

    private UserInfoResponse toUserInfo(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}