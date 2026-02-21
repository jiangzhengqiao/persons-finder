package com.persons.finder.infrastructure.security;

import com.persons.finder.infrastructure.security.strategy.SanitizeResult;
import com.persons.finder.infrastructure.security.strategy.SecurityStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityManager {

    private final List<SecurityStrategy> strategies;

    public void validateInput(String input) {
        for (SecurityStrategy strategy : strategies) {
            strategy.validateInput(input);
        }
    }

    public String sanitizeOutput(String output) {
        String result = output;
        for (SecurityStrategy strategy : strategies) {
            SanitizeResult sr = strategy.sanitizeOutput(result);
            result = sr.getValue();
            if (sr.isStopChain()) {
                break;
            }
        }
        return result;
    }
}
