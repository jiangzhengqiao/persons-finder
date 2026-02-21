package com.persons.finder.domain.repository;

import com.persons.finder.domain.model.SecurityPattern;
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
