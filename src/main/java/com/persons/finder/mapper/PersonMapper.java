package com.persons.finder.mapper;

import com.persons.finder.domain.model.Location;
import com.persons.finder.domain.model.Person;
import com.persons.finder.dto.PersonResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonMapper {
    default PersonResponse toResponse(Person person) {
        Location loc = person.getLocation();
        return new PersonResponse(
                person.getId(),
                person.getName(),
                person.getJobTitle(),
                person.getHobbies(),
                person.getBio(),
                loc != null ? loc.getLatitude() : null,
                loc != null ? loc.getLongitude() : null,
                person.getCreatedAt()
        );
    }
}
