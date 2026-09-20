package com.aireadiness.aireview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aireadiness.aireview.dto.AIReviewPromptContext;
import com.aireadiness.aireview.dto.AIReviewResult;
import com.aireadiness.aireview.model.AIReviewConfidence;
import com.aireadiness.aireview.util.AIReviewPromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class OpenAILLMService implements LLMService {

    private static final Logger log = LoggerFactory.getLogger(OpenAILLMService.class);

    private final String apiKey;
    private final String model;
    private final String endpoint;
    private final int timeoutMs;
    private final int maxRetries;

    private final AIReviewPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public OpenAILLMService(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.llm.model:gpt-4o-mini}") String model,
            @Value("${openai.llm.endpoint:https://api.openai.com/v1/chat/completions}") String endpoint,
            @Value("${openai.llm.timeout-ms:15000}") int timeoutMs,
            @Value("${openai.llm.max-retries:2}") int maxRetries,
            AIReviewPromptBuilder promptBuilder,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model != null ? model.trim() : "gpt-4o-mini";
        this.endpoint = endpoint != null ? endpoint.trim() : "https://api.openai.com/v1/chat/completions";
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : 15000;
        this.maxRetries = Math.max(0, maxRetries);
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(this.timeoutMs);
        requestFactory.setReadTimeout(this.timeoutMs);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public AIReviewResult generateAIReview(AIReviewPromptContext context) {
        if (context == null || context.getFinding() == null) {
            throw new IllegalArgumentException("AIReviewPromptContext and finding must not be null");
        }

        if (apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key is missing. Please configure OPENAI_API_KEY environment variable.");
        }

        String systemPrompt = promptBuilder.buildSystemPrompt();
        String userPrompt = promptBuilder.buildUserPrompt(context);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.2);
        requestBody.put("response_format", Map.of("type", "json_object"));

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
        requestBody.put("messages", messages);

        Exception lastException = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                String responseJson = restClient.post()
                        .uri(endpoint)
                        .header("Authorization", "Bearer " + apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(String.class);

                if (responseJson == null || responseJson.trim().isEmpty()) {
                    throw new IllegalStateException("Received empty HTTP response from OpenAI Chat Completions API");
                }

                return parseAndValidateLLMResponse(responseJson);
            } catch (Exception e) {
                lastException = e;
                if (e instanceof IllegalStateException || e instanceof IllegalArgumentException || !isTransientError(e) || attempt == maxRetries) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    throw new RuntimeException("OpenAI LLM API review generation failed: " + e.getMessage(), e);
                }
                log.warn("Attempt {}/{} failed with transient error: {}", attempt + 1, maxRetries + 1, e.getClass().getSimpleName());
            }
        }

        log.error("Failed to generate AI review after {} retries", maxRetries + 1);
        throw new RuntimeException("OpenAI LLM API review generation failed: " + (lastException != null ? lastException.getMessage() : "Unknown error"), lastException);
    }

    public boolean isTransientError(Throwable e) {
        if (e instanceof org.springframework.web.client.HttpStatusCodeException) {
            org.springframework.http.HttpStatusCode status = ((org.springframework.web.client.HttpStatusCodeException) e).getStatusCode();
            int value = status.value();
            if (value == 400 || value == 401 || value == 403) {
                return false;
            }
            if (value == 429 || value >= 500) {
                return true;
            }
            return false;
        }
        if (e instanceof org.springframework.web.client.ResourceAccessException) {
            return true;
        }
        if (e instanceof java.io.IOException || e instanceof java.net.SocketTimeoutException) {
            return true;
        }
        return false;
    }

    public AIReviewResult parseAndValidateLLMResponse(String rawJson) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawJson);
            JsonNode choicesNode = rootNode.path("choices");

            String contentJson = null;
            if (choicesNode.isArray() && choicesNode.size() > 0) {
                contentJson = choicesNode.get(0).path("message").path("content").asText();
            } else if (rootNode.has("summary")) {
                // Support direct structured response payload for direct mock testing
                contentJson = rawJson;
            }

            if (contentJson == null || contentJson.trim().isEmpty()) {
                throw new IllegalStateException("OpenAI LLM response did not contain content message in choices array");
            }

            JsonNode reviewNode = objectMapper.readTree(contentJson);

            String summary = reviewNode.path("summary").asText("").trim();
            String whyItMatters = reviewNode.path("whyItMatters").asText("").trim();
            String suggestedFix = reviewNode.path("suggestedFix").asText("").trim();

            if (!reviewNode.has("confidence") || reviewNode.path("confidence").isNull()) {
                throw new IllegalStateException("LLM response confidence field is missing");
            }
            String confidenceStr = reviewNode.path("confidence").asText("").trim().toUpperCase();

            List<String> whatToReview = new ArrayList<>();
            JsonNode reviewItemsNode = reviewNode.path("whatToReview");
            if (reviewItemsNode.isArray()) {
                for (JsonNode item : reviewItemsNode) {
                    String val = item.asText("").trim();
                    if (!val.isEmpty()) {
                        whatToReview.add(truncate(val, 500));
                    }
                    if (whatToReview.size() >= 10) break; // Max 10 bullet items
                }
            }

            if (summary.isEmpty()) {
                throw new IllegalStateException("LLM response summary field is missing or blank");
            }
            if (whyItMatters.isEmpty()) {
                throw new IllegalStateException("LLM response whyItMatters field is missing or blank");
            }

            AIReviewConfidence confidence;
            try {
                confidence = AIReviewConfidence.valueOf(confidenceStr);
            } catch (Exception e) {
                throw new IllegalStateException("LLM response confidence field is invalid: " + confidenceStr);
            }

            AIReviewResult result = new AIReviewResult();
            result.setSummary(truncate(summary, 1000));
            result.setWhyItMatters(truncate(whyItMatters, 2000));
            result.setWhatToReview(whatToReview);
            result.setSuggestedFix(truncate(suggestedFix, 3000));
            result.setConfidence(confidence);

            return result;
        } catch (Exception e) {
            if (e instanceof IllegalStateException) {
                throw (IllegalStateException) e;
            }
            throw new IllegalStateException("Failed to parse structured LLM response JSON: " + e.getMessage(), e);
        }
    }

    private String truncate(String val, int maxLen) {
        if (val == null) return "";
        if (val.length() <= maxLen) return val;
        return val.substring(0, maxLen);
    }

    public String getModel() {
        return model;
    }

    public boolean isApiKeyConfigured() {
        return !apiKey.isEmpty();
    }
}
