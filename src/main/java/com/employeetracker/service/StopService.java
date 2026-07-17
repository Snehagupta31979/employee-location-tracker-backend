package com.employeetracker.service;

import com.employeetracker.dto.StopResponse;
import com.employeetracker.entity.EmployeeStop;
import com.employeetracker.repository.EmployeeStopRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class StopService {

    private final EmployeeStopRepository stopRepository;

    public StopService(EmployeeStopRepository stopRepository) {
        this.stopRepository = stopRepository;
    }

    public List<StopResponse> getTodayStops(Long userId) {
        return getStopsForDate(userId, LocalDate.now());
    }

    public List<StopResponse> getStopsForDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);

        return stopRepository.findByUserIdAndStartTimeBetweenOrderByStartTimeAsc(userId, start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StopResponse toResponse(EmployeeStop stop) {
        return StopResponse.builder()
                .id(stop.getId())
                .latitude(stop.getLatitude())
                .longitude(stop.getLongitude())
                .startTime(stop.getStartTime())
                .endTime(stop.getEndTime())
                .durationMinutes(stop.getDurationMinutes())
                .ongoing(stop.isOngoing())
                .build();
    }
}
