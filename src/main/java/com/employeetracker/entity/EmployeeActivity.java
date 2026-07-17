package com.employeetracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "EmployeeActivity", indexes = {
        @Index(name = "idx_activity_user_time", columnList = "user_id,activity_time")
})
public class EmployeeActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private ActivityType activityType;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "activity_time", nullable = false)
    private LocalDateTime activityTime;

    public EmployeeActivity() {
    }

    public EmployeeActivity(Long id, Long userId, ActivityType activityType, String description,
                             LocalDateTime activityTime) {
        this.id = id;
        this.userId = userId;
        this.activityType = activityType;
        this.description = description;
        this.activityTime = activityTime;
    }

    @PrePersist
    public void prePersist() {
        if (activityTime == null) {
            activityTime = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(LocalDateTime activityTime) {
        this.activityTime = activityTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum ActivityType {
        LOGIN, LOGOUT, LOCATION_UPDATE, STOP_START, STOP_END, TRACKING_START, TRACKING_END,
        GEOFENCE_ENTER, GEOFENCE_EXIT
    }

    public static class Builder {
        private Long id;
        private Long userId;
        private ActivityType activityType;
        private String description;
        private LocalDateTime activityTime;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder activityType(ActivityType activityType) {
            this.activityType = activityType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder activityTime(LocalDateTime activityTime) {
            this.activityTime = activityTime;
            return this;
        }

        public EmployeeActivity build() {
            return new EmployeeActivity(id, userId, activityType, description, activityTime);
        }
    }
}
