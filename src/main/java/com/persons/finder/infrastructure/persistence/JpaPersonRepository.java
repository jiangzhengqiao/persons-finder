package com.persons.finder.infrastructure.persistence;

import com.persons.finder.domain.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface JpaPersonRepository extends JpaRepository<Person, Long> {
    // 专门用于批量回填，且能保证性能
    List<Person> findAllByIdIn(Collection<Long> ids);

    // 使用原生 PostGIS 语法
    // 1. ST_DWithin: 用于过滤范围（单位为米，所以 radiusKm * 1000）
    // 2. <-> : 空间排序操作符，利用索引进行最近邻排序
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

    List<Person> findAllByIdIn(List<Long> ids);
}