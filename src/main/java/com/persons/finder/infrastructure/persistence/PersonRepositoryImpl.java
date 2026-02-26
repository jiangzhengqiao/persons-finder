package com.persons.finder.infrastructure.persistence;

import com.persons.finder.domain.model.Person;
import com.persons.finder.domain.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.geo.*;
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

    @Override
    public Slice<Person> findNearby(double lat, double lon, double radiusKm, Pageable pageable) {
        try {
            // 1. 尝试使用 Redis 搜索
            GeoResults<RedisGeoCommands.GeoLocation<String>> geoResults = redisTemplate.opsForGeo()
                    .search(GEO_KEY,
                            GeoReference.fromCoordinate(lon, lat),
                            new Distance(radiusKm, Metrics.KILOMETERS),
                            RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().sortAscending().limit(500));

            if (geoResults != null && !geoResults.getContent().isEmpty()) {
                List<GeoResult<RedisGeoCommands.GeoLocation<String>>> allResults = geoResults.getContent();
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), allResults.size());

                if (start < allResults.size()) {
                    List<Long> orderedIds = allResults.subList(start, end).stream()
                            .map(res -> Long.parseLong(res.getContent().getName()))
                            .toList();

                    // 批量获取 DB 数据
                    Map<Long, Person> personMap = jpaRepo.findAllByIdIn(orderedIds).stream()
                            .collect(Collectors.toMap(Person::getId, p -> p));

                    // 保持 Redis 返回的距离顺序
                    List<Person> orderedPersons = orderedIds.stream()
                            .map(personMap::get)
                            .filter(Objects::nonNull)
                            .toList();

                    return new SliceImpl<>(orderedPersons, pageable, allResults.size() > end);
                }
            }
            return new SliceImpl<>(Collections.emptyList(), pageable, false);

        } catch (Exception e) {
            // 2. Redis 异常时触发降级：直接查询 PostGIS
            log.error("CRITICAL: Redis search failed. Falling back to PostGIS spatial query for lat:{}, lon:{}", lat, lon, e);

            List<Person> dbResults = jpaRepo.findNearbyWithPostgis(
                    lat, lon, radiusKm,
                    pageable.getPageSize() + 1, // 多查一个用于判断 hasNext
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
        // 1. save db
        Person saved = jpaRepo.save(person);

        // 2. update Redis GEO
        if (saved.getLocation() != null) {
            redisTemplate.opsForGeo().add(GEO_KEY,
                    new Point(
                            saved.getLocation().getLongitude(),
                            saved.getLocation().getLatitude()
                    ),
                    saved.getId().toString()
            );
        }
        return saved;
    }

    @Override
    public void deleteAll() {
        // 1. Clean database
        jpaRepo.deleteAll();

        // 2. Clean up GEO Key in Redis
        redisTemplate.delete(GEO_KEY);
    }
}