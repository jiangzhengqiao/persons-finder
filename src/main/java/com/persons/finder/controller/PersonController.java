package com.persons.finder.controller;

import com.persons.finder.application.PersonService;
import com.persons.finder.dto.LocationRequest;
import com.persons.finder.dto.NearbyRequest;
import com.persons.finder.dto.PersonRequest;
import com.persons.finder.dto.PersonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/v1/persons") // version control is very important
@RequiredArgsConstructor
@Validated
@Tag(name = "Person Management", description = "APIs for location-based search and AI profile creation")
public class PersonController {

    private final PersonService personService;

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby people", description = "Returns a sliced list of people within a specified radius, sorted by proximity.")
    public ResponseEntity<Slice<PersonResponse>> getNearby(
            @Valid NearbyRequest request,
            @PageableDefault(size = 20) Pageable pageable) {

        Slice<PersonResponse> nearbyPeople = personService.findNearby(request, pageable);
        return ResponseEntity.ok(nearbyPeople);
    }

    @PutMapping("/{id}/location")
    @Operation(summary = "Update location", description = "Updates the GPS coordinates for an existing person.")
    public PersonResponse updateLocation(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody LocationRequest request) {

        return personService.updateLocation(id, request);
    }

    @PostMapping
    @Operation(summary = "Create a person", description = "Creates a new person and generates AI bio.")
    public ResponseEntity<PersonResponse> createPerson(@Valid @RequestBody PersonRequest request) {
        PersonResponse response = personService.createPerson(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
