package com.employeetracker.controller;

import com.employeetracker.dto.AdminSummaryResponse;
import com.employeetracker.service.AdminEventService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.employeetracker.dto.EmployeeSummaryResponse;
import com.employeetracker.dto.LocationResponse;
import com.employeetracker.dto.ReportResponse;
import com.employeetracker.service.AdminService;
import com.employeetracker.service.ReportService;
import com.employeetracker.util.ExcelExportUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.employeetracker.dto.AdminStopEntry;
import com.employeetracker.dto.StopResponse;
import com.employeetracker.dto.LocationResponse;
import com.employeetracker.entity.User;
import com.employeetracker.repository.UserRepository;
import com.employeetracker.service.StopService;
import com.employeetracker.service.ActivityService;
import com.employeetracker.service.LocationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final AdminEventService adminEventService;
    private final StopService stopService;
    private final UserRepository userRepository;
    private final ActivityService activityService;
    private final LocationService locationService;

    public AdminController(AdminService adminService, ReportService reportService, AdminEventService adminEventService,
                            StopService stopService, UserRepository userRepository,
                            ActivityService activityService, LocationService locationService) {
        this.adminService = adminService;
        this.reportService = reportService;
        this.adminEventService = adminEventService;
        this.stopService = stopService;
        this.userRepository = userRepository;
        this.activityService = activityService;
        this.locationService = locationService;
    }

    @GetMapping("/employees")
    public ResponseEntity<List<EmployeeSummaryResponse>> getAllEmployees() {
        return ResponseEntity.ok(adminService.getAllEmployeeSummaries());
    }

    @GetMapping("/employee/{id}")
    public ResponseEntity<EmployeeSummaryResponse> getEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getEmployeeSummary(id));
    }

    @GetMapping("/live-locations")
    public ResponseEntity<List<LocationResponse>> getLiveLocations() {
        return ResponseEntity.ok(adminService.getLiveLocations());
    }

    @GetMapping("/summary")
    public ResponseEntity<AdminSummaryResponse> getSummary() {
        return ResponseEntity.ok(adminService.getSummary());
    }
    @GetMapping("/stops/today")
    public ResponseEntity<List<AdminStopEntry>> getAllStopsToday() {
        LocalDate today = LocalDate.now();
        List<User> employees = userRepository.findByRoleOrderByIdAsc(User.Role.EMPLOYEE);
        List<AdminStopEntry> allStops = new ArrayList<>();

        for (User employee : employees) {
            List<StopResponse> sessions = activityService.getTrackingSessions(employee.getId(), today);
            List<LocationResponse> locations = locationService.getHistory(employee.getId(), today);

            for (StopResponse session : sessions) {
                if (session.getStartTime() == null) continue;

                LocationResponse nearest = null;
                long bestDiffSeconds = Long.MAX_VALUE;
                for (LocationResponse loc : locations) {
                    if (loc.getRecordedAt() == null) continue;
                    long diff = Math.abs(Duration.between(session.getStartTime(), loc.getRecordedAt()).toSeconds());
                    if (diff < bestDiffSeconds) {
                        bestDiffSeconds = diff;
                        nearest = loc;
                    }
                }

                LocalDateTime effectiveEnd = session.getEndTime() != null ? session.getEndTime() : LocalDateTime.now();
                long durationMinutes = Duration.between(session.getStartTime(), effectiveEnd).toMinutes();

                allStops.add(new AdminStopEntry(
                        employee.getId(),
                        employee.getFullName(),
                        nearest != null ? nearest.getLatitude() : null,
                        nearest != null ? nearest.getLongitude() : null,
                        session.getStartTime(),
                        session.getEndTime(),
                        durationMinutes,
                        session.isOngoing()
                ));
            }
        }

        allStops.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));
        return ResponseEntity.ok(allStops);
    }
    @GetMapping("/events")
    public SseEmitter streamEvents() {
        return adminEventService.subscribe();
    }

    @GetMapping("/report")
    public ResponseEntity<ReportResponse> getReport(
            @RequestParam Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return ResponseEntity.ok(reportService.generateReport(employeeId, fromDate, toDate));
        }
        return ResponseEntity.ok(reportService.generateReport(employeeId, date));
    }

    @GetMapping("/report/export")
    public ResponseEntity<byte[]> exportReport(
            @RequestParam Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        ReportResponse report = reportService.generateReport(employeeId, date);
        byte[] workbook = ExcelExportUtil.buildReportWorkbook(report);

        String filename = String.format("report_%s_%s.xlsx",
                report.getEmployeeName().replaceAll("\\s+", "_"), report.getDate());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(workbook);
    }
}
