package com.employeetracker.service;

import com.employeetracker.entity.EmployeeLocation;
import com.employeetracker.repository.EmployeeLocationRepository;
import com.employeetracker.util.HaversineUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DistanceService {

    private final EmployeeLocationRepository locationRepository;

    public DistanceService(EmployeeLocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public double getTodayDistanceKm(Long userId) {
        return getDistanceForDateKm(userId, LocalDate.now());
    }

    public double getDistanceForDateKm(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        List<EmployeeLocation> locations =
                locationRepository.findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, start, end);

        if (locations.size() < 2) {
            return 0.0;
        }

        double totalKm = 0.0;
        for (int i = 1; i < locations.size(); i++) {
            EmployeeLocation prev = locations.get(i - 1);
            EmployeeLocation curr = locations.get(i);
            totalKm += HaversineUtil.distanceInKm(
                    prev.getLatitude(), prev.getLongitude(),
                    curr.getLatitude(), curr.getLongitude());
        }
        return totalKm;
    }
}
