package com.aireadiness.recommendation;

import com.aireadiness.model.Analysis;
import com.aireadiness.model.Finding;
import com.aireadiness.recommendation.catalog.RecommendationTemplateCatalog;
import com.aireadiness.recommendation.model.Recommendation;
import com.aireadiness.recommendation.model.RecommendationPriority;
import com.aireadiness.recommendation.model.RecommendationStatus;
import com.aireadiness.recommendation.repository.RecommendationRepository;
import com.aireadiness.recommendation.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecommendationServiceTest {

    private RecommendationTemplateCatalog catalog;

    @Mock
    private RecommendationRepository recommendationRepository;

    private RecommendationService recommendationService;

    @BeforeEach
    public void setUp() {
        catalog = new RecommendationTemplateCatalog();
        recommendationService = new RecommendationService(catalog, recommendationRepository);
    }

    @Test
    @DisplayName("1. Returns empty recommendations list when findings are empty or null")
    public void testZeroFindingsReturnsEmptyList() {
        List<Recommendation> recs1 = recommendationService.generateRecommendations(null);
        assertNotNull(recs1);
        assertTrue(recs1.isEmpty());

        Analysis emptyAnalysis = new Analysis();
        emptyAnalysis.setFindings(Collections.emptyList());
        List<Recommendation> recs2 = recommendationService.generateRecommendations(emptyAnalysis);
        assertNotNull(recs2);
        assertTrue(recs2.isEmpty());
    }

    @Test
    @DisplayName("2. Generates deterministic recommendation for supported finding preserving identities")
    public void testSupportedFindingGeneratesRecommendation() {
        Finding finding = new Finding();
        finding.setId("find-1");
        finding.setAnalysisId("ans-100");
        finding.setCategory("SECURITY");
        finding.setRuleId("SECURITY_SQL_INJECTION_RISK");
        finding.setSeverity("HIGH");
        finding.setFilePath("src/UserRepository.java");
        finding.setLineNumber(42);

        when(recommendationRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Recommendation> recs = recommendationService.generateRecommendations(List.of(finding), "ans-100", null);

        assertNotNull(recs);
        assertEquals(1, recs.size());

        Recommendation rec = recs.get(0);
        assertEquals("ans-100", rec.getAnalysisId());
        assertEquals("find-1", rec.getFindingId());
        assertEquals("SECURITY_SQL_INJECTION_RISK", rec.getRuleId());
        assertEquals("SECURITY", rec.getCategory());
        assertEquals("HIGH", rec.getSeverity());
        assertEquals("Use Parameterized SQL Queries", rec.getTitle());
        assertEquals("CRITICAL", rec.getPriority().name(), "HIGH security findings should resolve to CRITICAL priority");
        assertEquals(RecommendationStatus.OPEN, rec.getStatus());
        assertEquals("src/UserRepository.java", rec.getFilePath());
        assertEquals(42, rec.getLineNumber());

        verify(recommendationRepository, times(1)).deleteByAnalysisId("ans-100");
        verify(recommendationRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("3. Deduplicates findings with identical findingId")
    public void testDuplicateFindingDeduplication() {
        Finding finding1 = new Finding();
        finding1.setId("find-dup");
        finding1.setRuleId("SECURITY_HARDCODED_SECRET");

        Finding finding2 = new Finding();
        finding2.setId("find-dup");
        finding2.setRuleId("SECURITY_HARDCODED_SECRET");

        when(recommendationRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Recommendation> recs = recommendationService.generateRecommendations(List.of(finding1, finding2), "ans-100", null);

        assertNotNull(recs);
        assertEquals(1, recs.size());
    }

    @Test
    @DisplayName("4. Skips unsupported rule IDs and records warning")
    public void testUnsupportedRuleSkippedAndWarningLogged() {
        Finding finding = new Finding();
        finding.setId("find-unsupported");
        finding.setRuleId("UNSUPPORTED_RULE_XYZ");

        List<String> warnings = new ArrayList<>();
        List<Recommendation> recs = recommendationService.generateRecommendations(List.of(finding), "ans-100", warnings);

        assertNotNull(recs);
        assertTrue(recs.isEmpty());
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("Unsupported rule ID"));
    }

    @Test
    @DisplayName("5. Orders recommendations deterministically by Priority -> Category -> RuleId")
    public void testDeterministicOrdering() {
        Finding fLow = new Finding();
        fLow.setId("find-low");
        fLow.setRuleId("CODE_QUALITY_MAGIC_NUMBER");
        fLow.setCategory("CODE_QUALITY");
        fLow.setSeverity("LOW");

        Finding fHigh = new Finding();
        fHigh.setId("find-high");
        fHigh.setRuleId("PERFORMANCE_N_PLUS_ONE_QUERY");
        fHigh.setCategory("PERFORMANCE");
        fHigh.setSeverity("HIGH");

        Finding fCrit = new Finding();
        fCrit.setId("find-crit");
        fCrit.setRuleId("SECURITY_HARDCODED_SECRET");
        fCrit.setCategory("SECURITY");
        fCrit.setSeverity("CRITICAL");

        when(recommendationRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Recommendation> recs = recommendationService.generateRecommendations(List.of(fLow, fHigh, fCrit), "ans-100", null);

        assertEquals(3, recs.size());
        assertEquals(RecommendationPriority.CRITICAL, recs.get(0).getPriority());
        assertEquals(RecommendationPriority.HIGH, recs.get(1).getPriority());
        assertEquals(RecommendationPriority.LOW, recs.get(2).getPriority());
    }

    @Test
    @DisplayName("6. Generates recommendations for partial analysis without fabricating non-existent findings")
    public void testPartialAnalysisHandling() {
        Analysis partialAnalysis = new Analysis();
        partialAnalysis.setId("ans-partial");
        partialAnalysis.setWarnings(new ArrayList<>(List.of("Partial analysis warning")));

        Finding f1 = new Finding();
        f1.setId("find-part-1");
        f1.setRuleId("DEPENDENCY_UNPINNED_VERSION");
        f1.setCategory("DEPENDENCY");
        f1.setSeverity("MEDIUM");
        partialAnalysis.setFindings(List.of(f1));

        when(recommendationRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        List<Recommendation> recs = recommendationService.generateRecommendations(partialAnalysis);

        assertNotNull(recs);
        assertEquals(1, recs.size());
        assertEquals("ans-partial", recs.get(0).getAnalysisId());
        assertEquals("find-part-1", recs.get(0).getFindingId());
    }
}
