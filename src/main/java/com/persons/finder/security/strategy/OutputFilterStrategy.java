package com.persons.finder.security.strategy;

import com.persons.finder.repository.SecurityPatternRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutputFilterStrategy implements SecurityStrategy {

    private final SecurityPatternRepository patternRepository;

    @Override
    public void validateInput(String input) {
    }

    @Override
    public SanitizeResult  sanitizeOutput(String output) {
        if (output == null) return null;
        List<String> patterns = patternRepository.findPatternsByType("OUTPUT_FILTER");
        for (String pattern : patterns) {
            if (output.toLowerCase().contains(pattern.toLowerCase())) {
                log.warn("AI output blocked by pattern: {}", pattern);
                return new SanitizeResult("Dedicated professional with a diverse background.", true);            }
        }
        return new SanitizeResult(output, false);
    }
}