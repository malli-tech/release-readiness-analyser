package com.aireadiness.aireview;

import com.aireadiness.aireview.dto.AIReviewPromptContext;
import com.aireadiness.aireview.dto.AIReviewResult;
import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.model.AIReviewConfidence;
import com.aireadiness.aireview.model.AIReviewStatus;
import com.aireadiness.aireview.repository.AIReviewRepository;
import com.aireadiness.aireview.service.AIReviewService;
import com.aireadiness.aireview.service.LLMService;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.knowledge.dto.KnowledgeRetrievalRequest;
import com.aireadiness.knowledge.dto.KnowledgeRetrievalResponse;
import com.aireadiness.knowledge.service.KnowledgeRetrievalService;
import com.aireadiness.knowledge.util.RetrievalQueryBuilder;
import com.aireadiness.model.Analysis;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;
import com.aireadiness.repository.AnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AIReviewServiceTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private AIReviewRepository aiReviewRepository;

    @Mock
    private LLMService llmService;

    @Mock
    private KnowledgeRetrievalService retrievalService;

    private RetrievalQueryBuilder queryBuilder;
    private AIReviewService aiReviewService;

    @BeforeEach
    public void setUp() {
        queryBuilder = new RetrievalQueryBuilder();
        aiReviewService = new AIReviewService(
                analysisRepository,
                aiReviewRepository,
                llmService,
                retrievalService,
                queryBuilder
        );
    }

    private Analysis createSampleAnalysis() {
        Analysis analysis = new Analysis();
        analysis.setId("ans-100");
        analysis.setProjectId("proj-1");
        analysis.setReleaseId("rel-1");
        analysis.setUserId("user-100");
        analysis.setStatus("COMPLETED");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        profile.setFramework("Spring Boot");
        analysis.setProjectProfile(profile);

        Finding finding = new Finding();
        finding.setId("find-1");
        finding.setCategory("SECURITY");
        finding.setRuleId("SECURITY_SQL_INJECTION");
        finding.setSeverity("HIGH");
        finding.setTitle("SQL Injection");
        finding.setDescription("Raw query concatenation");
        finding.setFilePath("UserRepository.java");
        finding.setLineNumber(10);
        analysis.setFindings(List.of(finding));

        return analysis;
    }

    @Test
    @DisplayName("1. Generates and persists COMPLETED AI review successfully")
    public void testGenerateReviewsSuccess() {
        Analysis analysis = createSampleAnalysis();

        when(analysisRepository.findById("ans-100")).thenReturn(Optional.of(analysis));
        when(aiReviewRepository.findByAnalysisIdAndFindingId("ans-100", "find-1")).thenReturn(Optional.empty());
        when(retrievalService.retrieve(any(KnowledgeRetrievalRequest.class)))
                .thenReturn(new KnowledgeRetrievalResponse("query", 3, Collections.emptyList()));

        AIReviewResult mockLlmResult = new AIReviewResult(
                "SQL Injection risk detected.",
                "High security risk.",
                List.of("Check line 10"),
                "Use prepared statements.",
                AIReviewConfidence.HIGH
        );
        when(llmService.generateAIReview(any(AIReviewPromptContext.class))).thenReturn(mockLlmResult);

        when(llmService.getModel()).thenReturn("gpt-4o-mini");
        when(aiReviewRepository.save(any(AIReview.class))).thenAnswer(i -> i.getArgument(0));

        List<AIReview> reviews = aiReviewService.generateReviewsForAnalysis("ans-100", "user-100");

        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        AIReview review = reviews.get(0);

        assertEquals(AIReviewStatus.COMPLETED, review.getStatus());
        assertEquals("SQL Injection risk detected.", review.getSummary());
        assertEquals(AIReviewConfidence.HIGH, review.getConfidence());
        assertEquals("gpt-4o-mini", review.getModel());
    }

    @Test
    @DisplayName("2. Marks review status as FAILED with sanitized message when LLM fails without affecting analysis")
    public void testGenerateReviewsLLMFailureIsolation() {
        Analysis analysis = createSampleAnalysis();

        when(analysisRepository.findById("ans-100")).thenReturn(Optional.of(analysis));
        when(aiReviewRepository.findByAnalysisIdAndFindingId("ans-100", "find-1")).thenReturn(Optional.empty());
        when(retrievalService.retrieve(any(KnowledgeRetrievalRequest.class)))
                .thenReturn(new KnowledgeRetrievalResponse("query", 3, Collections.emptyList()));

        when(llmService.generateAIReview(any(AIReviewPromptContext.class)))
                .thenThrow(new IllegalStateException("OpenAI API key is missing"));
        when(llmService.getModel()).thenReturn("gpt-4o-mini");

        when(aiReviewRepository.save(any(AIReview.class))).thenAnswer(i -> i.getArgument(0));

        List<AIReview> reviews = aiReviewService.generateReviewsForAnalysis("ans-100", "user-100");

        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        AIReview review = reviews.get(0);

        assertEquals(AIReviewStatus.FAILED, review.getStatus());
        assertEquals("gpt-4o-mini", review.getModel());
        assertTrue(review.getErrorMessage().contains("AI_REVIEW_CONFIGURATION_ERROR"));
        assertFalse(review.getErrorMessage().contains("secret"));

        // Verify analysis remains completed and findings unchanged
        assertEquals("COMPLETED", analysis.getStatus());
        assertEquals(1, analysis.getFindings().size());
    }

    @Test
    @DisplayName("3. Continues AI review generation cleanly when RAG retrieval fails")
    public void testRAGFailureDoesNotFailAIReview() {
        Analysis analysis = createSampleAnalysis();

        when(analysisRepository.findById("ans-100")).thenReturn(Optional.of(analysis));
        when(aiReviewRepository.findByAnalysisIdAndFindingId("ans-100", "find-1")).thenReturn(Optional.empty());
        when(retrievalService.retrieve(any(KnowledgeRetrievalRequest.class)))
                .thenThrow(new RuntimeException("Vector database connection failed"));

        AIReviewResult mockLlmResult = new AIReviewResult(
                "SQL Injection risk.",
                "High impact.",
                List.of("Check query"),
                "Fix query.",
                AIReviewConfidence.HIGH
        );
        when(llmService.generateAIReview(any(AIReviewPromptContext.class))).thenReturn(mockLlmResult);
        when(llmService.getModel()).thenReturn("gpt-4o-mini");
        when(aiReviewRepository.save(any(AIReview.class))).thenAnswer(i -> i.getArgument(0));

        List<AIReview> reviews = aiReviewService.generateReviewsForAnalysis("ans-100", "user-100");

        assertNotNull(reviews);
        assertEquals(1, reviews.size());
        assertEquals(AIReviewStatus.COMPLETED, reviews.get(0).getStatus());
    }

    @Test
    @DisplayName("4. Throws ResourceNotFoundException for user ownership mismatch")
    public void testUnauthorizedUserAccess() {
        Analysis analysis = createSampleAnalysis();
        when(analysisRepository.findById("ans-100")).thenReturn(Optional.of(analysis));

        assertThrows(ResourceNotFoundException.class, () ->
                aiReviewService.generateReviewsForAnalysis("ans-100", "wrong-user"));
    }
}
