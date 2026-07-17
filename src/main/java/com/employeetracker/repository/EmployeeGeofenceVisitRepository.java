package com.employeetracker.repository;

import com.employeetracker.entity.EmployeeGeofenceVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeGeofenceVisitRepository extends JpaRepository<EmployeeGeofenceVisit, Long> {

    Optional<EmployeeGeofenceVisit> findFirstByUserIdAndGeofenceIdAndOngoingTrueOrderByEntryTimeDesc(
            Long userId, Long geofenceId);

    List<EmployeeGeofenceVisit> findByUserIdAndEntryTimeBetweenOrderByEntryTimeAsc(
            Long userId, LocalDateTime start, LocalDateTime end);
}