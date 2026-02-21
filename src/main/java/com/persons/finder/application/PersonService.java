package com.persons.finder.application;

import com.persons.finder.domain.model.Location;
import com.persons.finder.domain.model.Person;
import com.persons.finder.domain.service.BioGenerator;
import com.persons.finder.dto.LocationRequest;
import com.persons.finder.dto.PersonRequest;
import com.persons.finder.dto.PersonResponse;
import com.persons.finder.mapper.PersonMapper;
import com.persons.finder.domain.repository.PersonRepository;
import com.persons.finder.infrastructure.security.SecurityManager;
import com.persons.finder.infrastructure.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final BioGenerator bioGenerator;
    private final PersonMapper personMapper;
    private final SecurityManager securityManager;

    @Transactional(readOnly = true)
    public Slice<PersonResponse> findNearby(double lat, double lon, double radiusKm, Pageable pageable) {
        log.info("Searching for persons near ({}, {}) within {}km, page: {}", lat, lon, radiusKm, pageable.getPageNumber());
        long startTime = System.currentTimeMillis();
        var box = GeoUtils.calculateBoundingBox(lat, lon, radiusKm);

        Pageable distancePageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Slice<PersonResponse> results = personRepository.findNearbyEfficiently(
                lat, lon, radiusKm, box.minLat(), box.maxLat(), box.minLon(), box.maxLon(), distancePageable
        ).map(personMapper::toResponse);

        log.debug("Found {} results in {}ms", results.getNumberOfElements(), System.currentTimeMillis() - startTime);
        return results;
    }

    @Transactional
    public PersonResponse updateLocation(Long id, LocationRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Person not found"));

        person.setLocation(new Location(request.latitude(), request.longitude()));

        // return DTO
        return personMapper.toResponse(person);
    }

    @Transactional
    public PersonResponse createPerson(PersonRequest request) {
        String name = request.name();
        String hobbies = request.hobbies();
        log.info("Creating new person profile for: {}", name);

        securityManager.validateInput(hobbies);
        log.debug("Requesting AI bio generation for hobbies: {}", hobbies);

        Person person = Person.builder()
                .name(name)
                .jobTitle(request.jobTitle())
                .hobbies(hobbies)
                .location(new Location(request.latitude(), request.longitude()))
                .build();

        String bio = bioGenerator.generateBio(person);
        person.setBio(bio);

        Person saved = personRepository.save(person);
        return personMapper.toResponse(saved);
    }
}