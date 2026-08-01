package com.employeetracker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.employeetracker.config.TrackingProperties;
import java.util.TimeZone;
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(TrackingProperties.class)
public class EmployeeLocationTrackerApplication {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
    }
    public static void main(String[] args) {
        SpringApplication.run(EmployeeLocationTrackerApplication.class, args);
    }
}
