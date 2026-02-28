package com.persons.finder.infrastructure.persistence;

import com.persons.finder.domain.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Repository
public interface JpaPersonRepository extends JpaRepository<Person, Long> {

    @Query(value = """
            SELECT * FROM persons 
            WHERE ST_DWithin(location, ST_SetSRID(ST_Point(:lon, :lat), 4326), :radiusKm * 1000) 
            ORDER BY location <-> ST_SetSRID(ST_Point(:lon, :lat), 4326)
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Person> findNearbyWithPostgis(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radiusKm") double radiusKm,
            @Param("limit") int limit,
            @Param("offset") long offset);

    List<Person> findAllByIdIn(Collection<Long> ids);

    @Modifying
    @Transactional
    @Query(value = "TRUNCATE TABLE persons RESTART IDENTITY CASCADE", nativeQuery = true)
    void truncateTableNative();
}