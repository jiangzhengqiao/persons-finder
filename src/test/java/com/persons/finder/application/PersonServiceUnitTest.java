package com.persons.finder.application;


import com.persons.finder.domain.model.Location;
import com.persons.finder.domain.model.Person;
import com.persons.finder.domain.repository.PersonRepository;
import com.persons.finder.domain.service.BioGenerator;
import com.persons.finder.dto.LocationRequest;
import com.persons.finder.dto.PersonRequest;
import com.persons.finder.dto.PersonResponse;
import com.persons.finder.exception.PersonNotFoundException;
import com.persons.finder.infrastructure.security.SecurityManager;
import com.persons.finder.mapper.PersonMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // ← no Spring, just Mockito
class PersonServiceUnitTest {

    // These are FAKES — Mockito creates them automatically
    // No real DB, no real Redis, no real AI
    @Mock
    private PersonRepository personRepository;
    @Mock
    private BioGenerator bioGenerator;
    @Mock
    private PersonMapper personMapper;

    @Mock
    private SecurityManager securityManager;

    // This is the REAL PersonService, but with fake dependencies injected
    @InjectMocks
    private PersonService personService;

    @Captor
    private ArgumentCaptor<Person> personCaptor;

    @Test
    @DisplayName("createPerson: AI bio should be assigned to the saved person")
    void createPerson_ShouldAssignAiBio() {

        // Arrange — tell the fakes what to return
        when(bioGenerator.generateBio(any())).thenReturn("A great developer.");
        when(personRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(personMapper.toResponse(any())).thenReturn(
                new PersonResponse(1L, "Alice", "Dev", "Reading", "A great developer.", 0.0, 0.0, null)
        );

        PersonRequest request = new PersonRequest("Alice", "Dev", "Reading", 0.0, 0.0);

        // Act
        PersonResponse response = personService.createPerson(request);

        // Assert
        assertEquals("A great developer.", response.bio());
        verify(personRepository).save(any()); // confirm save was called
    }

    @Test
    @DisplayName("updateLocation: should update location and return updated person response")
    void updateLocation_ShouldUpdateLocationAndReturnResponse() {
        // Arrange
        Long id = 1L;
        LocationRequest request = new LocationRequest(12.34, 56.78);

        // Create a real Person object (assuming appropriate builder or constructor)
        Person existingPerson = Person.builder()
                .id(id)
                .name("Alice")
                .location(Location.fromCoordinates(0.0, 0.0))  // initial location
                .build();

        // Mock repository behavior
        when(personRepository.findById(id)).thenReturn(Optional.of(existingPerson));
        // When save is called, return the argument unchanged (simulate JPA's save behavior)
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Construct the expected response object (note: location in response should be updated)
        PersonResponse expectedResponse = new PersonResponse(
                id, "Alice", null, null, null,
                request.latitude(), request.longitude(), null  // assuming response contains latitude and longitude fields
        );
        when(personMapper.toResponse(any(Person.class))).thenReturn(expectedResponse);

        // Act
        PersonResponse actualResponse = personService.updateLocation(id, request);

        // Assert
        assertEquals(expectedResponse, actualResponse);

        // Verify repository method calls
        verify(personRepository).findById(id);
        verify(personRepository).save(personCaptor.capture());  // capture the argument passed to save

        // Verify that the saved person's location has been updated
        Person savedPerson = personCaptor.getValue();
        assertEquals(request.latitude(), savedPerson.getLocation().getLatitude());
        assertEquals(request.longitude(), savedPerson.getLocation().getLongitude());

        verify(personMapper).toResponse(savedPerson);  // Verify mapper is called with the saved person
        verifyNoMoreInteractions(personRepository, personMapper);  // Ensure no other unnecessary interactions
    }

    @Test
    @DisplayName("updateLocation: should throw PersonNotFoundException when person does not exist")
    void updateLocation_ShouldThrowExceptionWhenPersonNotFound() {
        // Arrange
        Long id = 999L;
        LocationRequest request = new LocationRequest(12.34, 56.78);

        when(personRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        PersonNotFoundException exception = assertThrows(PersonNotFoundException.class,
                () -> personService.updateLocation(id, request));

        assertEquals("Person not found with id: " + id, exception.getMessage());

        verify(personRepository).findById(id);
        verify(personRepository, never()).save(any());      // save should not be called
        verify(personMapper, never()).toResponse(any());    // mapper should not be called
    }
}