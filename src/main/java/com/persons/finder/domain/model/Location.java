package com.persons.finder.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {

    @Column(name = "location", columnDefinition = "geometry(Point, 4326)")
    private Point point;

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private Location(Point point) {
        this.point = point;
    }

    public static Location fromCoordinates(double lat, double lon) {
        Point point = GEOMETRY_FACTORY.createPoint(new Coordinate(lon, lat));
        return new Location(point);
    }

    public Double getLatitude() {
        return point != null ? point.getY() : null;
    }

    public Double getLongitude() {
        return point != null ? point.getX() : null;
    }
}