package com.persons.finder.repository;

import com.persons.finder.domain.SecurityPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityPatternRepository extends JpaRepository<SecurityPattern, Long> {


}
