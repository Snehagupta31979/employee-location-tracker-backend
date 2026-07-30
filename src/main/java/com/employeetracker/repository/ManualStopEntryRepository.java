package com.employeetracker.repository;

import com.employeetracker.entity.ManualStopEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualStopEntryRepository extends JpaRepository<ManualStopEntry, Long> {
    List<ManualStopEntry> findAllByOrderByCreatedAtDesc();
    List<ManualStopEntry> findByUserIdOrderByCreatedAtDesc(Long userId);
}
