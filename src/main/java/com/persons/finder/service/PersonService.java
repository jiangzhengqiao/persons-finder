package com.persons.finder.service;

import com.persons.finder.domain.Person;
import com.persons.finder.dto.LocationUpdateRequest;
import com.persons.finder.dto.PersonRequest;
import com.persons.finder.dto.PersonResponse;
import com.persons.finder.exception.SecurityValidationException;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;
    private final SecurityPatternRepository securityRepository;
    private final AiClient aiClient;

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

    @Transactional
    public PersonResponse createPerson(PersonRequest request) {
        log.info("Creating new person profile for: {}", request.name());
        validateInputSafety(request.hobbies());

        log.debug("Requesting AI bio generation for hobbies: {}", request.hobbies());

        String rawBio = generateBioWithPromptEngineering(request);
        String filteredBio = sanitizeAiOutput(rawBio);
        if (!filteredBio.equals(rawBio)) {
            log.warn("AI output for user '{}' was sanitized due to security patterns.", request.name());
        }
        Person person = Person.builder()
                .name(request.name())
                .jobTitle(request.jobTitle())
                .hobbies(request.hobbies())
                .bio(filteredBio)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        return convertToResponse(personRepository.save(person));
    }

    private PersonResponse convertToResponse(Person p) {
        return new PersonResponse(
                p.getId(), p.getName(), p.getJobTitle(),
                p.getHobbies(), p.getBio(), p.getLatitude(),
                p.getLongitude(), p.getCreatedAt()
        );
    }

    private String generateBioWithPromptEngineering(PersonRequest request) {
        if (request.hobbies() == null || request.hobbies().isBlank()) {
            return "A mysterious individual with no specific hobbies shared.";
        }

        String prompt = String.format(
                "Role: Create a professional bio.\n" +
                        "Name: %s\n" +
                        "Job: %s\n" +
                        "Hobbies: %s\n" +
                        "Constraint: Maximum 20 words.",
                request.name(), request.jobTitle(), request.hobbies()
        );

        return aiClient.generate(prompt);
    }

    private void validateInputSafety(String input) {
        if (input == null) return;
        List<String> filters = securityRepository.findPatternsByType("INPUT_FILTER");
        for (String pattern : filters) {
            if (input.toLowerCase().contains(pattern.toLowerCase())) {
                throw new SecurityValidationException("Input violates security policy: " + pattern);
            }
        }
    }

    private String sanitizeAiOutput(String rawBio) {
        List<String> filters = securityRepository.findPatternsByType("OUTPUT_FILTER");
        for (String pattern : filters) {
            if (rawBio != null && rawBio.toLowerCase().contains(pattern.toLowerCase())) {
                log.warn("AI output blocked by pattern: {}", pattern);
                return "Dedicated professional with a diverse background.";
            }
        }
        return rawBio;
    }
}