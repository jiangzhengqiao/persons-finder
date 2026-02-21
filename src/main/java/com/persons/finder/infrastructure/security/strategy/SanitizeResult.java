package com.persons.finder.infrastructure.security.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SanitizeResult {
    private final String value;
    private final boolean stopChain;
}
