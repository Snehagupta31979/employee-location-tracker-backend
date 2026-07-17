package com.employeetracker.config;

import com.employeetracker.entity.EmployeeLocation;
import com.employeetracker.entity.User;
import com.employeetracker.repository.EmployeeLocationRepository;
import com.employeetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds the database with sample users (module setup convenience) and a couple
 * of initial location points so the dashboard has something to display on first run.
 *
 * Passwords are BCrypt-hashed at startup using PasswordEncoder, so the sample
 * credentials in the README (password123) always work regardless of DB state.
 * Controlled by tracking.seed.enabled (default true).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final EmployeeLocationRepository locationRepository;
    private final PasswordEncoder passwordEncoder;
    private final TrackingProperties trackingProperties;

    private static final String DEFAULT_PASSWORD = "password123";

    public DataSeeder(UserRepository userRepository, EmployeeLocationRepository locationRepository,
                       PasswordEncoder passwordEncoder, TrackingProperties trackingProperties) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.passwordEncoder = passwordEncoder;
        this.trackingProperties = trackingProperties;
    }

    @Override
    public void run(String... args) {
        if (!trackingProperties.getSeed().isEnabled()) {
            return;
        }

        seedUser("admin", "System Administrator", "admin@company.com", User.Role.ADMIN, null);

        seedUser("john.smith", "John Smith", "john.smith@company.com", User.Role.EMPLOYEE,
                new double[]{28.6139, 77.2090}); // New Delhi
        seedUser("sarah.johnson", "Sarah Johnson", "sarah.johnson@company.com", User.Role.EMPLOYEE,
                new double[]{28.7041, 77.1025}); // Delhi NCR
        seedUser("michael.brown", "Michael Brown", "michael.brown@company.com", User.Role.EMPLOYEE,
                new double[]{28.5355, 77.3910}); // Noida

        log.info("Data seeding complete. Default password for all sample users is '{}'", DEFAULT_PASSWORD);
    }

    private Long seedUser(String username, String fullName, String email, User.Role role, double[] initialLocation) {
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                    .fullName(fullName)
                    .email(email)
                    .role(role)
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            User saved = userRepository.save(newUser);
            log.info("Seeded user '{}' with role {}", username, role);
            return saved;
        });

        if (initialLocation != null && locationRepository.findFirstByUserIdOrderByRecordedAtDesc(user.getId()).isEmpty()) {
            EmployeeLocation location = EmployeeLocation.builder()
                    .userId(user.getId())
                    .latitude(initialLocation[0])
                    .longitude(initialLocation[1])
                    .accuracy(15.0)
                    .recordedAt(LocalDateTime.now())
                    .build();
            locationRepository.save(location);
        }

        return user.getId();
    }
}
