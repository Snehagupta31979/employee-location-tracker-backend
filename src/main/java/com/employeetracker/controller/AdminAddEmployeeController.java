package com.employeetracker.controller;

import com.employeetracker.dto.AddEmployeeRequest;
import com.employeetracker.dto.AddEmployeeResponse;
import com.employeetracker.entity.User;
import com.employeetracker.exception.BadRequestException;
import com.employeetracker.repository.UserRepository;
import com.employeetracker.service.MailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/admin")
public class AdminAddEmployeeController {

    private static final String USERNAME_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final String PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private final SecureRandom random = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AdminAddEmployeeController(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       MailService mailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @PostMapping("/employees/add")
    public ResponseEntity<AddEmployeeResponse> addEmployee(@Valid @RequestBody AddEmployeeRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An employee with this email already exists.");
        }

        String username = generateUniqueUsername(request.getFullName());
        String rawPassword = generatePassword();

        User newUser = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .role(User.Role.EMPLOYEE)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        newUser.setDepartment(request.getDepartment());
        newUser.setDesignation(request.getDesignation());
        newUser.setMobile(request.getMobile());
        newUser.setAddress(request.getAddress());
        newUser.setJoiningDate(request.getJoiningDate());

        User saved = userRepository.save(newUser);
        String employeeCode = "EMP" + String.format("%04d", saved.getId());

        mailService.sendRegistrationEmail(saved.getEmail(), saved.getFullName(),
                saved.getUsername(), rawPassword, saved.getId());

        AddEmployeeResponse response = new AddEmployeeResponse(
                saved.getId(),
                employeeCode,
                saved.getUsername(),
                saved.getFullName(),
                saved.getEmail(),
                "Employee added. Login credentials sent to " + saved.getEmail()
        );

        return ResponseEntity.ok(response);
    }

    private String generateUniqueUsername(String fullName) {
        String base = fullName.trim().toLowerCase().replaceAll("[^a-z]", "");
        if (base.isEmpty()) base = "employee";
        if (base.length() > 10) base = base.substring(0, 10);

        String username;
        do {
            String suffix = randomString(USERNAME_CHARS, 4);
            username = base + suffix;
        } while (userRepository.existsByUsername(username));

        return username;
    }

    private String generatePassword() {
        return randomString(PASSWORD_CHARS, 10);
    }

    private String randomString(String chars, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
