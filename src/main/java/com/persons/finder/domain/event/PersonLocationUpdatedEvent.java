package com.persons.finder.domain.event;

import org.springframework.data.geo.Point;

public record PersonLocationUpdatedEvent(
        Long personId,
        Point oldLocation,
        Point newLocation
) {
}