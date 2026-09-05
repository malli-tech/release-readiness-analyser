package com.aireadiness.readiness;

import com.aireadiness.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ReadinessScoreEngineTest {

    private ReadinessScoreEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ReadinessScoreEngine();
    }

    @Test
    @DisplayName("1. COMPLETE analysis with 0 risk points should yield 100.00 EXCELLENT with HIGH confidence")
    void testCompleteZeroFindingsReturns100() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("0.00"), new BigDecimal("0.00"), RiskLevel.LOW, 0, 0, 0, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertNotNull(res);
        assertEquals(new BigDecimal("100.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.EXCELLENT, res.getReadinessLevel());
        assertEquals(ReadinessConfidence.HIGH, res.getConfidence());
        assertEquals("readiness-v1", res.getCalculationVersion());
    }

    @Test
    @DisplayName("2. 5.00 risk points -> 95.00 EXCELLENT")
    void testFiveRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("5.00"), new BigDecimal("5.00"), RiskLevel.LOW, 1, 0, 1, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("95.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.EXCELLENT, res.getReadinessLevel());
    }

    @Test
    @DisplayName("3. 10.00 risk points -> 90.00 EXCELLENT")
    void testTenRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("10.00"), new BigDecimal("10.00"), RiskLevel.MEDIUM, 1, 0, 2, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("90.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.EXCELLENT, res.getReadinessLevel());
    }

    @Test
    @DisplayName("4. 10.01 risk points -> 89.99 GOOD")
    void testTenPointZeroOneRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("10.01"), new BigDecimal("10.00"), RiskLevel.MEDIUM, 2, 0, 2, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("89.99"), res.getReadinessScore());
        assertEquals(ReadinessLevel.GOOD, res.getReadinessLevel());
    }

    @Test
    @DisplayName("5. 25.00 risk points -> 75.00 GOOD")
    void testTwentyFiveRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("25.00"), new BigDecimal("20.00"), RiskLevel.HIGH, 2, 2, 0, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("75.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.GOOD, res.getReadinessLevel());
    }

    @Test
    @DisplayName("6. 25.01 risk points -> 74.99 FAIR")
    void testTwentyFivePointZeroOneRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("25.01"), new BigDecimal("20.00"), RiskLevel.HIGH, 3, 2, 1, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("74.99"), res.getReadinessScore());
        assertEquals(ReadinessLevel.FAIR, res.getReadinessLevel());
    }

    @Test
    @DisplayName("7. 50.00 risk points -> 50.00 FAIR")
    void testFiftyRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("50.00"), new BigDecimal("50.00"), RiskLevel.CRITICAL, 5, 5, 0, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("50.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.FAIR, res.getReadinessLevel());
    }

    @Test
    @DisplayName("8. 50.01 risk points -> 49.99 POOR")
    void testFiftyPointZeroOneRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("50.01"), new BigDecimal("50.00"), RiskLevel.CRITICAL, 6, 5, 0, 1, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("49.99"), res.getReadinessScore());
        assertEquals(ReadinessLevel.POOR, res.getReadinessLevel());
    }

    @Test
    @DisplayName("9. 75.00 risk points -> 25.00 POOR")
    void testSeventyFiveRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("75.00"), new BigDecimal("70.00"), RiskLevel.CRITICAL, 7, 7, 0, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("25.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.POOR, res.getReadinessLevel());
    }

    @Test
    @DisplayName("10. 75.01 risk points -> 24.99 NOT_READY")
    void testSeventyFivePointZeroOneRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("75.01"), new BigDecimal("70.00"), RiskLevel.CRITICAL, 8, 7, 1, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("24.99"), res.getReadinessScore());
        assertEquals(ReadinessLevel.NOT_READY, res.getReadinessLevel());
    }

    @Test
    @DisplayName("11. 100.00 risk points -> 0.00 NOT_READY")
    void testOneHundredRiskPoints() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("100.00"), new BigDecimal("100.00"), RiskLevel.CRITICAL, 10, 10, 0, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("0.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.NOT_READY, res.getReadinessLevel());
    }

    @Test
    @DisplayName("12. >100 risk points -> 0.00 (clamped) NOT_READY")
    void testOverOneHundredRiskPointsClampedToZero() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("150.00"), new BigDecimal("120.00"), RiskLevel.CRITICAL, 15, 12, 3, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("0.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.NOT_READY, res.getReadinessLevel());
    }

    @Test
    @DisplayName("13. PARTIAL completeness -> numeric score computed, MEDIUM confidence, warning attached")
    void testPartialCompleteness() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("PARTIAL", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("15.00"), new BigDecimal("12.00"), RiskLevel.MEDIUM, 2, 1, 1, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("85.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.GOOD, res.getReadinessLevel());
        assertEquals(ReadinessConfidence.MEDIUM, res.getConfidence());
        assertTrue(res.getReadinessWarnings().stream().anyMatch(w -> w.contains("partial analysis coverage")));
    }

    @Test
    @DisplayName("14. UNKNOWN completeness -> null score, UNKNOWN readiness level, UNKNOWN confidence, warning present")
    void testUnknownCompleteness() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("UNKNOWN", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("0.00"), new BigDecimal("0.00"), RiskLevel.UNKNOWN, 0, 0, 0, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertNull(res.getReadinessScore());
        assertEquals(ReadinessLevel.UNKNOWN, res.getReadinessLevel());
        assertEquals(ReadinessConfidence.UNKNOWN, res.getConfidence());
        assertTrue(res.getReadinessWarnings().stream().anyMatch(w -> w.contains("unknown or insufficient analysis coverage")));
    }

    @Test
    @DisplayName("15. RiskSummary overallRiskLevel UNKNOWN -> UNKNOWN readiness")
    void testUnknownRiskLevelYieldsUnknownReadiness() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("0.00"), new BigDecimal("0.00"), RiskLevel.UNKNOWN, 0, 0, 0, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertNull(res.getReadinessScore());
        assertEquals(ReadinessLevel.UNKNOWN, res.getReadinessLevel());
        assertEquals(ReadinessConfidence.UNKNOWN, res.getConfidence());
    }

    @Test
    @DisplayName("16. HIGH risk with high security findings -> score derived from points, security factor present")
    void testHighRiskWithSecurityFindings() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("15.00"), new BigDecimal("10.00"), RiskLevel.HIGH, 1, 1, 0, 0, 0);

        CategoryRisk secRisk = new CategoryRisk("SECURITY");
        secRisk.setFindingCount(1);
        secRisk.setHighFindings(1);
        secRisk.setWeightedRiskPoints(new BigDecimal("15.00"));
        risk.setCategoryRisk(Map.of("SECURITY", secRisk));

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("85.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.GOOD, res.getReadinessLevel());
        assertTrue(res.getReadinessFactors().stream().anyMatch(f -> "High-Security Findings Present".equals(f.getFactor())));
    }

    @Test
    @DisplayName("17. CRITICAL risk -> score calculated based on points without double penalty")
    void testCriticalRiskNoDoublePenalty() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("55.00"), new BigDecimal("50.00"), RiskLevel.CRITICAL, 5, 5, 0, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("45.00"), res.getReadinessScore());
        assertEquals(ReadinessLevel.POOR, res.getReadinessLevel());
    }

    @Test
    @DisplayName("18. Null risk summary safety -> UNKNOWN readiness")
    void testNullRiskSummary() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());

        ReadinessScore res = engine.calculateReadiness(unified, null, createProfile());

        assertNull(res.getReadinessScore());
        assertEquals(ReadinessLevel.UNKNOWN, res.getReadinessLevel());
        assertEquals(ReadinessConfidence.UNKNOWN, res.getConfidence());
    }

    @Test
    @DisplayName("19. Deterministic factor ordering")
    void testDeterministicFactorOrdering() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("PARTIAL", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("15.00"), new BigDecimal("10.00"), RiskLevel.HIGH, 1, 1, 0, 0, 0);

        CategoryRisk secRisk = new CategoryRisk("SECURITY");
        secRisk.setFindingCount(1);
        secRisk.setHighFindings(1);
        risk.setCategoryRisk(Map.of("SECURITY", secRisk));

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());
        List<ReadinessFactor> factors = res.getReadinessFactors();

        assertNotNull(factors);
        assertFalse(factors.isEmpty());
        assertEquals("High Severity Findings Present", factors.get(0).getFactor());
        assertEquals("Elevated Release Risk", factors.get(1).getFactor());
        assertEquals("High-Security Findings Present", factors.get(2).getFactor());
    }

    @Test
    @DisplayName("20. Deterministic repeated calculation yields identical ReadinessScore")
    void testRepeatedCalculationIsIdentical() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("12.50"), new BigDecimal("10.00"), RiskLevel.MEDIUM, 2, 0, 2, 0, 0);
        ProjectProfile profile = createProfile();

        ReadinessScore res1 = engine.calculateReadiness(unified, risk, profile);
        ReadinessScore res2 = engine.calculateReadiness(unified, risk, profile);

        assertEquals(res1.getReadinessScore(), res2.getReadinessScore());
        assertEquals(res1.getReadinessLevel(), res2.getReadinessLevel());
        assertEquals(res1.getConfidence(), res2.getConfidence());
        assertEquals(res1.getReadinessFactors().size(), res2.getReadinessFactors().size());
    }

    @Test
    @DisplayName("21. No mutation of input UnifiedAnalysisSummary")
    void testNoMutationOfUnifiedSummary() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", List.of("Warn 1"));
        int origSize = unified.getWarnings().size();
        RiskSummary risk = createRiskSummary(new BigDecimal("5.00"), new BigDecimal("5.00"), RiskLevel.LOW, 1, 0, 1, 0, 0);

        engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(origSize, unified.getWarnings().size());
    }

    @Test
    @DisplayName("22. No mutation of input RiskSummary")
    void testNoMutationOfRiskSummary() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("5.00"), new BigDecimal("5.00"), RiskLevel.LOW, 1, 0, 1, 0, 0);
        risk.setRiskWarnings(List.of("Risk warning 1"));
        int origSize = risk.getRiskWarnings().size();

        engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(origSize, risk.getRiskWarnings().size());
    }

    @Test
    @DisplayName("23. Failed analyzers propagate warning and set confidence to MEDIUM")
    void testFailedAnalyzersPropagateWarning() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("PARTIAL", Collections.emptyList());
        unified.setFailedAnalyzers(List.of("PERFORMANCE"));
        RiskSummary risk = createRiskSummary(new BigDecimal("5.00"), new BigDecimal("5.00"), RiskLevel.LOW, 1, 0, 1, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(ReadinessConfidence.MEDIUM, res.getConfidence());
        assertTrue(res.getReadinessWarnings().stream().anyMatch(w -> w.contains("PERFORMANCE")));
    }

    @Test
    @DisplayName("24. Base risk points and weighted risk points are copied accurately")
    void testRiskPointsAreCopiedAccurately() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("22.50"), new BigDecimal("16.00"), RiskLevel.MEDIUM, 3, 1, 1, 1, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals(new BigDecimal("22.50"), res.getWeightedRiskPoints());
        assertEquals(new BigDecimal("16.00"), res.getBaseRiskPoints());
    }

    @Test
    @DisplayName("25. Calculation version is strictly readiness-v1")
    void testCalculationVersionIsReadinessV1() {
        UnifiedAnalysisSummary unified = createUnifiedSummary("COMPLETE", Collections.emptyList());
        RiskSummary risk = createRiskSummary(new BigDecimal("0.00"), new BigDecimal("0.00"), RiskLevel.LOW, 0, 0, 0, 0, 0);

        ReadinessScore res = engine.calculateReadiness(unified, risk, createProfile());

        assertEquals("readiness-v1", res.getCalculationVersion());
    }

    private UnifiedAnalysisSummary createUnifiedSummary(String completeness, List<String> warnings) {
        UnifiedAnalysisSummary u = new UnifiedAnalysisSummary();
        u.setCompleteness(completeness);
        u.setWarnings(new ArrayList<>(warnings));
        return u;
    }

    private RiskSummary createRiskSummary(
            BigDecimal weightedPoints,
            BigDecimal basePoints,
            RiskLevel overallLevel,
            int total,
            int high,
            int med,
            int low,
            int info
    ) {
        RiskSummary r = new RiskSummary();
        r.setWeightedRiskPoints(weightedPoints);
        r.setBaseRiskPoints(basePoints);
        r.setOverallRiskLevel(overallLevel);
        r.setTotalFindings(total);
        r.setHighFindings(high);
        r.setMediumFindings(med);
        r.setLowFindings(low);
        r.setInfoFindings(info);
        return r;
    }

    private ProjectProfile createProfile() {
        ProjectProfile p = new ProjectProfile();
        p.setPrimaryLanguage("Java");
        p.setProjectType("MAVEN");
        return p;
    }
}
