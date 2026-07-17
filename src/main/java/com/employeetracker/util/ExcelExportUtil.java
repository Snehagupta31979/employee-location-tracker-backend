package com.employeetracker.util;

import com.employeetracker.dto.ReportResponse;
import com.employeetracker.dto.StopResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Builds a downloadable .xlsx report for an employee's daily locations and stops.
 */
public final class ExcelExportUtil {

    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ExcelExportUtil() {
    }

    public static byte[] buildReportWorkbook(ReportResponse report) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = buildHeaderStyle(workbook);

            buildSummarySheet(workbook, report, headerStyle);
            buildLocationsSheet(workbook, report, headerStyle);
            buildStopsSheet(workbook, report, headerStyle);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    private static CellStyle buildHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static void buildSummarySheet(Workbook workbook, ReportResponse report, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Summary");
        int rowIdx = 0;

        Row title = sheet.createRow(rowIdx++);
        title.createCell(0).setCellValue("Employee Location Report");

        rowIdx++;
        writeRow(sheet, rowIdx++, headerStyle, "Employee Name", report.getEmployeeName());
        writeRow(sheet, rowIdx++, headerStyle, "Date", String.valueOf(report.getDate()));
        writeRow(sheet, rowIdx++, headerStyle, "Total Distance (km)",
                String.format("%.2f", report.getTotalDistanceKm()));
        writeRow(sheet, rowIdx++, headerStyle, "Total Stops", String.valueOf(report.getStops().size()));
        writeRow(sheet, rowIdx, headerStyle, "Total Location Points", String.valueOf(report.getLocations().size()));

        for (int i = 0; i < 2; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void writeRow(Sheet sheet, int rowIdx, CellStyle headerStyle, String label, String value) {
        Row row = sheet.createRow(rowIdx);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(headerStyle);
        row.createCell(1).setCellValue(value);
    }

    private static void buildLocationsSheet(Workbook workbook, ReportResponse report, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Locations");
        String[] headers = {"#", "Latitude", "Longitude", "Accuracy (m)", "Recorded At"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        int counter = 1;
        for (var loc : report.getLocations()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(counter++);
            row.createCell(1).setCellValue(loc.getLatitude());
            row.createCell(2).setCellValue(loc.getLongitude());
            row.createCell(3).setCellValue(loc.getAccuracy() != null ? loc.getAccuracy() : 0);
            row.createCell(4).setCellValue(
                    loc.getRecordedAt() != null ? loc.getRecordedAt().format(DATE_TIME_FMT) : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private static void buildStopsSheet(Workbook workbook, ReportResponse report, CellStyle headerStyle) {
        Sheet sheet = workbook.createSheet("Stops");
        String[] headers = {"#", "Latitude", "Longitude", "Start Time", "End Time", "Duration (min)", "Ongoing"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        int counter = 1;
        for (StopResponse stop : report.getStops()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(counter++);
            row.createCell(1).setCellValue(stop.getLatitude());
            row.createCell(2).setCellValue(stop.getLongitude());
            row.createCell(3).setCellValue(stop.getStartTime() != null ? stop.getStartTime().format(DATE_TIME_FMT) : "");
            row.createCell(4).setCellValue(stop.getEndTime() != null ? stop.getEndTime().format(DATE_TIME_FMT) : "");
            row.createCell(5).setCellValue(stop.getDurationMinutes() != null ? stop.getDurationMinutes() : 0);
            row.createCell(6).setCellValue(stop.isOngoing() ? "Yes" : "No");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
