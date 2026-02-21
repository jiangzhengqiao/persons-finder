package com.persons.finder.domain.service;

import com.persons.finder.domain.model.Person;

public interface BioGenerator {
    String generateBio(Person person);
}
