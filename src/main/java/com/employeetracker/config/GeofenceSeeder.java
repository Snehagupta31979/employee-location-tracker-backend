package com.employeetracker.config;

import com.employeetracker.entity.Geofence;
import com.employeetracker.repository.GeofenceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GeofenceSeeder implements CommandLineRunner {

    private final GeofenceRepository geofenceRepository;

    public GeofenceSeeder(GeofenceRepository geofenceRepository) {
        this.geofenceRepository = geofenceRepository;
    }

    @Override
    public void run(String... args) {
        if (geofenceRepository.count() == 0) {
            Geofence office = new Geofence(null, "Head Office", Geofence.GeofenceType.OFFICE,
                    28.993441, 77.706040, 200, true);
            geofenceRepository.save(office);
        }
    }
}