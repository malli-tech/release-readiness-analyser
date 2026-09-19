package com.aireadiness.knowledge.service;

import com.aireadiness.knowledge.dto.KnowledgeRetrievalRequest;
import com.aireadiness.knowledge.dto.KnowledgeRetrievalResponse;
import com.aireadiness.knowledge.dto.KnowledgeRetrievalResult;
import com.aireadiness.knowledge.model.KnowledgeChunk;
import com.aireadiness.knowledge.repository.KnowledgeChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KnowledgeRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRetrievalService.class);

    private final EmbeddingService embeddingService;
    private final KnowledgeChunkRepository chunkRepository;
    private final MongoTemplate mongoTemplate;
    private final int defaultTopK;
    private final int maxTopK;

    public KnowledgeRetrievalService(
            EmbeddingService embeddingService,
            KnowledgeChunkRepository chunkRepository,
            MongoTemplate mongoTemplate,
            @Value("${rag.retrieval.default-top-k:5}") int defaultTopK,
            @Value("${rag.retrieval.max-top-k:20}") int maxTopK
    ) {
        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
        this.mongoTemplate = mongoTemplate;
        this.defaultTopK = Math.max(1, defaultTopK);
        this.maxTopK = Math.max(this.defaultTopK, maxTopK);
    }

    public KnowledgeRetrievalResponse retrieve(KnowledgeRetrievalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("KnowledgeRetrievalRequest must not be null");
        }
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new IllegalArgumentException("Retrieval query must not be blank");
        }

        int requestedTopK = request.getTopK() != null ? request.getTopK() : defaultTopK;
        if (requestedTopK < 1) {
            throw new IllegalArgumentException("topK must be at least 1");
        }
        int effectiveTopK = Math.min(requestedTopK, maxTopK);

        String trimmedQuery = request.getQuery().trim();
        List<Double> queryVector = embeddingService.embed(trimmedQuery);

        if (queryVector == null || queryVector.isEmpty()) {
            throw new IllegalStateException("Failed to generate embedding vector for retrieval query");
        }

        // Build filtered query to fetch candidate chunks from MongoDB
        Query mongoQuery = new Query();
        if (request.getCategory() != null) {
            mongoQuery.addCriteria(Criteria.where("category").is(request.getCategory()));
        }
        if (request.getTechnology() != null && !request.getTechnology().trim().isEmpty()) {
            mongoQuery.addCriteria(Criteria.where("technology").regex(".*" + request.getTechnology().trim() + ".*", "i"));
        }
        if (request.getVersion() != null && !request.getVersion().trim().isEmpty()) {
            mongoQuery.addCriteria(Criteria.where("version").is(request.getVersion().trim()));
        }

        List<KnowledgeChunk> candidateChunks = mongoTemplate.find(mongoQuery, KnowledgeChunk.class);

        List<KnowledgeRetrievalResult> scoredResults = new ArrayList<>();
        for (KnowledgeChunk chunk : candidateChunks) {
            double score = calculateCosineSimilarity(queryVector, chunk.getEmbedding());
            // Round score to 4 decimal places for presentation
            double roundedScore = BigDecimal.valueOf(score)
                    .setScale(4, RoundingMode.HALF_UP)
                    .doubleValue();
            scoredResults.add(KnowledgeRetrievalResult.fromChunk(chunk, roundedScore));
        }

        // Deterministic sorting: similarityScore DESC -> documentId ASC -> chunkIndex ASC
        scoredResults.sort(Comparator
                .comparing(KnowledgeRetrievalResult::getSimilarityScore, Comparator.reverseOrder())
                .thenComparing(KnowledgeRetrievalResult::getDocumentId, Comparator.nullsLast(String::compareTo))
                .thenComparingInt(KnowledgeRetrievalResult::getChunkIndex));

        List<KnowledgeRetrievalResult> topResults = scoredResults.stream()
                .limit(effectiveTopK)
                .collect(Collectors.toList());

        return new KnowledgeRetrievalResponse(trimmedQuery, effectiveTopK, topResults);
    }

    /**
     * Calculates Cosine Similarity between query vector u and chunk embedding vector v.
     *
     * Cosine Similarity = (u . v) / (||u|| * ||v||)
     */
    public double calculateCosineSimilarity(List<Double> u, List<Double> v) {
        if (u == null || v == null || u.isEmpty() || v.isEmpty() || u.size() != v.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normU = 0.0;
        double normV = 0.0;

        for (int i = 0; i < u.size(); i++) {
            double valU = u.get(i);
            double valV = v.get(i);
            dotProduct += valU * valV;
            normU += valU * valU;
            normV += valV * valV;
        }

        if (normU == 0.0 || normV == 0.0) {
            return 0.0;
        }

        double similarity = dotProduct / (Math.sqrt(normU) * Math.sqrt(normV));
        // Clamp to [-1.0, 1.0] to handle floating-point precision inaccuracies
        return Math.max(-1.0, Math.min(1.0, similarity));
    }
}
