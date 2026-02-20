package com.persons.finder.util;

/**
 * Utility for geospatial calculations.
 */
public class GeoUtils {

    // Mean radius of the Earth (KM)
    private static final double EARTH_RADIUS = 6371.01;

    /**
     * define boundaries
     */
    public record BoundingBox(
            double minLat, double maxLat,
            double minLon, double maxLon
    ) {}

    /**
     * Calculate the latitude and longitude bounding box (Bounding Box) based on the center point and radius
     * This is crucial for querying 1 million data. It allows the database to use the index to filter out 99% of non-target data.
     */
    public static BoundingBox calculateBoundingBox(double lat, double lon, double radiusKm) {

        double latChange = Math.toDegrees(radiusKm / EARTH_RADIUS);
        double lonChange = Math.toDegrees(radiusKm / EARTH_RADIUS / Math.cos(Math.toRadians(lat)));

        return new BoundingBox(
                lat - latChange,
                lat + latChange,
                lon - lonChange,
                lon + lonChange
        );
    }
}