package com.persons.finder.infrastructure.persistence;

import com.persons.finder.domain.model.Person;
import com.persons.finder.domain.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PersonRepositoryImpl implements PersonRepository {

    private final JpaPersonRepository jpaRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private static final String GEO_KEY = "person_locations";

    @Value("${app.geo.redis-search-limit:500}")
    private int redisSearchLimit;

    @Override
    public Slice<Person> findNearby(double lat, double lon, double radiusKm, Pageable pageable) {
        try {
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo()
                    .search(GEO_KEY,
                            GeoReference.fromCoordinate(lon, lat),
                            new Distance(radiusKm, Metrics.KILOMETERS),
                            RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().sortAscending()
                                    .limit(redisSearchLimit));

            if (geoResults != null && !geoResults.getContent().isEmpty()) {
                List<GeoResult<RedisGeoCommands.GeoLocation<String>>> allResults = geoResults.getContent();
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), allResults.size());

                if (start < allResults.size()) {
                    List<Long> orderedIds = allResults.subList(start, end).stream()
                            .map(res -> Long.parseLong(res.getContent().getName()))
                            .toList();

                    Map<Long, Person> personMap = jpaRepo.findAllByIdIn(orderedIds).stream()
                            .collect(Collectors.toMap(Person::getId, p -> p));

                    List<Person> orderedPersons = orderedIds.stream()
                            .map(personMap::get)
                            .filter(Objects::nonNull)
                            .toList();

                    return new SliceImpl<>(orderedPersons, pageable, allResults.size() > end);
                }
            }
            return new SliceImpl<>(Collections.emptyList(), pageable, false);

        } catch (Exception e) {
            log.error("CRITICAL: Redis search failed. Falling back to PostGIS spatial query for lat:{}, lon:{}", lat, lon, e);
            double radiusInDegrees = radiusKm / 111.0;
            List<Person> dbResults = jpaRepo.findNearbyWithPostgis(
                    lat, lon, radiusInDegrees,
                    pageable.getPageSize() + 1,
                    pageable.getOffset()
            );

            boolean hasNext = dbResults.size() > pageable.getPageSize();
            List<Person> content = hasNext ? dbResults.subList(0, pageable.getPageSize()) : dbResults;

            return new SliceImpl<>(content, pageable, hasNext);
        }
    }

    @Override
    public Optional<Person> findById(Long id) {
        return jpaRepo.findById(id);
    }

    @Override
    @Transactional
    public Person save(Person person) {
        return jpaRepo.save(person);
    }

    @Override
    public void deleteAll() {
        jpaRepo.truncateTableNative();
    }
}