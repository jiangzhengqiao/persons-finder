package com.persons.finder.service;


import com.persons.finder.domain.Person;
import com.persons.finder.dto.PersonResponse;
import com.persons.finder.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Automatic rollback without polluting the database
class PersonServiceTest {

    @Autowired
    private PersonService personService;

    @Autowired
    private PersonRepository personRepository;

    private Long savedPersonId;

    @BeforeEach
    void setUp() {
        personRepository.deleteAll();
        Person p = new Person();
        p.setName("Leo");
        p.setJobTitle("Interviewer");
        p.setLatitude(-41.2865);
        p.setLongitude(174.7762);
        p = personRepository.save(p);
        savedPersonId = p.getId();
    }

    @Test
    void updateLocation_Success() {
        double newLat = -36.8485; // Auckland
        double newLon = 174.7633;

        personService.updateLocation(savedPersonId, newLat, newLon);

        Person updated = personRepository.findById(savedPersonId).orElseThrow();
        assertEquals(newLat, updated.getLatitude(), 0.0001);
        assertEquals(newLon, updated.getLongitude(), 0.0001);
        System.out.println("updateLocation success");
    }

    @Test
    void updateLocation_NotFound() {
        assertThrows(RuntimeException.class, () -> {
            personService.updateLocation(99999L, 0.0, 0.0);
        });
        System.out.println("updateLocation success");
    }

    @Test
    void findNearby_SpatialAccuracy() {
        Person nearPerson = new Person();
        nearPerson.setName("Nearby User");
        nearPerson.setLatitude(-41.32);
        nearPerson.setLongitude(174.78);
        personRepository.save(nearPerson);

        Person farPerson = new Person();
        farPerson.setName("Far User");
        farPerson.setLatitude(-36.84);
        farPerson.setLongitude(174.76);
        personRepository.save(farPerson);

        Page<PersonResponse> results10km = personService.findNearby(
                -41.2865, 174.7762, 10.0, PageRequest.of(0, 10));
        assertEquals(2, results10km.getTotalElements(), "2 people should be found within 10km");

        Page<PersonResponse> results1km = personService.findNearby(
                -41.2865, 174.7762, 1.0, PageRequest.of(0, 10));
        assertEquals(1, results1km.getTotalElements(), "there should be only one person within 1km");

        System.out.println("findNearby success");
    }

    @Test
    void findNearby_Pagination() {
        // add 2 people
        for (int i = 0; i < 2; i++) {
            Person p = new Person();
            p.setName("Extra " + i);
            p.setLatitude(-41.28);
            p.setLongitude(174.77);
            personRepository.save(p);
        }

        // 3 people in total, 2 people per page
        Page<PersonResponse> page0 = personService.findNearby(
                -41.28, 174.77, 10.0, PageRequest.of(0, 2));

        assertEquals(2, page0.getContent().size());
        assertTrue(page0.hasNext(), "should be a next page");

        System.out.println("findNearby success");
    }
}
