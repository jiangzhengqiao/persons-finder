package com.persons.finder.application;

import com.persons.finder.domain.event.PersonLocationUpdatedEvent;
import com.persons.finder.domain.model.Location;
import com.persons.finder.domain.model.Person;
import com.persons.finder.domain.repository.PersonRepository;
import com.persons.finder.domain.service.BioGenerator;
import com.persons.finder.dto.LocationRequest;
import com.persons.finder.dto.NearbyRequest;
import com.persons.finder.dto.PersonRequest;
import com.persons.finder.dto.PersonResponse;
import com.persons.finder.exception.PersonNotFoundException;
import com.persons.finder.infrastructure.security.SecurityManager;
import com.persons.finder.mapper.PersonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public Slice<PersonResponse> findNearby(NearbyRequest request, Pageable pageable) {
        return personRepository.findNearby(request.lat(), request.lon(), request.radius(), pageable)
                .map(personMapper::toResponse);
    }

    @Transactional
    public PersonResponse updateLocation(Long id, LocationRequest request) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new PersonNotFoundException(id));

        Location location = Location.fromCoordinates(request.latitude(), request.longitude());
        person.updateLocation(location);

        Person saved = personRepository.save(person);
        eventPublisher.publishEvent(new PersonLocationUpdatedEvent(saved));
        return personMapper.toResponse(saved);
    }

    @Transactional
    public PersonResponse createPerson(PersonRequest request) {
        String name = request.name();
        String hobbies = request.hobbies();
        log.info("Creating new person profile for: {}", name);

        securityManager.validateInput(hobbies);
        log.debug("Requesting AI bio generation for hobbies: {}", hobbies);

        Location location = Location.fromCoordinates(request.latitude(), request.longitude());

        Person person = Person.builder()
                .name(name)
                .jobTitle(request.jobTitle())
                .hobbies(hobbies)
                .location(location)
                .build();

        String bio = bioGenerator.generateBio(person);
        person.assignBio(bio);

        Person saved = personRepository.save(person);
        eventPublisher.publishEvent(new PersonLocationUpdatedEvent(saved));
        return personMapper.toResponse(saved);
    }
}