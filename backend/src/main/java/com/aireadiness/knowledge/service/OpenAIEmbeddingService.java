package com.aireadiness.knowledge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenAIEmbeddingService implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(OpenAIEmbeddingService.class);

    private final String apiKey;
    private final String model;
    private final String endpoint;
    private final int timeoutMs;
    private final int dimension;
    private final RestClient restClient;

    public OpenAIEmbeddingService(
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.embedding.model:text-embedding-3-small}") String model,
            @Value("${openai.embedding.endpoint:https://api.openai.com/v1/embeddings}") String endpoint,
            @Value("${openai.embedding.timeout-ms:5000}") int timeoutMs,
            @Value("${openai.embedding.dimension:1536}") int dimension
    ) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.model = model != null ? model.trim() : "text-embedding-3-small";
        this.endpoint = endpoint != null ? endpoint.trim() : "https://api.openai.com/v1/embeddings";
        this.timeoutMs = timeoutMs > 0 ? timeoutMs : 5000;
        this.dimension = dimension > 0 ? dimension : 1536;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(this.timeoutMs);
        requestFactory.setReadTimeout(this.timeoutMs);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public List<Double> embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text content for embedding generation must not be null or blank");
        }
        List<List<Double>> results = embedBatch(List.of(text));
        if (results.isEmpty()) {
            throw new IllegalStateException("OpenAI Embedding API returned empty response for single text input");
        }
        return results.get(0);
    }

    @Override
    public List<List<Double>> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> nonBlankTexts = texts.stream()
                .map(t -> t == null ? "" : t.trim())
                .collect(Collectors.toList());

        for (String t : nonBlankTexts) {
            if (t.isEmpty()) {
                throw new IllegalArgumentException("Batch texts contain empty or blank content which cannot be embedded");
            }
        }

        if (apiKey.isEmpty()) {
            throw new IllegalStateException("OpenAI API key is missing. Please configure the OPENAI_API_KEY environment variable.");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("input", nonBlankTexts);

        try {
            OpenAIEmbeddingResponse response = restClient.post()
                    .uri(endpoint)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(OpenAIEmbeddingResponse.class);

            return extractAndValidateVectors(response);
        } catch (Exception e) {
            if (e instanceof IllegalStateException || e instanceof IllegalArgumentException) {
                throw e;
            }
            log.error("Failed to generate vector embeddings from OpenAI API: {}", e.getMessage());
            throw new RuntimeException("OpenAI embedding API call failed: " + e.getMessage(), e);
        }
    }

    public List<List<Double>> extractAndValidateVectors(OpenAIEmbeddingResponse response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new IllegalStateException("OpenAI Embedding API returned a null or empty data payload");
        }

        List<OpenAIEmbeddingData> sortedData = new ArrayList<>(response.getData());
        sortedData.sort(Comparator.comparingInt(OpenAIEmbeddingData::getIndex));

        List<List<Double>> resultVectors = new ArrayList<>();
        for (OpenAIEmbeddingData item : sortedData) {
            List<Double> vector = item.getEmbedding();
            if (vector == null || vector.isEmpty()) {
                throw new IllegalStateException("Received empty embedding vector from OpenAI API");
            }
            if (vector.size() != dimension) {
                throw new IllegalStateException(String.format(
                        "Returned embedding vector dimension (%d) does not match configured dimension (%d)",
                        vector.size(), dimension
                ));
            }
            resultVectors.add(vector);
        }

        return resultVectors;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    public String getModel() {
        return model;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public boolean isApiKeyConfigured() {
        return !apiKey.isEmpty();
    }

    // Inner DTO classes for parsing OpenAI response JSON
    public static class OpenAIEmbeddingResponse {
        private String object;
        private List<OpenAIEmbeddingData> data;
        private String model;

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public List<OpenAIEmbeddingData> getData() {
            return data;
        }

        public void setData(List<OpenAIEmbeddingData> data) {
            this.data = data;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }
    }

    public static class OpenAIEmbeddingData {
        private String object;
        private int index;
        private List<Double> embedding;

        public String getObject() {
            return object;
        }

        public void setObject(String object) {
            this.object = object;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
}
