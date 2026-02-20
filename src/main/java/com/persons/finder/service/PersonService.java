package com.persons.finder.service;

import com.persons.finder.domain.Person;
import com.persons.finder.dto.LocationUpdateRequest;
import com.persons.finder.dto.PersonResponse;
import com.persons.finder.repository.PersonRepository;
import com.persons.finder.repository.SecurityPatternRepository;
import com.persons.finder.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final SecurityPatternRepository securityRepository;

    @Transactional(readOnly = true)
    public Page<PersonResponse> findNearby(double lat, double lon, double radiusKm, Pageable pageable) {
        log.info("Searching for persons near ({}, {}) within {}km, page: {}", lat, lon, radiusKm, pageable.getPageNumber());
        long startTime = System.currentTimeMillis();
        var box = GeoUtils.calculateBoundingBox(lat, lon, radiusKm);

        Pageable distancePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<PersonResponse> results = personRepository.findNearbyEfficiently(
                lat, lon, radiusKm, box.minLat(), box.maxLat(), box.minLon(), box.maxLon(), distancePageable
        ).map(this::convertToResponse);

        log.debug("Found {} results in {}ms", results.getTotalElements(), System.currentTimeMillis() - startTime);
        return results;
    }

    @Transactional
    public PersonResponse updateLocation(Long id, LocationUpdateRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Person not found"));

        person.setLatitude(request.latitude());
        person.setLongitude(request.longitude());

        // return DTO
        return new PersonResponse(person.getId(), person.getName(), person.getJobTitle(),
                person.getHobbies(), person.getBio(),
                person.getLatitude(), person.getLongitude(),
                java.time.LocalDateTime.now());
    }

    private PersonResponse convertToResponse(Person p) {
        return new PersonResponse(
                p.getId(), p.getName(), p.getJobTitle(),
                p.getHobbies(), p.getBio(), p.getLatitude(),
                p.getLongitude(), p.getCreatedAt()
        );
    }
}