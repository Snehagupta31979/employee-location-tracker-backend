package com.employeetracker.service;

import com.employeetracker.dto.AdminSummaryResponse;
import com.employeetracker.dto.EmployeeSummaryResponse;
import com.employeetracker.dto.LocationResponse;
import com.employeetracker.entity.EmployeeLocation;
import com.employeetracker.entity.User;
import com.employeetracker.repository.EmployeeLocationRepository;
import com.employeetracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final EmployeeLocationRepository locationRepository;
    private final LocationService locationService;
    private final DistanceService distanceService;

    public AdminService(UserRepository userRepository, EmployeeLocationRepository locationRepository,
                         LocationService locationService, DistanceService distanceService) {
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.locationService = locationService;
        this.distanceService = distanceService;
    }

    public List<EmployeeSummaryResponse> getAllEmployeeSummaries() {
    	List<User> employees = userRepository.findByRoleOrderByIdAsc(User.Role.EMPLOYEE);

        return employees.stream().map(employee -> {
            Optional<EmployeeLocation> latest = locationRepository.findFirstByUserIdOrderByRecordedAtDesc(employee.getId());

            return EmployeeSummaryResponse.builder()
                    .id(employee.getId())
                    .username(employee.getUsername())
                    .fullName(employee.getFullName())
                    .status(locationService.getStatus(employee.getId()))
                    .latitude(latest.map(EmployeeLocation::getLatitude).orElse(null))
                    .longitude(latest.map(EmployeeLocation::getLongitude).orElse(null))
                    .todayDistanceKm(distanceService.getTodayDistanceKm(employee.getId()))
                    .lastUpdated(latest.map(EmployeeLocation::getRecordedAt).orElse(null))
                    .build();
        }).toList();
    }

    public EmployeeSummaryResponse getEmployeeSummary(Long employeeId) {
        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new com.employeetracker.exception.ResourceNotFoundException("Employee not found"));

        Optional<EmployeeLocation> latest = locationRepository.findFirstByUserIdOrderByRecordedAtDesc(employeeId);

        return EmployeeSummaryResponse.builder()
                .id(employee.getId())
                .username(employee.getUsername())
                .fullName(employee.getFullName())
                .status(locationService.getStatus(employeeId))
                .latitude(latest.map(EmployeeLocation::getLatitude).orElse(null))
                .longitude(latest.map(EmployeeLocation::getLongitude).orElse(null))
                .todayDistanceKm(distanceService.getTodayDistanceKm(employeeId))
                .lastUpdated(latest.map(EmployeeLocation::getRecordedAt).orElse(null))
                .build();
    }

    public List<LocationResponse> getLiveLocations() {
    	List<User> employees = userRepository.findByRoleOrderByIdAsc(User.Role.EMPLOYEE);
        return employees.stream()
                .map(e -> locationRepository.findFirstByUserIdOrderByRecordedAtDesc(e.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(loc -> LocationResponse.builder()
                        .id(loc.getId())
                        .userId(loc.getUserId())
                        .employeeName(userRepository.findById(loc.getUserId()).map(User::getFullName).orElse("Unknown"))
                        .latitude(loc.getLatitude())
                        .longitude(loc.getLongitude())
                        .accuracy(loc.getAccuracy())
                        .recordedAt(loc.getRecordedAt())
                        .status(locationService.getStatus(loc.getUserId()))
                        .build())
                .toList();
    }

    public AdminSummaryResponse getSummary() {
        List<EmployeeSummaryResponse> summaries = getAllEmployeeSummaries();
        long total = summaries.size();
        long online = summaries.stream().filter(s -> !"OFFLINE".equals(s.getStatus())).count();
        long offline = total - online;

        return AdminSummaryResponse.builder()
                .totalEmployees(total)
                .onlineEmployees(online)
                .offlineEmployees(offline)
                .build();
    }
}
