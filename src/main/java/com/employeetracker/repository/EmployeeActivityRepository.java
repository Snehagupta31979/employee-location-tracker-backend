package com.employeetracker.repository;

import com.employeetracker.entity.EmployeeActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmployeeActivityRepository extends JpaRepository<EmployeeActivity, Long> {

    List<EmployeeActivity> findByUserIdAndActivityTimeBetweenOrderByActivityTimeAsc(
            Long userId, LocalDateTime start, LocalDateTime end);
}
