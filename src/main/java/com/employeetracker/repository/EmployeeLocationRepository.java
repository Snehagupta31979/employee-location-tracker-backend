package com.employeetracker.repository;

import com.employeetracker.entity.EmployeeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeLocationRepository extends JpaRepository<EmployeeLocation, Long> {

    List<EmployeeLocation> findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long userId, LocalDateTime start, LocalDateTime end);

    Optional<EmployeeLocation> findFirstByUserIdOrderByRecordedAtDesc(Long userId);

    @Query("SELECT el FROM EmployeeLocation el WHERE el.userId IN " +
            "(SELECT u.id FROM User u) AND el.recordedAt = " +
            "(SELECT MAX(el2.recordedAt) FROM EmployeeLocation el2 WHERE el2.userId = el.userId)")
    List<EmployeeLocation> findLatestForAllUsers();

    @Query("SELECT el FROM EmployeeLocation el WHERE el.recordedAt = " +
            "(SELECT MAX(el2.recordedAt) FROM EmployeeLocation el2 WHERE el2.userId = :userId) " +
            "AND el.userId = :userId")
    Optional<EmployeeLocation> findLatestForUser(@Param("userId") Long userId);
}
