package com.aireadiness.aireview.service;

import com.aireadiness.aireview.dto.AIReviewPromptContext;
import com.aireadiness.aireview.dto.AIReviewResult;
import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.model.AIReviewStatus;
import com.aireadiness.aireview.repository.AIReviewRepository;
import com.aireadiness.aireview.util.AIReviewPromptBuilder;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.knowledge.dto.KnowledgeRetrievalRequest;
import com.aireadiness.knowledge.dto.KnowledgeRetrievalResponse;
import com.aireadiness.knowledge.dto.KnowledgeRetrievalResult;
import com.aireadiness.knowledge.model.KnowledgeCategory;
import com.aireadiness.knowledge.service.KnowledgeRetrievalService;
import com.aireadiness.knowledge.util.RetrievalQueryBuilder;
import com.aireadiness.model.Analysis;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;
import com.aireadiness.repository.AnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class AIReviewService {

    private static final Logger log = LoggerFactory.getLogger(AIReviewService.class);

    private final AnalysisRepository analysisRepository;
    private final AIReviewRepository aiReviewRepository;
    private final LLMService llmService;
    private final KnowledgeRetrievalService retrievalService;
    private final RetrievalQueryBuilder queryBuilder;

    public AIReviewService(
            AnalysisRepository analysisRepository,
            AIReviewRepository aiReviewRepository,
            LLMService llmService,
            KnowledgeRetrievalService retrievalService,
            RetrievalQueryBuilder queryBuilder
    ) {
        this.analysisRepository = analysisRepository;
        this.aiReviewRepository = aiReviewRepository;
        this.llmService = llmService;
        this.retrievalService = retrievalService;
        this.queryBuilder = queryBuilder;
    }

    public List<AIReview> generateReviewsForAnalysis(String analysisId, String userId) {
        Analysis analysis = getAnalysisWithOwnershipCheck(analysisId, userId);
        List<Finding> findings = analysis.getFindings();

        if (findings == null || findings.isEmpty()) {
            return Collections.emptyList();
        }

        List<AIReview> reviews = new ArrayList<>();
        for (Finding finding : findings) {
            AIReview review = generateReviewForFindingInternal(analysis, finding, false);
            reviews.add(review);
        }

        return reviews;
    }

    public AIReview generateReviewForFinding(String analysisId, String findingId, String userId) {
        Analysis analysis = getAnalysisWithOwnershipCheck(analysisId, userId);
        Finding targetFinding = analysis.getFindings().stream()
                .filter(f -> f.getId() != null && f.getId().equals(findingId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Finding not found with id: " + findingId + " in analysis: " + analysisId));

        return generateReviewForFindingInternal(analysis, targetFinding, true);
    }

    public List<AIReview> getReviewsForAnalysis(String analysisId, String userId) {
        getAnalysisWithOwnershipCheck(analysisId, userId);
        return aiReviewRepository.findByAnalysisId(analysisId);
    }

    public AIReview getReviewForFinding(String analysisId, String findingId, String userId) {
        getAnalysisWithOwnershipCheck(analysisId, userId);
        return aiReviewRepository.findByAnalysisIdAndFindingId(analysisId, findingId)
                .orElseThrow(() -> new ResourceNotFoundException("AI review not found for analysis: " + analysisId + ", finding: " + findingId));
    }

    private AIReview generateReviewForFindingInternal(Analysis analysis, Finding finding, boolean forceRegenerate) {
        String analysisId = analysis.getId();
        String findingId = finding.getId();

        Optional<AIReview> existingOpt = aiReviewRepository.findByAnalysisIdAndFindingId(analysisId, findingId);
        if (existingOpt.isPresent() && !forceRegenerate) {
            AIReview existing = existingOpt.get();
            if (existing.getStatus() == AIReviewStatus.COMPLETED) {
                return existing;
            }
        }

        AIReview review = existingOpt.orElseGet(() -> {
            AIReview r = new AIReview();
            r.setAnalysisId(analysisId);
            r.setFindingId(findingId);
            r.setReleaseId(analysis.getReleaseId());
            r.setUserId(analysis.getUserId());
            r.setRuleId(finding.getRuleId());
            r.setCategory(finding.getCategory());
            return r;
        });

        review.setStatus(AIReviewStatus.IN_PROGRESS);
        review.setModel(llmService.getModel());
        review.setUpdatedAt(Instant.now());
        review = aiReviewRepository.save(review);

        try {
            // 1. Retrieve RAG Knowledge Chunks
            List<KnowledgeRetrievalResult> ragChunks = retrieveRAGKnowledge(finding, analysis.getProjectProfile());

            // 2. Assemble Prompt Context
            String evidenceSnippet = finding.getEvidence() != null ? finding.getEvidence() : "";
            AIReviewPromptContext promptContext = new AIReviewPromptContext(
                    analysis.getProjectProfile(),
                    finding,
                    evidenceSnippet,
                    ragChunks
            );

            // 3. Generate Structured AI Review via LLM
            AIReviewResult llmResult = llmService.generateAIReview(promptContext);

            // 4. Update Review Record
            review.setStatus(AIReviewStatus.COMPLETED);
            review.setModel(llmService.getModel());
            review.setSummary(llmResult.getSummary());
            review.setWhyItMatters(llmResult.getWhyItMatters());
            review.setWhatToReview(llmResult.getWhatToReview());
            review.setSuggestedFix(llmResult.getSuggestedFix());
            review.setConfidence(llmResult.getConfidence());
            review.setErrorMessage(null);
            review.setUpdatedAt(Instant.now());

            return aiReviewRepository.save(review);
        } catch (Exception e) {
            String safeErrorCategory = categorizeFailure(e);
            log.warn("AI review generation failed for finding {} in analysis {}: {}", findingId, analysisId, safeErrorCategory);
            review.setStatus(AIReviewStatus.FAILED);
            review.setModel(llmService.getModel());
            review.setErrorMessage(safeErrorCategory);
            review.setUpdatedAt(Instant.now());
            return aiReviewRepository.save(review);
        }
    }

    private List<KnowledgeRetrievalResult> retrieveRAGKnowledge(Finding finding, ProjectProfile profile) {
        try {
            KnowledgeCategory category = null;
            if (finding.getCategory() != null) {
                try {
                    category = KnowledgeCategory.valueOf(finding.getCategory());
                } catch (Exception ignored) {}
            }

            String tech = profile != null ? profile.getPrimaryLanguage() : null;
            String framework = profile != null ? profile.getFramework() : null;
            String queryString = queryBuilder.buildQuery(
                    category,
                    finding.getRuleId(),
                    finding.getSeverity(),
                    finding.getTitle(),
                    finding.getDescription(),
                    tech,
                    framework
            );

            KnowledgeRetrievalRequest req = new KnowledgeRetrievalRequest(queryString, category, tech, null, 3);
            KnowledgeRetrievalResponse response = retrievalService.retrieve(req);
            return response != null ? response.getResults() : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to retrieve RAG knowledge chunks for AI review prompt: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String categorizeFailure(Throwable e) {
        if (e == null) {
            return "AI_REVIEW_UNKNOWN_ERROR: An unexpected error occurred while generating AI review.";
        }
        String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        if (e instanceof IllegalStateException && msg.contains("api key is missing")) {
            return "AI_REVIEW_CONFIGURATION_ERROR: OpenAI API key or model configuration is missing.";
        }
        if (msg.contains("401") || msg.contains("403") || msg.contains("unauthorized") || msg.contains("authentication")) {
            return "AI_REVIEW_AUTHENTICATION_ERROR: Authentication or authorization failed with AI provider.";
        }
        if (msg.contains("429") || msg.contains("rate limit") || msg.contains("too many requests")) {
            return "AI_REVIEW_RATE_LIMITED: AI review provider rate limit exceeded.";
        }
        if (msg.contains("timeout") || msg.contains("timed out") || e instanceof java.net.SocketTimeoutException) {
            return "AI_REVIEW_TIMEOUT: Request to AI provider timed out.";
        }
        if (msg.contains("invalid") || msg.contains("malformed") || msg.contains("parse") || msg.contains("json") || msg.contains("schema")) {
            return "AI_REVIEW_INVALID_RESPONSE: AI provider returned invalid or malformed review output.";
        }
        if (msg.contains("500") || msg.contains("502") || msg.contains("503") || msg.contains("504") || msg.contains("provider")) {
            return "AI_REVIEW_PROVIDER_ERROR: AI review provider encountered an internal error.";
        }
        return "AI_REVIEW_UNKNOWN_ERROR: An unexpected error occurred while generating AI review.";
    }

    private Analysis getAnalysisWithOwnershipCheck(String analysisId, String userId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + analysisId));

        if (!analysis.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Analysis not found with id: " + analysisId);
        }

        return analysis;
    }
}
