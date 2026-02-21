package com.persons.finder.mapper;

import com.persons.finder.domain.model.Person;
import com.persons.finder.dto.PersonResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PersonMapper {
    PersonResponse toResponse(Person person);
}
