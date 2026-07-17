package com.employeetracker.dto;

import java.time.LocalDateTime;

public class ActivityResponse {
    private Long id;
    private String activityType;
    private String description;
    private LocalDateTime activityTime;

    public ActivityResponse() {
    }

    public ActivityResponse(Long id, String activityType, String description, LocalDateTime activityTime) {
        this.id = id;
        this.activityType = activityType;
        this.description = description;
        this.activityTime = activityTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
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

    public static class Builder {
        private Long id;
        private String activityType;
        private String description;
        private LocalDateTime activityTime;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder activityType(String activityType) {
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

        public ActivityResponse build() {
            return new ActivityResponse(id, activityType, description, activityTime);
        }
    }
}
