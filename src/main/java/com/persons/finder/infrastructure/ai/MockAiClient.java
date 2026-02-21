package com.persons.finder.infrastructure.ai;

import com.persons.finder.infrastructure.ai.AiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.mock", havingValue = "true")
public class MockAiClient implements AiClient {

    @Override
    public String generate(String prompt) {
        log.info("Mock AI client generating bio from prompt: {}", prompt);
        return "A creative soul who loves exploring new ideas and enjoys coding, hiking, and good coffee.";
    }
}