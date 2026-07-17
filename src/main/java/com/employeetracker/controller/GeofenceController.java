package com.employeetracker.controller;

import com.employeetracker.dto.GeofenceResponse;
import com.employeetracker.service.GeofenceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/geofences")
public class GeofenceController {

    private final GeofenceService geofenceService;

    public GeofenceController(GeofenceService geofenceService) {
        this.geofenceService = geofenceService;
    }

    @GetMapping
    public List<GeofenceResponse> list() {
        return geofenceService.getActiveGeofences();
    }
}