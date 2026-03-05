package com.persons.finder.infrastructure.persistence;

import com.persons.finder.domain.model.Person;
import com.persons.finder.domain.repository.PersonRepository;
import com.persons.finder.utils.GeoShardingUtil;
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

    @Value("${app.geo.redis-search-limit:500}")
    private int redisSearchLimit;

    @Override
    public Slice<Person> findNearby(double lat, double lon, double radiusKm, Pageable pageable) {
        List<Person> results = null;
        String shardKey = GeoShardingUtil.getShardKey(lon, lat);
        try {
            results = findWithRedisGEO(lat, lon, radiusKm, pageable, shardKey);
        } catch (Exception e) {
            log.error("Redis search failed for shard {}: {}", shardKey, e.getMessage());
        }

        if (results == null) {
            log.info("Falling back to PostGIS for [{}, {}]", lat, lon);
            results = findWithPostgres(lat, lon, radiusKm, pageable);
        }
        boolean hasNext = results.size() > pageable.getPageSize();
        List<Person> content = hasNext ? results.subList(0, pageable.getPageSize()) : results;
        return new SliceImpl<>(content, pageable, hasNext);
    }

    private List<Person> findWithRedisGEO(double lat, double lon, double radiusKm, Pageable pageable, String shardKey) {
        try {
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo()
                    .search(shardKey,
                            GeoReference.fromCoordinate(lon, lat),
                            new Distance(radiusKm, Metrics.KILOMETERS),
                            RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().sortAscending()
                                    .limit(redisSearchLimit));

            List<Person> redisPersons = new ArrayList<>();
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

                    redisPersons = orderedIds.stream()
                            .map(personMap::get)
                            .filter(Objects::nonNull)
                            .toList();
                }
            }
            return redisPersons;
        } catch (Exception e) {
            // Fallback
            log.error("Redis GEOSearch failed: {}", e.getMessage());
            return null;
        }
    }

    private List<Person> findWithPostgres(double lat, double lon, double radiusKm, Pageable pageable) {
        double radiusInDegrees = radiusKm / 111;
        return jpaRepo.findNearbyWithPostgis(
                lat, lon, radiusInDegrees,
                pageable.getPageSize() + 1,
                pageable.getOffset()
        );
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