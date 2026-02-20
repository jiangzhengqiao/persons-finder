package com.persons.finder.repository;

import com.persons.finder.domain.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    @Query(value = "SELECT p FROM Person p WHERE " +
            "p.latitude BETWEEN :minLat AND :maxLat AND p.longitude BETWEEN :minLon AND :maxLon " +
            "AND (6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
            "cos(radians(p.longitude) - radians(:lon)) + " +
            "sin(radians(:lat)) * sin(radians(p.latitude)))) <= :radius " +
            "ORDER BY (p.latitude - :lat)*(p.latitude - :lat) + (p.longitude - :lon)*(p.longitude - :lon) ASC")
    Page<Person> findNearbyEfficiently(
            @Param("lat") double lat, @Param("lon") double lon,
            @Param("radius") double radius,
            @Param("minLat") double minLat, @Param("maxLat") double maxLat,
            @Param("minLon") double minLon, @Param("maxLon") double maxLon,
            Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE Person p SET p.latitude = :lat, p.longitude = :lon, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int updateLocation(@Param("id") Long id, @Param("lat") Double lat, @Param("lon") Double lon);

}