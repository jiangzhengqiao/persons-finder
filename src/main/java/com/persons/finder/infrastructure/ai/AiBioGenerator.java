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
        String prompt = String.format(
                "Role: Create a professional bio.\n" +
                        "Name: %s\n" +
                        "Job: %s\n" +
                        "Hobbies: %s\n" +
                        "Constraint: Maximum 20 words.",
                person.getName(), person.getJobTitle(), person.getHobbies()
        );
        String raw = aiClient.generate(prompt);
        return securityManager.sanitizeOutput(raw);
    }
}