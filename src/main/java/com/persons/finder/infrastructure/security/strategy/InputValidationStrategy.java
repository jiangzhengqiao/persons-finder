package com.persons.finder.infrastructure.security.strategy;

import com.persons.finder.exception.SecurityValidationException;
import com.persons.finder.domain.repository.SecurityPatternRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InputValidationStrategy implements SecurityStrategy {

    private final SecurityPatternRepository patternRepository;

    @Override
    public void validateInput(String input) {
        if (input == null) return;
        List<String> patterns = patternRepository.findPatternsByType("INPUT_FILTER");
        for (String pattern : patterns) {
            if (input.toLowerCase().contains(pattern.toLowerCase())) {
                throw new SecurityValidationException("Input violates security policy: " + pattern);
            }
        }
    }

    @Override
    public SanitizeResult sanitizeOutput(String output) {
        return new SanitizeResult(output, false);
    }
}
