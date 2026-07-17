package com.employeetracker.service;

import com.employeetracker.dto.GeofenceResponse;
import com.employeetracker.dto.GeofenceSessionResponse;
import com.employeetracker.entity.EmployeeActivity;
import com.employeetracker.entity.EmployeeGeofenceVisit;
import com.employeetracker.entity.Geofence;
import com.employeetracker.entity.User;
import com.employeetracker.repository.EmployeeGeofenceVisitRepository;
import com.employeetracker.repository.GeofenceRepository;
import com.employeetracker.repository.UserRepository;
import com.employeetracker.util.HaversineUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class GeofenceService {

    private final GeofenceRepository geofenceRepository;
    private final EmployeeGeofenceVisitRepository visitRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final AdminEventService adminEventService;

    public GeofenceService(GeofenceRepository geofenceRepository, EmployeeGeofenceVisitRepository visitRepository,
                            UserRepository userRepository, ActivityService activityService,
                            AdminEventService adminEventService) {
        this.geofenceRepository = geofenceRepository;
        this.visitRepository = visitRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.adminEventService = adminEventService;
    }

    public List<GeofenceResponse> getActiveGeofences() {
        return geofenceRepository.findByActiveTrue().stream()
                .map(g -> new GeofenceResponse(g.getId(), g.getName(), g.getType().name(),
                        g.getLatitude(), g.getLongitude(), g.getRadiusMeters()))
                .toList();
    }

    @Transactional
    public void evaluate(Long userId, double latitude, double longitude) {
        List<Geofence> geofences = geofenceRepository.findByActiveTrue();
        if (geofences.isEmpty()) return;

        String employeeName = userRepository.findById(userId).map(User::getFullName).orElse("Employee");

        for (Geofence g : geofences) {
            double distance = HaversineUtil.distanceInMeters(g.getLatitude(), g.getLongitude(), latitude, longitude);
            boolean inside = distance <= g.getRadiusMeters();

            Optional<EmployeeGeofenceVisit> ongoingOpt =
                    visitRepository.findFirstByUserIdAndGeofenceIdAndOngoingTrueOrderByEntryTimeDesc(userId, g.getId());

            if (inside && ongoingOpt.isEmpty()) {
                EmployeeGeofenceVisit visit = EmployeeGeofenceVisit.builder()
                        .userId(userId)
                        .geofenceId(g.getId())
                        .geofenceName(g.getName())
                        .geofenceType(g.getType())
                        .entryTime(LocalDateTime.now())
                        .ongoing(true)
                        .build();
                visitRepository.save(visit);

                String label = g.getType() == Geofence.GeofenceType.OFFICE ? "Entered Office" : "Entered Client Location";
                activityService.log(userId, EmployeeActivity.ActivityType.GEOFENCE_ENTER, label + ": " + g.getName());
                adminEventService.broadcastGeofenceEvent(userId, employeeName, g.getName(), g.getType().name(), "ENTER");

            } else if (!inside && ongoingOpt.isPresent()) {
                EmployeeGeofenceVisit visit = ongoingOpt.get();
                visit.setExitTime(LocalDateTime.now());
                visit.setDurationMinutes(Duration.between(visit.getEntryTime(), visit.getExitTime()).toMinutes());
                visit.setOngoing(false);
                visitRepository.save(visit);

                String label = g.getType() == Geofence.GeofenceType.OFFICE ? "Exited Office" : "Left Client Location";
                activityService.log(userId, EmployeeActivity.ActivityType.GEOFENCE_EXIT, label + ": " + g.getName());
                adminEventService.broadcastGeofenceEvent(userId, employeeName, g.getName(), g.getType().name(), "EXIT");
            }
        }
    }

    public List<GeofenceSessionResponse> getSessionsForDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return visitRepository.findByUserIdAndEntryTimeBetweenOrderByEntryTimeAsc(userId, start, end).stream()
                .map(v -> new GeofenceSessionResponse(v.getGeofenceName(), v.getGeofenceType().name(),
                        v.getEntryTime(), v.getExitTime(), v.getDurationMinutes(), v.isOngoing()))
                .toList();
    }
}