package com.aireadiness.risk;

import com.aireadiness.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RiskEngineTest {

    private RiskEngine riskEngine;

    @BeforeEach
    void setUp() {
        riskEngine = new RiskEngine();
    }

    @Test
    @DisplayName("Should evaluate complete analysis with zero findings as LOW risk with 0 points")
    void testZeroFindingsCompleteAnalysisReturnsLowRisk() {
        UnifiedAnalysisSummary unified = new UnifiedAnalysisSummary();
        unified.setCompleteness("COMPLETE");
        unified.setTotalFindings(0);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        RiskSummary risk = riskEngine.calculateRisk(unified, Collections.emptyList(), profile);

        assertNotNull(risk);
        assertEquals(RiskLevel.LOW, risk.getOverallRiskLevel());
        assertEquals(new BigDecimal("0.00"), risk.getWeightedRiskPoints());
        assertEquals(new BigDecimal("0.00"), risk.getBaseRiskPoints());
        assertEquals("risk-v1", risk.getCalculationVersion());
        assertTrue(risk.getRiskWarnings().isEmpty());
    }

    @Test
    @DisplayName("Should enforce exact boundary thresholds: 0, 9.99, 10.00, 24.99, 25.00, 49.99, 50.00")
    void testExactBoundaryScores() {
        assertEquals(RiskLevel.LOW, riskEngine.classifyRiskLevel(new BigDecimal("0.00")));
        assertEquals(RiskLevel.LOW, riskEngine.classifyRiskLevel(new BigDecimal("9.99")));
        assertEquals(RiskLevel.MEDIUM, riskEngine.classifyRiskLevel(new BigDecimal("10.00")));
        assertEquals(RiskLevel.MEDIUM, riskEngine.classifyRiskLevel(new BigDecimal("24.99")));
        assertEquals(RiskLevel.HIGH, riskEngine.classifyRiskLevel(new BigDecimal("25.00")));
        assertEquals(RiskLevel.HIGH, riskEngine.classifyRiskLevel(new BigDecimal("49.99")));
        assertEquals(RiskLevel.CRITICAL, riskEngine.classifyRiskLevel(new BigDecimal("50.00")));
    }

    @Test
    @DisplayName("Should trigger HIGH security override when high security finding exists and score < 25.00")
    void testHighSecurityOverride() {
        UnifiedAnalysisSummary unified = new UnifiedAnalysisSummary();
        unified.setCompleteness("COMPLETE");
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        // 1 High Security finding: 10 * 1.5 = 15.00 points (normal threshold = MEDIUM)
        // High security override forces risk level to HIGH
        RiskSummary risk1 = riskEngine.calculateRisk(unified, List.of(createFinding("SECURITY", "HIGH")), profile);
        assertEquals(new BigDecimal("15.00"), risk1.getWeightedRiskPoints());
        assertEquals(RiskLevel.HIGH, risk1.getOverallRiskLevel());

        // 4 High Security findings: 4 * 15 = 60.00 points => CRITICAL score threshold
        RiskSummary risk2 = riskEngine.calculateRisk(unified, List.of(
                createFinding("SECURITY", "HIGH"),
                createFinding("SECURITY", "HIGH"),
                createFinding("SECURITY", "HIGH"),
                createFinding("SECURITY", "HIGH")
        ), profile);
        assertEquals(new BigDecimal("60.00"), risk2.getWeightedRiskPoints());
        assertEquals(RiskLevel.CRITICAL, risk2.getOverallRiskLevel());
    }

    @Test
    @DisplayName("Should apply category multipliers correctly (SECURITY=1.50, DEPENDENCY=1.25, PERFORMANCE=1.10, TESTING=1.00, CODE_QUALITY=1.00)")
    void testCategoryMultipliers() {
        assertEquals(new BigDecimal("1.50"), riskEngine.getCategoryMultiplier("SECURITY"));
        assertEquals(new BigDecimal("1.25"), riskEngine.getCategoryMultiplier("DEPENDENCY"));
        assertEquals(new BigDecimal("1.25"), riskEngine.getCategoryMultiplier("DEPENDENCIES"));
        assertEquals(new BigDecimal("1.10"), riskEngine.getCategoryMultiplier("PERFORMANCE"));
        assertEquals(new BigDecimal("1.00"), riskEngine.getCategoryMultiplier("TESTING"));
        assertEquals(new BigDecimal("1.00"), riskEngine.getCategoryMultiplier("CODE_QUALITY"));

        UnifiedAnalysisSummary unified = new UnifiedAnalysisSummary();
        unified.setCompleteness("COMPLETE");
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        List<Finding> findings = List.of(
                createFinding("SECURITY", "HIGH"),      // 10 * 1.50 = 15.00
                createFinding("DEPENDENCY", "MEDIUM"),  // 5 * 1.25 = 6.25
                createFinding("PERFORMANCE", "INFO"),   // 0 * 1.10 = 0.00
                createFinding("TESTING", "LOW"),        // 1 * 1.00 = 1.00
                createFinding("CODE_QUALITY", "MEDIUM") // 5 * 1.00 = 5.00
        );

        RiskSummary risk = riskEngine.calculateRisk(unified, findings, profile);
        // Base = 10 + 5 + 0 + 1 + 5 = 21.00
        assertEquals(new BigDecimal("21.00"), risk.getBaseRiskPoints());
        // Weighted = 15.00 + 6.25 + 0.00 + 1.00 + 5.00 = 27.25
        assertEquals(new BigDecimal("27.25"), risk.getWeightedRiskPoints());
        assertEquals(RiskLevel.HIGH, risk.getOverallRiskLevel());
    }

    @Test
    @DisplayName("Should treat CRITICAL severity findings consistently with HIGH weight (10.00)")
    void testCriticalSeverityHandling() {
        UnifiedAnalysisSummary unified = new UnifiedAnalysisSummary();
        unified.setCompleteness("COMPLETE");
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        Finding criticalFinding = createFinding("CODE_QUALITY", "CRITICAL");
        RiskSummary risk = riskEngine.calculateRisk(unified, List.of(criticalFinding), profile);

        assertEquals(new BigDecimal("10.00"), risk.getBaseRiskPoints());
        assertEquals(new BigDecimal("10.00"), risk.getWeightedRiskPoints());
        assertEquals(1, risk.getHighFindings());
        assertEquals(RiskLevel.MEDIUM, risk.getOverallRiskLevel());
    }

    @Test
    @DisplayName("Should handle null findings list gracefully without NPE")
    void testNullFindings() {
        UnifiedAnalysisSummary unified = new UnifiedAnalysisSummary();
        unified.setCompleteness("COMPLETE");
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        RiskSummary risk = riskEngine.calculateRisk(unified, null, profile);

        assertNotNull(risk);
        assertEquals(0, risk.getTotalFindings());
        assertEquals(new BigDecimal("0.00"), risk.getWeightedRiskPoints());
        assertEquals(RiskLevel.LOW, risk.getOverallRiskLevel());
    }

    @Test
    @DisplayName("Should handle null unified summary gracefully")
    void testNullUnifiedSummary() {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        RiskSummary risk = riskEngine.calculateRisk(null, Collections.emptyList(), profile);

        assertNotNull(risk);
        assertEquals("UNKNOWN", risk.getCompleteness());
        assertEquals(RiskLevel.UNKNOWN, risk.getOverallRiskLevel());
        assertTrue(risk.getRiskWarnings().stream().anyMatch(w -> w.contains("marked UNKNOWN")));
    }

    @Test
    @DisplayName("Should attach warning and RiskFactor for PARTIAL completeness")
    void testPartialCompleteness() {
        UnifiedAnalysisSummary partialUnified = new UnifiedAnalysisSummary();
        partialUnified.setCompleteness("PARTIAL");
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        RiskSummary partialRisk = riskEngine.calculateRisk(partialUnified, List.of(createFinding("CODE_QUALITY", "LOW")), profile);
        assertEquals("PARTIAL", partialRisk.getCompleteness());
        assertTrue(partialRisk.getRiskWarnings().stream().anyMatch(w -> w.contains("selected uploaded content")));
        assertTrue(partialRisk.getRiskFactors().stream().anyMatch(rf -> "Partial Upload Coverage".equals(rf.getTitle())));
    }

    @Test
    @DisplayName("Should set overall risk level to UNKNOWN for UNKNOWN completeness")
    void testUnknownCompleteness() {
        UnifiedAnalysisSummary unknownUnified = new UnifiedAnalysisSummary();
        unknownUnified.setCompleteness("UNKNOWN");
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        RiskSummary unknownRisk = riskEngine.calculateRisk(unknownUnified, Collections.emptyList(), profile);
        assertEquals("UNKNOWN", unknownRisk.getCompleteness());
        assertEquals(RiskLevel.UNKNOWN, unknownRisk.getOverallRiskLevel());
        assertTrue(unknownRisk.getRiskWarnings().stream().anyMatch(w -> w.contains("marked UNKNOWN")));
        assertTrue(unknownRisk.getRiskFactors().stream().anyMatch(rf -> "Incomplete Analysis Coverage".equals(rf.getTitle())));
    }

    @Test
    @DisplayName("Should propagate failed analyzers warnings")
    void testFailedAnalyzerWarnings() {
        UnifiedAnalysisSummary unified = new UnifiedAnalysisSummary();
        unified.setCompleteness("PARTIAL");
        unified.setFailedAnalyzers(List.of("TESTING", "PERFORMANCE"));

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        RiskSummary risk = riskEngine.calculateRisk(unified, Collections.emptyList(), profile);
        assertTrue(risk.getRiskWarnings().stream().anyMatch(w -> w.contains("TESTING, PERFORMANCE")));
    }

    @Test
    @DisplayName("Should calculate category breakdown accurately for all 5 categories")
    void testCategoryBreakdown() {
        UnifiedAnalysisSummary unified = new UnifiedAnalysisSummary();
        unified.setCompleteness("COMPLETE");
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        List<Finding> findings = List.of(
                createFinding("SECURITY", "HIGH"),
                createFinding("PERFORMANCE", "MEDIUM"),
                createFinding("PERFORMANCE", "LOW")
        );

        RiskSummary risk = riskEngine.calculateRisk(unified, findings, profile);
        Map<String, CategoryRisk> catMap = risk.getCategoryRisk();

        assertNotNull(catMap);
        assertEquals(5, catMap.size());

        CategoryRisk secRisk = catMap.get("SECURITY");
        assertEquals(1, secRisk.getFindingCount());
        assertEquals(1, secRisk.getHighFindings());
        assertEquals(new BigDecimal("15.00"), secRisk.getWeightedRiskPoints());

        CategoryRisk perfRisk = catMap.get("PERFORMANCE");
        assertEquals(2, perfRisk.getFindingCount());
        assertEquals(1, perfRisk.getMediumFindings());
        assertEquals(1, perfRisk.getLowFindings());
        assertEquals(new BigDecimal("6.60"), perfRisk.getWeightedRiskPoints());
    }

    @Test
    @DisplayName("Should return all 5 categories with zero findings when no findings are present")
    void testZeroFindingsCategoryBreakdown() {
        UnifiedAnalysisSummary unified = new UnifiedAnalysisSummary();
        unified.setCompleteness("COMPLETE");
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        RiskSummary risk = riskEngine.calculateRisk(unified, Collections.emptyList(), profile);
        Map<String, CategoryRisk> catMap = risk.getCategoryRisk();

        assertNotNull(catMap);
        assertEquals(5, catMap.size());
        assertTrue(catMap.containsKey("CODE_QUALITY"));
        assertTrue(catMap.containsKey("TESTING"));
        assertTrue(catMap.containsKey("DEPENDENCY"));
        assertTrue(catMap.containsKey("SECURITY"));
        assertTrue(catMap.containsKey("PERFORMANCE"));

        for (CategoryRisk cr : catMap.values()) {
            assertEquals(0, cr.getFindingCount());
            assertEquals(new BigDecimal("0.00"), cr.getWeightedRiskPoints());
            assertEquals(RiskLevel.LOW, cr.getRiskLevel());
        }
    }

    @Test
    @DisplayName("Should generate deterministic risk factors derived strictly from empirical findings and summary data")
    void testDeterministicRiskFactors() {
        UnifiedAnalysisSummary unified = new UnifiedAnalysisSummary();
        unified.setCompleteness("COMPLETE");
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        List<Finding> findings = List.of(
                createFinding("SECURITY", "HIGH"),
                createFinding("DEPENDENCY", "MEDIUM"),
                createFinding("PERFORMANCE", "LOW")
        );

        RiskSummary risk = riskEngine.calculateRisk(unified, findings, profile);
        List<RiskFactor> factors = risk.getRiskFactors();

        assertNotNull(factors);
        assertFalse(factors.isEmpty());

        assertEquals("High Security Findings Detected", factors.get(0).getTitle());
        assertEquals("HIGH", factors.get(0).getSeverity());

        assertEquals("High Severity Issues Concentration", factors.get(1).getTitle());
        assertEquals("Dependency Declarations Risk", factors.get(2).getTitle());
        assertEquals("Static Performance Smells", factors.get(3).getTitle());
    }

    private Finding createFinding(String category, String severity) {
        Finding f = new Finding();
        f.setCategory(category);
        f.setSeverity(severity);
        f.setRuleId("RULE_" + category + "_" + severity);
        f.setTitle("Title for " + category);
        f.setDescription("Description");
        return f;
    }
}
