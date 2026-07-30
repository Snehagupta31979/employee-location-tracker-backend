package com.employeetracker.config;
import com.employeetracker.service.AdminEventService;

import com.employeetracker.entity.EmployeeActivity;
import com.employeetracker.repository.UserRepository;
import com.employeetracker.service.ActivityService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ActivityService activityService;
    private final UserRepository userRepository;
    private final AdminEventService adminEventService;

    public SecurityConfig(CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder,
            ActivityService activityService, UserRepository userRepository,
            AdminEventService adminEventService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.activityService = activityService;
        this.userRepository = userRepository;
        this.adminEventService = adminEventService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public org.springframework.security.web.session.HttpSessionEventPublisher httpSessionEventPublisher() {
        return new org.springframework.security.web.session.HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .maximumSessions(5)
                    .maxSessionsPreventsLogin(false)
            )
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/", "/index.html", "/dashboard.html",
                            "/css/**", "/js/**", "/favicon.ico",
                            "/api/auth/login", "/api/auth/logout", "/api/auth/register",
                            "/api/auth/forgot-password", "/api/auth/verify-otp", "/api/auth/reset-password"
                    ).permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/location/**", "/api/auth/me", "/api/employee/**")
                    .authenticated()
                    .anyRequest().permitAll()
            )
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .logout(logout -> logout
                    .logoutUrl("/api/auth/logout")
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .addLogoutHandler((request, response, authentication) -> {
                        if (authentication != null) {
                            userRepository.findByUsername(authentication.getName())
                                    .ifPresent(u -> {
                                        u.setLoggedIn(false);
                                        userRepository.save(u);
                                        activityService.log(
                                                u.getId(),
                                                EmployeeActivity.ActivityType.LOGOUT,
                                                "Employee logged out"
                                        );
                                        adminEventService.broadcastStatusChange(u.getId(), "OFFLINE");
                                    });
                        }
                    })
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                        response.setHeader("Pragma", "no-cache");
                        response.setDateHeader("Expires", 0);
                        response.setStatus(HttpStatus.OK.value());
                    })
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        configuration.setAllowedHeaders(List.of("*"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
