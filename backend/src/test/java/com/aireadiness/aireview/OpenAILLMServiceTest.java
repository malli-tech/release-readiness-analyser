package com.aireadiness.aireview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aireadiness.aireview.dto.AIReviewResult;
import com.aireadiness.aireview.model.AIReviewConfidence;
import com.aireadiness.aireview.service.OpenAILLMService;
import com.aireadiness.aireview.util.AIReviewPromptBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenAILLMServiceTest {

    private OpenAILLMService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        service = new OpenAILLMService(
                "",
                "gpt-4o-mini",
                "https://api.openai.com/v1/chat/completions",
                5000,
                2,
                new AIReviewPromptBuilder(),
                objectMapper
        );
    }

    @Test
    @DisplayName("1. Throws IllegalStateException when OPENAI_API_KEY is missing")
    public void testMissingApiKeyThrowsException() {
        assertFalse(service.isApiKeyConfigured());
        assertEquals("gpt-4o-mini", service.getModel());

        com.aireadiness.aireview.dto.AIReviewPromptContext dummyContext = new com.aireadiness.aireview.dto.AIReviewPromptContext(
                new com.aireadiness.model.ProjectProfile(),
                new com.aireadiness.model.Finding(),
                "snippet",
                java.util.Collections.emptyList()
        );
        assertThrows(IllegalStateException.class, () -> service.generateAIReview(dummyContext));
    }

    @Test
    @DisplayName("2. Parses structured LLM response JSON accurately")
    public void testParseAndValidateLLMResponseSuccess() {
        String jsonPayload = """
            {
              "choices": [
                {
                  "message": {
                    "content": "{\\"summary\\": \\"Potential SQL Injection vulnerability detected.\\", \\"whyItMatters\\": \\"Unsanitized input in SQL statements allows arbitrary database execution.\\", \\"whatToReview\\": [\\"Inspect UserRepository.java line 42\\", \\"Verify prepareStatement usage\\"], \\"suggestedFix\\": \\"Use parameterized PreparedStatement queries.\\", \\"confidence\\": \\"HIGH\\"}"
                  }
                }
              ]
            }
            """;

        AIReviewResult result = service.parseAndValidateLLMResponse(jsonPayload);

        assertNotNull(result);
        assertEquals("Potential SQL Injection vulnerability detected.", result.getSummary());
        assertEquals("Unsanitized input in SQL statements allows arbitrary database execution.", result.getWhyItMatters());
        assertEquals(2, result.getWhatToReview().size());
        assertEquals("Inspect UserRepository.java line 42", result.getWhatToReview().get(0));
        assertEquals("Use parameterized PreparedStatement queries.", result.getSuggestedFix());
        assertEquals(AIReviewConfidence.HIGH, result.getConfidence());
    }

    @Test
    @DisplayName("3. Rejects response with missing summary")
    public void testParseMissingSummaryFails() {
        String jsonPayload = """
            {
              "choices": [
                {
                  "message": {
                    "content": "{\\"whyItMatters\\": \\"Some impact\\", \\"confidence\\": \\"MEDIUM\\"}"
                  }
                }
              ]
            }
            """;

        assertThrows(IllegalStateException.class, () -> service.parseAndValidateLLMResponse(jsonPayload));
    }

    @Test
    @DisplayName("4. Rejects response with invalid confidence value")
    public void testParseInvalidConfidenceFails() {
        String jsonPayload = """
            {
              "choices": [
                {
                  "message": {
                    "content": "{\\"summary\\": \\"SQL Injection\\", \\"whyItMatters\\": \\"High impact\\", \\"confidence\\": \\"EXTREME\\"}"
                  }
                }
              ]
            }
            """;

        assertThrows(IllegalStateException.class, () -> service.parseAndValidateLLMResponse(jsonPayload));
    }

    @Test
    @DisplayName("5. Verifies error classification: 400/401/403 permanent vs 429/5xx/timeout transient")
    public void testErrorClassification() {
        // Permanent errors (must not retry)
        assertFalse(service.isTransientError(new org.springframework.web.client.HttpClientErrorException(org.springframework.http.HttpStatus.BAD_REQUEST)));
        assertFalse(service.isTransientError(new org.springframework.web.client.HttpClientErrorException(org.springframework.http.HttpStatus.UNAUTHORIZED)));
        assertFalse(service.isTransientError(new org.springframework.web.client.HttpClientErrorException(org.springframework.http.HttpStatus.FORBIDDEN)));

        // Transient errors (can retry)
        assertTrue(service.isTransientError(new org.springframework.web.client.HttpClientErrorException(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)));
        assertTrue(service.isTransientError(new org.springframework.web.client.HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)));
        assertTrue(service.isTransientError(new org.springframework.web.client.HttpServerErrorException(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE)));
        assertTrue(service.isTransientError(new org.springframework.web.client.ResourceAccessException("Read timed out")));
        assertTrue(service.isTransientError(new java.net.SocketTimeoutException("Connect timed out")));
    }
}
