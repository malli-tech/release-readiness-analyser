package com.aireadiness.knowledge;

import com.aireadiness.knowledge.service.OpenAIEmbeddingService;
import com.aireadiness.knowledge.service.OpenAIEmbeddingService.OpenAIEmbeddingData;
import com.aireadiness.knowledge.service.OpenAIEmbeddingService.OpenAIEmbeddingResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OpenAIEmbeddingServiceTest {

    @Test
    @DisplayName("1. Throws IllegalStateException when OPENAI_API_KEY is missing without logging secret")
    public void testMissingApiKeyThrowsException() {
        String mockSecretKey = "sk-proj-secret123456789";
        OpenAIEmbeddingService service = new OpenAIEmbeddingService(
                "",
                "text-embedding-3-small",
                "https://api.openai.com/v1/embeddings",
                5000,
                1536
        );

        assertFalse(service.isApiKeyConfigured());
        assertEquals(1536, service.getDimension());
        assertEquals("text-embedding-3-small", service.getModel());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.embed("Test text"));
        assertTrue(ex.getMessage().contains("OpenAI API key is missing"));
        assertFalse(ex.getMessage().contains(mockSecretKey));
    }

    @Test
    @DisplayName("2. Throws IllegalArgumentException for null or blank embedding input")
    public void testNullOrBlankInputValidation() {
        OpenAIEmbeddingService service = new OpenAIEmbeddingService(
                "mock-api-key",
                "text-embedding-3-small",
                "https://api.openai.com/v1/embeddings",
                5000,
                1536
        );

        assertTrue(service.isApiKeyConfigured());

        assertThrows(IllegalArgumentException.class, () -> service.embed(null));
        assertThrows(IllegalArgumentException.class, () -> service.embed("   "));
        assertThrows(IllegalArgumentException.class, () -> service.embedBatch(List.of("Valid", "   ")));
        assertTrue(service.embedBatch(Collections.emptyList()).isEmpty());
    }

    @Test
    @DisplayName("3. Matching embedding vector dimension succeeds")
    public void testMatchingEmbeddingDimensionSucceeds() {
        OpenAIEmbeddingService service = new OpenAIEmbeddingService(
                "mock-key",
                "text-embedding-3-small",
                "https://api.openai.com/v1/embeddings",
                5000,
                3
        );

        OpenAIEmbeddingResponse response = new OpenAIEmbeddingResponse();
        OpenAIEmbeddingData data = new OpenAIEmbeddingData();
        data.setIndex(0);
        data.setEmbedding(List.of(0.1, 0.2, 0.3));
        response.setData(List.of(data));

        List<List<Double>> vectors = service.extractAndValidateVectors(response);

        assertEquals(1, vectors.size());
        assertEquals(List.of(0.1, 0.2, 0.3), vectors.get(0));
    }

    @Test
    @DisplayName("4. Mismatched embedding vector dimension fails with IllegalStateException")
    public void testMismatchedEmbeddingDimensionFails() {
        OpenAIEmbeddingService service = new OpenAIEmbeddingService(
                "mock-key",
                "text-embedding-3-small",
                "https://api.openai.com/v1/embeddings",
                5000,
                1536
        );

        OpenAIEmbeddingResponse response = new OpenAIEmbeddingResponse();
        OpenAIEmbeddingData data = new OpenAIEmbeddingData();
        data.setIndex(0);
        data.setEmbedding(List.of(0.1, 0.2, 0.3)); // 3 dimensions instead of 1536
        response.setData(List.of(data));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> service.extractAndValidateVectors(response));
        assertTrue(ex.getMessage().contains("Returned embedding vector dimension (3) does not match configured dimension (1536)"));
    }

    @Test
    @DisplayName("5. Correctly initializes custom model and dimension parameters")
    public void testCustomConfigParameters() {
        OpenAIEmbeddingService service = new OpenAIEmbeddingService(
                "mock-key",
                "text-embedding-3-large",
                "https://custom-endpoint.com/v1/embeddings",
                10000,
                3072
        );

        assertEquals("text-embedding-3-large", service.getModel());
        assertEquals("https://custom-endpoint.com/v1/embeddings", service.getEndpoint());
        assertEquals(3072, service.getDimension());
    }
}
