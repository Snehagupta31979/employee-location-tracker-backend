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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final AdminEventService adminEventService;

    public AdminController(AdminService adminService, ReportService reportService, AdminEventService adminEventService) {
        this.adminService = adminService;
        this.reportService = reportService;
        this.adminEventService = adminEventService;
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
    @GetMapping("/events")
    public SseEmitter streamEvents() {
        return adminEventService.subscribe();
    }

    @GetMapping("/report")
    public ResponseEntity<ReportResponse> getReport(
            @RequestParam Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
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
