package com.employeetracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tracking")
public class TrackingProperties {

    private Stop stop = new Stop();
    private Online online = new Online();
    private AutoUpdate autoUpdate = new AutoUpdate();
    private Seed seed = new Seed();

    public Stop getStop() {
        return stop;
    }

    public void setStop(Stop stop) {
        this.stop = stop;
    }

    public Online getOnline() {
        return online;
    }

    public void setOnline(Online online) {
        this.online = online;
    }

    public AutoUpdate getAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(AutoUpdate autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public Seed getSeed() {
        return seed;
    }

    public void setSeed(Seed seed) {
        this.seed = seed;
    }

    public static class Stop {
        /** Radius in meters within which the employee is considered "not moving" */
        private double radiusMeters = 30;
        /** Minutes within the radius before a stop is registered */
        private int durationMinutes = 10;

        public double getRadiusMeters() {
            return radiusMeters;
        }

        public void setRadiusMeters(double radiusMeters) {
            this.radiusMeters = radiusMeters;
        }

        public int getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(int durationMinutes) {
            this.durationMinutes = durationMinutes;
        }
    }

    public static class Online {
        /** Minutes since last update after which an employee is considered offline */
        private int thresholdMinutes = 15;

        public int getThresholdMinutes() {
            return thresholdMinutes;
        }

        public void setThresholdMinutes(int thresholdMinutes) {
            this.thresholdMinutes = thresholdMinutes;
        }
    }

    public static class AutoUpdate {
        private int intervalMinutes = 10;

        public int getIntervalMinutes() {
            return intervalMinutes;
        }

        public void setIntervalMinutes(int intervalMinutes) {
            this.intervalMinutes = intervalMinutes;
        }
    }

    public static class Seed {
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
