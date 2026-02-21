package com.persons.finder.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.persons.finder.domain.model.Location;
import com.persons.finder.domain.model.Person;
import com.persons.finder.dto.LocationRequest;
import com.persons.finder.dto.PersonRequest;
import com.persons.finder.dto.PersonResponse;
import com.persons.finder.domain.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "app.ai.mock=true",
        "app.seed-data=false"
})
public class PersonControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUp() {
        personRepository.deleteAll();
    }

    @Test
    void createPerson_ShouldReturnCreatedPersonWithMockBio() throws Exception {
        // Given
        PersonRequest request = new PersonRequest(
                "John Doe",
                "Software Engineer",
                "Hiking, Photography",
                40.7128,
                -74.0060
        );

        // When
        ResponseEntity<PersonResponse> response = restTemplate.postForEntity(
                "/api/v1/persons",
                request,
                PersonResponse.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        PersonResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.id()).isNotNull();
        assertThat(body.name()).isEqualTo("John Doe");
        assertThat(body.bio()).isNotEmpty(); // MockAiClient 会生成一个 bio
        assertThat(body.location().getLatitude()).isEqualTo(40.7128);
        assertThat(body.location().getLongitude()).isEqualTo(-74.0060);
    }

    @Test
    void createPerson_WithInvalidInput_ShouldReturnBadRequest() {
        // name is ""
        PersonRequest invalidRequest = new PersonRequest(
                "",
                "Engineer",
                "Hiking",
                0.0,
                0.0
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/persons",
                invalidRequest,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void updateLocation_ShouldUpdateAndReturnUpdatedPerson() {
        // create
        PersonRequest createRequest = new PersonRequest(
                "Jane Doe",
                "Data Scientist",
                "Reading",
                34.0522,
                -118.2437
        );
        ResponseEntity<PersonResponse> createResponse = restTemplate.postForEntity(
                "/api/v1/persons",
                createRequest,
                PersonResponse.class
        );
        Long id = createResponse.getBody().id();

        double newLat = 40.7128;
        double newLon = -74.0060;
        var updateRequest = new LocationRequest(newLat, newLon);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LocationRequest> entity = new HttpEntity<>(updateRequest, headers);

        // update
        ResponseEntity<PersonResponse> updateResponse = restTemplate.exchange(
                "/api/v1/persons/{id}/location",
                HttpMethod.PUT,
                entity,
                PersonResponse.class,
                id
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        PersonResponse updated = updateResponse.getBody();

        assertThat(updated.location().getLatitude()).isEqualTo(newLat);
        assertThat(updated.location().getLongitude()).isEqualTo(newLon);
    }

    @Test
    void updateLocation_WithNonExistingId_ShouldReturnNotFound() {
        var updateRequest = new LocationRequest(10.0, 20.0);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LocationRequest> entity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/persons/99999/location",
                HttpMethod.PUT,
                entity,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void findNearby_ShouldReturnPeopleWithinRadiusSortedByDistance() {
        Person personA = new Person();
        personA.setName("Nearby Person");
        personA.setLocation(new Location(40.7130, -74.0065));
        personA.setBio("Bio A");
        personRepository.save(personA);

        Person personB = new Person();
        personB.setName("Medium Person");
        personB.setLocation(new Location(40.7150, -74.0100));
        personB.setBio("Bio B");
        personRepository.save(personB);

        Person personC = new Person();
        personC.setName("Far Person");
        personC.setLocation(new Location(41.0000, -74.0000));
        personC.setBio("Bio C");
        personRepository.save(personC);

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/v1/persons/nearby?lat=40.7128&lon=-74.0060&radius=5",
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();
        assertThat(body).isNotNull();


        JsonNode content = body.get("content");
        if (content == null) {
            content = body;
        }

        assertThat(content).hasSize(2);
        assertThat(content.get(0).get("name").asText()).isEqualTo("Nearby Person");
        assertThat(content.get(1).get("name").asText()).isEqualTo("Medium Person");
    }

    @Test
    void findNearby_WithNoResults_ShouldReturnEmptySlice() {
        ResponseEntity<JsonNode> response = restTemplate.getForEntity(
                "/api/v1/persons/nearby?lat=40.7128&lon=-74.0060&radius=100",
                JsonNode.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = response.getBody();

        JsonNode content = body.get("content");
        if (content == null) {
            content = body;
        }
        assertThat(content).isEmpty();
    }
}