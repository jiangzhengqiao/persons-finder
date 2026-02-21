package com.persons.finder.domain.repository;

import com.persons.finder.domain.model.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    @Query(value = "SELECT p FROM Person p WHERE " +
            "p.location.latitude BETWEEN :minLat AND :maxLat AND p.location.longitude BETWEEN :minLon AND :maxLon " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(p.location.latitude)) * " +
            "cos(radians(p.location.longitude) - radians(:lon)) + " +
            "sin(radians(:lat)) * sin(radians(p.location.latitude)))) <= :radius " +
            "ORDER BY (p.location.latitude - :lat)*(p.location.latitude - :lat) + (p.location.longitude - :lon)*(p.location.longitude - :lon) ASC")
    Slice<Person> findNearbyEfficiently(
            @Param("lat") double lat, @Param("lon") double lon,
            @Param("radius") double radius,
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLon") double minLon, @Param("maxLon") double maxLon,
            Pageable pageable);

}