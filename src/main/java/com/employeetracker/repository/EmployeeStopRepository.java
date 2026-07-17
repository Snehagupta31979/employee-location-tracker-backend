package com.employeetracker.repository;

import com.employeetracker.entity.EmployeeStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeStopRepository extends JpaRepository<EmployeeStop, Long> {

    List<EmployeeStop> findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(
            Long userId, LocalDateTime start, LocalDateTime end);

    Optional<EmployeeStop> findFirstByUserIdAndOngoingTrueOrderByStartTimeDesc(Long userId);
}
