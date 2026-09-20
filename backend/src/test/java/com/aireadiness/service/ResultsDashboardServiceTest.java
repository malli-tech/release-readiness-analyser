package com.aireadiness.service;

import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.model.AIReviewConfidence;
import com.aireadiness.aireview.model.AIReviewStatus;
import com.aireadiness.aireview.repository.AIReviewRepository;
import com.aireadiness.dto.results.ReleaseResultsResponse;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.*;
import com.aireadiness.recommendation.model.Recommendation;
import com.aireadiness.recommendation.model.RecommendationPriority;
import com.aireadiness.recommendation.model.RecommendationStatus;
import com.aireadiness.recommendation.repository.RecommendationRepository;
import com.aireadiness.repository.AnalysisRepository;
import com.aireadiness.repository.ProjectRepository;
import com.aireadiness.repository.ReleaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ResultsDashboardServiceTest {

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private AIReviewRepository aiReviewRepository;

    @Mock
    private RecommendationRepository recommendationRepository;

    private ResultsDashboardService resultsDashboardService;

    @BeforeEach
    public void setUp() {
        resultsDashboardService = new ResultsDashboardService(
                releaseRepository,
                projectRepository,
                analysisRepository,
                aiReviewRepository,
                recommendationRepository
        );
    }

    @Test
    @DisplayName("1. Successfully retrieves aggregated results for an analyzed release without recalculation")
    public void testSuccessfulResultsRetrievalForAnalyzedRelease() {
        String releaseId = "rel-100";
        String userId = "user-1";
        String projectId = "proj-1";

        Release release = new Release(projectId, userId, "v1.0.0", "Release 1.0", "Description");
        release.setId(releaseId);
        release.setStatus("ANALYZED");

        Project project = new Project(userId, "Test Project", "Desc", "WEB", "Java", "Spring Boot", "https://github.com/repo");
        project.setId(projectId);

        Analysis analysis = new Analysis(projectId, releaseId, userId, 1, "COMPLETED");
        analysis.setId("ans-100");

        UnifiedAnalysisSummary uSummary = new UnifiedAnalysisSummary();
        uSummary.setCompleteness("COMPLETE");
        uSummary.setTotalFindings(5);
        uSummary.setHighFindings(2);
        uSummary.setMediumFindings(3);
        uSummary.setAffectedFiles(3);
        uSummary.setCompletedAnalyzers(List.of("CODE_QUALITY", "SECURITY"));
        analysis.setUnifiedAnalysisSummary(uSummary);

        RiskSummary riskSummary = new RiskSummary();
        riskSummary.setOverallRiskLevel(RiskLevel.MEDIUM);
        riskSummary.setWeightedRiskPoints(BigDecimal.valueOf(12.5));
        riskSummary.setCalculationVersion("v1.0");
        analysis.setRiskSummary(riskSummary);

        ReadinessScore readinessScore = new ReadinessScore();
        readinessScore.setReadinessScore(BigDecimal.valueOf(85.0));
        readinessScore.setReadinessLevel(ReadinessLevel.GOOD);
        readinessScore.setConfidence(ReadinessConfidence.HIGH);
        analysis.setReadinessScore(readinessScore);

        AIReview review = new AIReview();
        review.setAnalysisId("ans-100");
        review.setStatus(AIReviewStatus.COMPLETED);
        review.setModel("gpt-4o");
        review.setConfidence(AIReviewConfidence.HIGH);
        review.setSummary("Code looks good");

        Recommendation rec = new Recommendation();
        rec.setAnalysisId("ans-100");
        rec.setPriority(RecommendationPriority.HIGH);
        rec.setStatus(RecommendationStatus.OPEN);
        rec.setCategory("SECURITY");

        when(releaseRepository.findByIdAndUserId(releaseId, userId)).thenReturn(Optional.of(release));
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(releaseId, userId)).thenReturn(Optional.of(analysis));
        when(aiReviewRepository.findByAnalysisId("ans-100")).thenReturn(List.of(review));
        when(recommendationRepository.findByAnalysisId("ans-100")).thenReturn(List.of(rec));

        ReleaseResultsResponse response = resultsDashboardService.getReleaseResults(releaseId, userId);

        assertNotNull(response);
        assertNotNull(response.getReleaseInfo());
        assertEquals("rel-100", response.getReleaseInfo().getReleaseId());
        assertEquals("Test Project", response.getReleaseInfo().getProjectName());

        assertNotNull(response.getAnalysisSummary());
        assertEquals("ans-100", response.getAnalysisSummary().getAnalysisId());
        assertEquals("COMPLETE", response.getAnalysisSummary().getCompleteness());
        assertEquals(5, response.getAnalysisSummary().getTotalFindings());

        assertNotNull(response.getRiskSummary());
        assertEquals(RiskLevel.MEDIUM, response.getRiskSummary().getOverallRiskLevel());
        assertEquals(BigDecimal.valueOf(12.5), response.getRiskSummary().getWeightedRiskPoints());

        assertNotNull(response.getReadinessScore());
        assertEquals(BigDecimal.valueOf(85.0), response.getReadinessScore().getReadinessScore());
        assertEquals(ReadinessLevel.GOOD, response.getReadinessScore().getReadinessLevel());

        assertNotNull(response.getAiReviewSummary());
        assertEquals(1, response.getAiReviewSummary().getTotalReviews());
        assertEquals("COMPLETED", response.getAiReviewSummary().getOverallStatus());

        assertNotNull(response.getRecommendationSummary());
        assertEquals(1, response.getRecommendationSummary().getTotalRecommendations());
        assertEquals(1, response.getRecommendationSummary().getCountsByPriority().get("HIGH"));
    }

    @Test
    @DisplayName("2. Rejects request when user does not own release or release is missing")
    public void testOwnershipRejectionAndMissingRelease() {
        when(releaseRepository.findByIdAndUserId("rel-999", "other-user")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                resultsDashboardService.getReleaseResults("rel-999", "other-user")
        );
    }

    @Test
    @DisplayName("3. Returns response with release info and null sections when release has no analysis")
    public void testReleaseWithoutAnalysis() {
        String releaseId = "rel-200";
        String userId = "user-1";
        Release release = new Release("proj-1", userId, "v2.0.0", "Release 2.0", "No analysis yet");
        release.setId(releaseId);

        when(releaseRepository.findByIdAndUserId(releaseId, userId)).thenReturn(Optional.of(release));
        when(projectRepository.findByIdAndUserId("proj-1", userId)).thenReturn(Optional.empty());
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(releaseId, userId)).thenReturn(Optional.empty());

        ReleaseResultsResponse response = resultsDashboardService.getReleaseResults(releaseId, userId);

        assertNotNull(response);
        assertNotNull(response.getReleaseInfo());
        assertEquals("rel-200", response.getReleaseInfo().getReleaseId());
        assertNull(response.getAnalysisSummary());
        assertNull(response.getRiskSummary());
        assertNull(response.getReadinessScore());
        assertNull(response.getAiReviewSummary());
        assertNull(response.getRecommendationSummary());
        assertNull(response.getFindingSummary());
    }

    @Test
    @DisplayName("4. Handles partial analysis with findings fallback when UnifiedAnalysisSummary is absent")
    public void testPartialAnalysisCompleteness() {
        String releaseId = "rel-300";
        String userId = "user-1";
        Release release = new Release("proj-1", userId, "v3.0.0", "Release 3.0", "Partial");
        release.setId(releaseId);

        Analysis analysis = new Analysis("proj-1", releaseId, userId, 1, "PARTIAL");
        analysis.setId("ans-300");

        Finding f1 = new Finding();
        f1.setSeverity("HIGH");
        f1.setCategory("CODE_QUALITY");
        f1.setFilePath("src/Main.java");

        Finding f2 = new Finding();
        f2.setSeverity("LOW");
        f2.setCategory("TESTING");
        f2.setFilePath("src/Main.java");

        analysis.setFindings(List.of(f1, f2));

        when(releaseRepository.findByIdAndUserId(releaseId, userId)).thenReturn(Optional.of(release));
        when(projectRepository.findByIdAndUserId("proj-1", userId)).thenReturn(Optional.empty());
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(releaseId, userId)).thenReturn(Optional.of(analysis));
        when(aiReviewRepository.findByAnalysisId("ans-300")).thenReturn(Collections.emptyList());
        when(recommendationRepository.findByAnalysisId("ans-300")).thenReturn(Collections.emptyList());

        ReleaseResultsResponse response = resultsDashboardService.getReleaseResults(releaseId, userId);

        assertNotNull(response);
        assertNotNull(response.getAnalysisSummary());
        assertEquals("PARTIAL", response.getAnalysisSummary().getCompleteness());
        assertEquals(2, response.getAnalysisSummary().getTotalFindings());
        assertEquals(1, response.getAnalysisSummary().getAffectedFiles());
    }

    @Test
    @DisplayName("5. Propagates sanitized failure category for failed AI review")
    public void testFailedAIReviewPropagation() {
        String releaseId = "rel-400";
        String userId = "user-1";
        Release release = new Release("proj-1", userId, "v4.0.0", "Release 4.0", "Desc");
        release.setId(releaseId);

        Analysis analysis = new Analysis("proj-1", releaseId, userId, 1, "COMPLETED");
        analysis.setId("ans-400");

        AIReview failedReview = new AIReview();
        failedReview.setAnalysisId("ans-400");
        failedReview.setStatus(AIReviewStatus.FAILED);
        failedReview.setErrorMessage("AI_REVIEW_PROVIDER_ERROR");

        when(releaseRepository.findByIdAndUserId(releaseId, userId)).thenReturn(Optional.of(release));
        when(projectRepository.findByIdAndUserId("proj-1", userId)).thenReturn(Optional.empty());
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(releaseId, userId)).thenReturn(Optional.of(analysis));
        when(aiReviewRepository.findByAnalysisId("ans-400")).thenReturn(List.of(failedReview));
        when(recommendationRepository.findByAnalysisId("ans-400")).thenReturn(Collections.emptyList());

        ReleaseResultsResponse response = resultsDashboardService.getReleaseResults(releaseId, userId);

        assertNotNull(response);
        assertNotNull(response.getAiReviewSummary());
        assertEquals(1, response.getAiReviewSummary().getFailedReviews());
        assertEquals("FAILED", response.getAiReviewSummary().getOverallStatus());
        assertEquals("AI_REVIEW_PROVIDER_ERROR", response.getAiReviewSummary().getFailureCategory());
    }

    @Test
    @DisplayName("6. Handles zero recommendations gracefully")
    public void testZeroRecommendationsSummary() {
        String releaseId = "rel-500";
        String userId = "user-1";
        Release release = new Release("proj-1", userId, "v5.0.0", "Release 5.0", "Desc");
        release.setId(releaseId);

        Analysis analysis = new Analysis("proj-1", releaseId, userId, 1, "COMPLETED");
        analysis.setId("ans-500");

        when(releaseRepository.findByIdAndUserId(releaseId, userId)).thenReturn(Optional.of(release));
        when(projectRepository.findByIdAndUserId("proj-1", userId)).thenReturn(Optional.empty());
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(releaseId, userId)).thenReturn(Optional.of(analysis));
        when(aiReviewRepository.findByAnalysisId("ans-500")).thenReturn(Collections.emptyList());
        when(recommendationRepository.findByAnalysisId("ans-500")).thenReturn(Collections.emptyList());

        ReleaseResultsResponse response = resultsDashboardService.getReleaseResults(releaseId, userId);

        assertNotNull(response);
        assertNotNull(response.getRecommendationSummary());
        assertEquals(0, response.getRecommendationSummary().getTotalRecommendations());
        assertTrue(response.getRecommendationSummary().getCountsByPriority().isEmpty());
    }
}
