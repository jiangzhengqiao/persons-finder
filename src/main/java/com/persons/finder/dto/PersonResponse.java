package com.persons.finder.dto;

import com.persons.finder.domain.model.Location;

import java.time.LocalDateTime;

public record PersonResponse(
        Long id,
        String name,
        String jobTitle,
        String hobbies,
        String bio,
        Location location,
        LocalDateTime createdAt
) {
}