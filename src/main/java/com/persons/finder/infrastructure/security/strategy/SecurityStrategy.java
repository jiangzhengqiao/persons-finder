package com.persons.finder.infrastructure.security.strategy;

public interface SecurityStrategy {
    void validateInput(String input);

    SanitizeResult sanitizeOutput(String output);

}
