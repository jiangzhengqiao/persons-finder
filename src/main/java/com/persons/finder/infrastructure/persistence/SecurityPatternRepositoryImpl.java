package com.persons.finder.infrastructure.persistence;

import com.persons.finder.domain.repository.SecurityPatternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SecurityPatternRepositoryImpl implements SecurityPatternRepository {
    private final JpaSecurityPatternRepository jpaSecurityPatternRepository;

    @Override
    public List<String> findPatternsByType(String type) {
        return jpaSecurityPatternRepository.findPatternsByType(type);
    }
}
