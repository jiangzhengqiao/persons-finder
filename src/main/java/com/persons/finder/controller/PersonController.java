package com.persons.finder.controller;

import com.persons.finder.dto.LocationUpdateRequest;
import com.persons.finder.dto.PersonRequest;
import com.persons.finder.dto.PersonResponse;
import com.persons.finder.service.PersonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/persons") // version control is very important
@RequiredArgsConstructor
@Tag(name = "Person Management", description = "APIs for location-based search and AI profile creation")
public class PersonController {

    private final PersonService personService;

    @GetMapping("/nearby")
    @Operation(summary = "Find nearby people", description = "Returns a paginated list of people within a specified radius, sorted by proximity.")
    public ResponseEntity<Page<PersonResponse>> getNearby(
            @Parameter(description = "Center latitude, e.g., -36.8485") @RequestParam double lat,
            @Parameter(description = "Center longitude, e.g., 174.7633") @RequestParam double lon,
            @Parameter(description = "Radius in kilometers") @RequestParam(defaultValue = "10.0") double radius,
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {

        Page<PersonResponse> nearbyPeople = personService.findNearby(lat, lon, radius, pageable);
        return ResponseEntity.ok(nearbyPeople);
    }

    @PutMapping("/{id}/location")
    @Operation(summary = "Update location", description = "Updates the GPS coordinates for an existing person.")
    public PersonResponse updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request) {

        return personService.updateLocation(id, request);
    }

    @PostMapping
    @Operation(summary = "Create a person", description = "Creates a new person and generates AI bio.")
    public ResponseEntity<PersonResponse> createPerson(@Valid @RequestBody PersonRequest request) {

        // 1. 调用 Service，这里面已经包含了：
        //    AI 生成 -> 安全脱敏 -> 存入数据库
        PersonResponse response = personService.createPerson(request);

        // 2. 返回 201 Created 状态码，并带上完整的对象
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
