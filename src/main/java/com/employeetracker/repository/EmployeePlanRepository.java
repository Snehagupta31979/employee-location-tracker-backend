package com.employeetracker.repository;

import com.employeetracker.entity.EmployeePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EmployeePlanRepository extends JpaRepository<EmployeePlan, Long> {
    List<EmployeePlan> findByUserIdAndPlanDateOrderByCreatedAtDesc(Long userId, LocalDate planDate);
    List<EmployeePlan> findByPlanDateOrderByCreatedAtDesc(LocalDate planDate);
}
