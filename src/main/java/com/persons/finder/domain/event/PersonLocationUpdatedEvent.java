package com.persons.finder.domain.event;

import com.persons.finder.domain.model.Person;

public record PersonLocationUpdatedEvent(Person person) {
}