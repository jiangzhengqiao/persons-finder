package com.persons.finder.dto;

import java.time.LocalDateTime;

public record PersonResponse(
        Long id,
        String name,
        String jobTitle,
        String hobbies,
        String bio,
        Double latitude,
        Double longitude,
        LocalDateTime createdAt
) {
}