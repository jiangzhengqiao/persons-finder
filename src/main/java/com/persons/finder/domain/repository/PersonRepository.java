package com.persons.finder.domain.repository;

import com.persons.finder.domain.model.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;

public interface PersonRepository {
    Slice<Person> findNearby(double lat, double lon, double radiusKm, Pageable pageable);

    Optional<Person> findById(Long id);

    Person save(Person person);

    void deleteAll();
}