package com.employeetracker.dto;

public class AdminSummaryResponse {
    private long totalEmployees;
    private long onlineEmployees;
    private long offlineEmployees;

    public AdminSummaryResponse() {
    }

    public AdminSummaryResponse(long totalEmployees, long onlineEmployees, long offlineEmployees) {
        this.totalEmployees = totalEmployees;
        this.onlineEmployees = onlineEmployees;
        this.offlineEmployees = offlineEmployees;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getOnlineEmployees() {
        return onlineEmployees;
    }

    public void setOnlineEmployees(long onlineEmployees) {
        this.onlineEmployees = onlineEmployees;
    }

    public long getOfflineEmployees() {
        return offlineEmployees;
    }

    public void setOfflineEmployees(long offlineEmployees) {
        this.offlineEmployees = offlineEmployees;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalEmployees;
        private long onlineEmployees;
        private long offlineEmployees;

        public Builder totalEmployees(long totalEmployees) {
            this.totalEmployees = totalEmployees;
            return this;
        }

        public Builder onlineEmployees(long onlineEmployees) {
            this.onlineEmployees = onlineEmployees;
            return this;
        }

        public Builder offlineEmployees(long offlineEmployees) {
            this.offlineEmployees = offlineEmployees;
            return this;
        }

        public AdminSummaryResponse build() {
            return new AdminSummaryResponse(totalEmployees, onlineEmployees, offlineEmployees);
        }
    }
}
