package com.employeetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.employeetracker.config.TrackingProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TrackingProperties.class)
public class EmployeeLocationTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeLocationTrackerApplication.class, args);
    }
}
