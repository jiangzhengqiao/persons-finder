package com.persons.finder.security.strategy;

public interface SecurityStrategy {
    void validateInput(String input);

    SanitizeResult sanitizeOutput(String output);

}
