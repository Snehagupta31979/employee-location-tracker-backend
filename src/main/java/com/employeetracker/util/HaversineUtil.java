package com.employeetracker.util;

/**
 * Utility for calculating great-circle distance between two GPS coordinates
 * using the Haversine formula.
 */
public final class HaversineUtil {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private HaversineUtil() {
    }

    /**
     * @return distance in meters between the two points
     */
    public static double distanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * @return distance in kilometers between the two points
     */
    public static double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        return distanceInMeters(lat1, lon1, lat2, lon2) / 1000.0;
    }
}
