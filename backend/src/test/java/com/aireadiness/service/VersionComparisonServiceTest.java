package com.aireadiness.service;

import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.model.AIReviewConfidence;
import com.aireadiness.aireview.model.AIReviewStatus;
import com.aireadiness.aireview.repository.AIReviewRepository;
import com.aireadiness.dto.comparison.VersionComparisonResponse;
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
public class VersionComparisonServiceTest {

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

    private VersionComparisonService versionComparisonService;

    @BeforeEach
    public void setUp() {
        versionComparisonService = new VersionComparisonService(
                releaseRepository,
                projectRepository,
                analysisRepository,
                aiReviewRepository,
                recommendationRepository
        );
    }

    @Test
    @DisplayName("1. Successful version comparison between two analyzed releases of the same project")
    public void testSuccessfulVersionComparison() {
        String projectId = "proj-1";
        String userId = "user-1";
        String baseRelId = "rel-1";
        String targetRelId = "rel-2";

        Release baseRel = new Release(projectId, userId, "1.0.0", "Release 1.0", "Base");
        baseRel.setId(baseRelId);

        Release targetRel = new Release(projectId, userId, "1.1.0", "Release 1.1", "Target");
        targetRel.setId(targetRelId);

        Project project = new Project(userId, "Project Alpha", "Desc", "WEB", "Java", "Spring", "repo");
        project.setId(projectId);

        Analysis baseAns = new Analysis(projectId, baseRelId, userId, 1, "COMPLETED");
        baseAns.setId("ans-1");

        RiskSummary baseRisk = new RiskSummary();
        baseRisk.setOverallRiskLevel(RiskLevel.HIGH);
        baseRisk.setWeightedRiskPoints(BigDecimal.valueOf(25.0));
        baseAns.setRiskSummary(baseRisk);

        ReadinessScore baseReadiness = new ReadinessScore();
        baseReadiness.setReadinessScore(BigDecimal.valueOf(70.0));
        baseReadiness.setReadinessLevel(ReadinessLevel.POOR);
        baseAns.setReadinessScore(baseReadiness);

        Finding fBaseUnchanged = new Finding();
        fBaseUnchanged.setId("f-1");
        fBaseUnchanged.setRuleId("SEC-01");
        fBaseUnchanged.setCategory("SECURITY");
        fBaseUnchanged.setSeverity("HIGH");
        fBaseUnchanged.setFilePath("src/Auth.java");
        fBaseUnchanged.setLineNumber(10);

        Finding fBaseResolved = new Finding();
        fBaseResolved.setId("f-2");
        fBaseResolved.setRuleId("QUAL-01");
        fBaseResolved.setCategory("CODE_QUALITY");
        fBaseResolved.setSeverity("MEDIUM");
        fBaseResolved.setFilePath("src/Util.java");
        fBaseResolved.setLineNumber(20);

        baseAns.setFindings(List.of(fBaseUnchanged, fBaseResolved));

        Analysis targetAns = new Analysis(projectId, targetRelId, userId, 1, "COMPLETED");
        targetAns.setId("ans-2");

        RiskSummary targetRisk = new RiskSummary();
        targetRisk.setOverallRiskLevel(RiskLevel.LOW);
        targetRisk.setWeightedRiskPoints(BigDecimal.valueOf(5.0));
        targetAns.setRiskSummary(targetRisk);

        ReadinessScore targetReadiness = new ReadinessScore();
        targetReadiness.setReadinessScore(BigDecimal.valueOf(90.0));
        targetReadiness.setReadinessLevel(ReadinessLevel.GOOD);
        targetAns.setReadinessScore(targetReadiness);

        Finding fTargetUnchanged = new Finding();
        fTargetUnchanged.setId("f-3");
        fTargetUnchanged.setRuleId("SEC-01");
        fTargetUnchanged.setCategory("SECURITY");
        fTargetUnchanged.setSeverity("HIGH");
        fTargetUnchanged.setFilePath("src/Auth.java");
        fTargetUnchanged.setLineNumber(10);

        Finding fTargetNew = new Finding();
        fTargetNew.setId("f-4");
        fTargetNew.setRuleId("PERF-01");
        fTargetNew.setCategory("PERFORMANCE");
        fTargetNew.setSeverity("LOW");
        fTargetNew.setFilePath("src/DB.java");
        fTargetNew.setLineNumber(30);

        targetAns.setFindings(List.of(fTargetUnchanged, fTargetNew));

        when(releaseRepository.findByIdAndUserId(baseRelId, userId)).thenReturn(Optional.of(baseRel));
        when(releaseRepository.findByIdAndUserId(targetRelId, userId)).thenReturn(Optional.of(targetRel));
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.of(project));
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(baseRelId, userId)).thenReturn(Optional.of(baseAns));
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(targetRelId, userId)).thenReturn(Optional.of(targetAns));

        VersionComparisonResponse response = versionComparisonService.compareReleases(projectId, baseRelId, targetRelId, userId);

        assertNotNull(response);
        assertEquals("1.0.0", response.getBaseRelease().getVersion());
        assertEquals("1.1.0", response.getTargetRelease().getVersion());

        // Risk points delta: 5.0 - 25.0 = -20.0
        assertEquals(BigDecimal.valueOf(-20.0), response.getRiskComparison().getDeltaWeightedRiskPoints());
        assertEquals("HIGH", response.getRiskComparison().getBaseRiskLevel());
        assertEquals("LOW", response.getRiskComparison().getTargetRiskLevel());

        // Readiness score delta: 90.0 - 70.0 = 20.0
        assertEquals(BigDecimal.valueOf(20.0), response.getReadinessComparison().getScoreDelta());

        // Finding changes: 1 NEW (PERF-01), 1 RESOLVED (QUAL-01), 1 UNCHANGED (SEC-01)
        assertNotNull(response.getFindingChanges());
        assertEquals(1, response.getFindingChanges().getNewCount());
        assertEquals(1, response.getFindingChanges().getResolvedCount());
        assertEquals(1, response.getFindingChanges().getUnchangedCount());
    }

    @Test
    @DisplayName("2. Rejects comparison when releases belong to different projects")
    public void testDifferentProjectsRejection() {
        String userId = "user-1";
        Release baseRel = new Release("proj-1", userId, "1.0.0", "Release 1", "Desc");
        baseRel.setId("rel-1");

        Release targetRel = new Release("proj-2", userId, "1.1.0", "Release 2", "Desc");
        targetRel.setId("rel-2");

        when(releaseRepository.findByIdAndUserId("rel-1", userId)).thenReturn(Optional.of(baseRel));
        when(releaseRepository.findByIdAndUserId("rel-2", userId)).thenReturn(Optional.of(targetRel));

        assertThrows(IllegalArgumentException.class, () ->
                versionComparisonService.compareReleases("proj-1", "rel-1", "rel-2", userId)
        );
    }

    @Test
    @DisplayName("3. Rejects comparison when base and target release IDs are identical")
    public void testSameReleaseRejection() {
        String userId = "user-1";
        assertThrows(IllegalArgumentException.class, () ->
                versionComparisonService.compareReleases("proj-1", "rel-1", "rel-1", userId)
        );
    }

    @Test
    @DisplayName("4. Throws ResourceNotFoundException when base release is missing or unowned")
    public void testMissingBaseRelease() {
        String userId = "user-1";
        when(releaseRepository.findByIdAndUserId("rel-missing", userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                versionComparisonService.compareReleases("proj-1", "rel-missing", "rel-2", userId)
        );
    }

    @Test
    @DisplayName("5. Throws ResourceNotFoundException when target release is missing or unowned")
    public void testMissingTargetRelease() {
        String userId = "user-1";
        Release baseRel = new Release("proj-1", userId, "1.0.0", "Release 1", "Desc");
        baseRel.setId("rel-1");

        when(releaseRepository.findByIdAndUserId("rel-1", userId)).thenReturn(Optional.of(baseRel));
        when(releaseRepository.findByIdAndUserId("rel-missing", userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                versionComparisonService.compareReleases("proj-1", "rel-1", "rel-missing", userId)
        );
    }

    @Test
    @DisplayName("6. Handles releases with no analysis cleanly without throwing exceptions")
    public void testReleaseWithNoAnalysis() {
        String projectId = "proj-1";
        String userId = "user-1";
        Release baseRel = new Release(projectId, userId, "1.0.0", "Release 1", "Desc");
        baseRel.setId("rel-1");
        Release targetRel = new Release(projectId, userId, "1.1.0", "Release 2", "Desc");
        targetRel.setId("rel-2");

        when(releaseRepository.findByIdAndUserId("rel-1", userId)).thenReturn(Optional.of(baseRel));
        when(releaseRepository.findByIdAndUserId("rel-2", userId)).thenReturn(Optional.of(targetRel));
        when(projectRepository.findByIdAndUserId(projectId, userId)).thenReturn(Optional.empty());
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc("rel-1", userId)).thenReturn(Optional.empty());
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc("rel-2", userId)).thenReturn(Optional.empty());

        VersionComparisonResponse response = versionComparisonService.compareReleases(projectId, "rel-1", "rel-2", userId);

        assertNotNull(response);
        assertEquals(0, response.getFindingsComparison().getBaseTotal());
        assertEquals(0, response.getFindingsComparison().getTargetTotal());
        assertEquals(0, response.getFindingsComparison().getDelta());
        assertEquals(BigDecimal.ZERO, response.getRiskComparison().getDeltaWeightedRiskPoints());
        assertEquals(BigDecimal.ZERO, response.getReadinessComparison().getScoreDelta());
    }
}
