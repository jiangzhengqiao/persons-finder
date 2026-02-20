package com.persons.finder.repository;

import com.persons.finder.domain.SecurityPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityPatternRepository extends JpaRepository<SecurityPattern, Long> {

    @Query("SELECT s.pattern FROM SecurityPattern s WHERE s.type = :type")
    List<String> findPatternsByType(@Param("type") String type);

}
