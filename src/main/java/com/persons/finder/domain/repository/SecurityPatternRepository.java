package com.persons.finder.domain.repository;

import java.util.List;

public interface SecurityPatternRepository {

    List<String> findPatternsByType(String type);
}
