package com.persons.finder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class OpenAiClient implements AiClient {

    private final RestTemplate restTemplate;

    @Value("${app.ai.api-url}")
    private String apiUrl;

    @Value("${app.ai.api-key}")
    private String apiKey;

    @Override
    public String generate(String prompt) {
        log.info("Preparing to send prompt to AI at: {}", apiUrl);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 这里的解析逻辑保持不变
                List choices = (List) response.getBody().get("choices");
                Map message = (Map) ((Map) choices.get(0)).get("message");
                return (String) message.get("content");
            }

            throw new RuntimeException("AI API responded with error status: " + response.getStatusCode());

        } catch (Exception e) {
            log.error("AI service communication error: {}", e.getMessage());
            throw new RuntimeException("AI service unavailable, please try again later.");
        }
    }
}