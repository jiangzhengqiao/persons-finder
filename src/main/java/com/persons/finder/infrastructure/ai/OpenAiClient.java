package com.persons.finder.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.ai.mock", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
public class OpenAiClient implements AiClient {

    private final RestTemplate restTemplate;

    @Value("${app.ai.api-url}")
    private String apiUrl;

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Value("${app.ai.model}")
    private String model;


    @Override
    public String generate(String prompt) {
        log.info("Preparing to send prompt to AI at: {}", apiUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<OpenAiResponse> response = restTemplate
                    .postForEntity(apiUrl, entity, OpenAiResponse.class);

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null
                    && !response.getBody().choices().isEmpty()) {
                return response.getBody().choices().get(0).message().content();
            }
            throw new RuntimeException("AI API responded with error: " + response.getStatusCode());
        } catch (Exception e) {
            log.error("AI service communication error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable, please try again later.");
        }
    }
}