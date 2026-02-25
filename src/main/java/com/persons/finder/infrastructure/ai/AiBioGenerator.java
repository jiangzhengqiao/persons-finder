package com.persons.finder.infrastructure.ai;

import com.persons.finder.domain.model.Person;
import com.persons.finder.domain.service.BioGenerator;
import com.persons.finder.infrastructure.security.SecurityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiBioGenerator implements BioGenerator {

    private final AiClient aiClient;
    private final SecurityManager securityManager;

    @Override
    public String generateBio(Person person) {
        String prompt = """
        Role: Create a professional bio.
        Name: %s
        Job: %s
        Hobbies: %s
        Constraint: Maximum 20 words.
        """.formatted(person.getName(), person.getJobTitle(), person.getHobbies());
        String raw = aiClient.generate(prompt);
        return securityManager.sanitizeOutput(raw);
    }
}