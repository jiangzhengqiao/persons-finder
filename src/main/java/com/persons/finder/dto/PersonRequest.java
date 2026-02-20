package com.persons.finder.dto;

import javax.validation.constraints.*;

public record PersonRequest(
        @NotBlank(message = "Name is required")
        String name,

        String jobTitle,

        @Size(max = 500, message = "Hobbies must be under 500 characters")
        @Pattern(
                regexp = "^[\\p{L}\\p{N}\\s,.!?'\"\\-]+$",
                message = "Hobbies contain forbidden special characters"
        )
        String hobbies,

        @NotNull(message = "Latitude is required")
        @Min(value = -90, message = "Latitude must be between -90 and 90")
        @Max(value = 90, message = "Latitude must be between -90 and 90")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @Min(value = -180, message = "Longitude must be between -180 and 180")
        @Max(value = 180, message = "Longitude must be between -180 and 180")
        Double longitude
) {
}