package com.persons.finder.utils;

public class GeoShardingUtil {
    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    // The accuracy of 3 digits corresponds to an area of approximately 150km x 150km, which is suitable for bucketing of 10M data.
    // 4 digits of accuracy corresponds to approximately 30km x 20km, which is more precise
    private static final int PRECISION = 4;

    public static String getShardKey(double lon, double lat) {
        return "person:geo:" + encode(lat, lon).substring(0, PRECISION);
    }

    private static String encode(double lat, double lon) {
        double[] latInterval = {-90.0, 90.0};
        double[] lonInterval = {-180.0, 180.0};
        StringBuilder geohash = new StringBuilder();
        boolean isEven = true;
        int bit = 0;
        int ch = 0;

        while (geohash.length() < PRECISION + 1) {
            double mid;
            if (isEven) {
                mid = (lonInterval[0] + lonInterval[1]) / 2;
                if (lon > mid) {
                    ch |= (1 << (4 - bit));
                    lonInterval[0] = mid;
                } else {
                    lonInterval[1] = mid;
                }
            } else {
                mid = (latInterval[0] + latInterval[1]) / 2;
                if (lat > mid) {
                    ch |= (1 << (4 - bit));
                    latInterval[0] = mid;
                } else {
                    latInterval[1] = mid;
                }
            }
            isEven = !isEven;
            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }
        return geohash.toString();
    }
}