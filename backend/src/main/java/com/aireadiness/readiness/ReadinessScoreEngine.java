package com.aireadiness.readiness;

import com.aireadiness.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class ReadinessScoreEngine {

    public static final String CALCULATION_VERSION = "readiness-v1";

    public static final BigDecimal SCORE_MAX = new BigDecimal("100.00");
    public static final BigDecimal BAND_EXCELLENT = new BigDecimal("90.00");
    public static final BigDecimal BAND_GOOD = new BigDecimal("75.00");
    public static final BigDecimal BAND_FAIR = new BigDecimal("50.00");
    public static final BigDecimal BAND_POOR = new BigDecimal("25.00");

    public ReadinessScore calculateReadiness(UnifiedAnalysisSummary unifiedSummary, RiskSummary riskSummary, ProjectProfile profile) {
        ReadinessScore scoreObj = new ReadinessScore();
        scoreObj.setCalculationVersion(CALCULATION_VERSION);

        Set<String> warningSet = new LinkedHashSet<>();

        if (unifiedSummary != null && unifiedSummary.getWarnings() != null) {
            warningSet.addAll(unifiedSummary.getWarnings());
        }
        if (riskSummary != null && riskSummary.getRiskWarnings() != null) {
            warningSet.addAll(riskSummary.getRiskWarnings());
        }

        String completeness = unifiedSummary != null ? unifiedSummary.getCompleteness() : "UNKNOWN";
        scoreObj.setCompleteness(completeness);

        String riskLevelStr = riskSummary != null && riskSummary.getOverallRiskLevel() != null
                ? riskSummary.getOverallRiskLevel().name()
                : "UNKNOWN";
        scoreObj.setRiskLevel(riskLevelStr);

        if (riskSummary != null) {
            scoreObj.setWeightedRiskPoints(riskSummary.getWeightedRiskPoints());
            scoreObj.setBaseRiskPoints(riskSummary.getBaseRiskPoints());
            scoreObj.setTotalFindings(riskSummary.getTotalFindings());
            scoreObj.setHighFindings(riskSummary.getHighFindings());
            scoreObj.setMediumFindings(riskSummary.getMediumFindings());
            scoreObj.setLowFindings(riskSummary.getLowFindings());
            scoreObj.setInfoFindings(riskSummary.getInfoFindings());
        }

        // UNKNOWN Completeness / Unsupported Ecosystem / Missing Risk Summary Handling
        if (riskSummary == null ||
                "UNKNOWN".equalsIgnoreCase(completeness) ||
                "UNKNOWN".equalsIgnoreCase(riskLevelStr) ||
                (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType()))) {

            scoreObj.setReadinessScore(null);
            scoreObj.setReadinessLevel(ReadinessLevel.UNKNOWN);
            scoreObj.setConfidence(ReadinessConfidence.UNKNOWN);
            warningSet.add("Readiness score could not be calculated due to unknown or insufficient analysis coverage.");
            scoreObj.setReadinessWarnings(new ArrayList<>(warningSet));

            List<ReadinessFactor> factors = new ArrayList<>();
            factors.add(new ReadinessFactor(
                    "Unknown Analysis Coverage",
                    "Analysis coverage is unknown or unsupported, preventing reliable readiness scoring.",
                    "HIGH",
                    "COMPLETENESS"
            ));
            scoreObj.setReadinessFactors(factors);
            return scoreObj;
        }

        // 1. Calculate Numeric Score: max(0.00, 100.00 - weightedRiskPoints)
        BigDecimal weightedPoints = riskSummary.getWeightedRiskPoints() != null ? riskSummary.getWeightedRiskPoints() : BigDecimal.ZERO;
        BigDecimal calculatedScore = SCORE_MAX.subtract(weightedPoints);
        if (calculatedScore.compareTo(BigDecimal.ZERO) < 0) {
            calculatedScore = BigDecimal.ZERO;
        }
        calculatedScore = calculatedScore.setScale(2, RoundingMode.HALF_UP);
        scoreObj.setReadinessScore(calculatedScore);

        // 2. Classify Readiness Level
        ReadinessLevel level = classifyReadinessLevel(calculatedScore);
        scoreObj.setReadinessLevel(level);

        // 3. Classify Readiness Confidence
        if ("COMPLETE".equalsIgnoreCase(completeness)) {
            scoreObj.setConfidence(ReadinessConfidence.HIGH);
        } else if ("PARTIAL".equalsIgnoreCase(completeness)) {
            scoreObj.setConfidence(ReadinessConfidence.MEDIUM);
            warningSet.add("Readiness is based on partial analysis coverage. Additional unanalyzed content may contain further risks.");
        } else {
            scoreObj.setConfidence(ReadinessConfidence.MEDIUM);
        }

        // Add warning for failed analyzers if applicable
        if (unifiedSummary != null && unifiedSummary.getFailedAnalyzers() != null && !unifiedSummary.getFailedAnalyzers().isEmpty()) {
            scoreObj.setConfidence(ReadinessConfidence.MEDIUM);
            warningSet.add("Readiness calculation is based on partial analyzer coverage because one or more analyzers failed: " + String.join(", ", unifiedSummary.getFailedAnalyzers()));
        }

        scoreObj.setReadinessWarnings(new ArrayList<>(warningSet));

        // 4. Generate Deterministic Readiness Factors
        List<ReadinessFactor> factors = generateReadinessFactors(
                calculatedScore,
                riskSummary,
                completeness
        );
        scoreObj.setReadinessFactors(factors);

        return scoreObj;
    }

    public ReadinessLevel classifyReadinessLevel(BigDecimal score) {
        if (score == null) return ReadinessLevel.UNKNOWN;
        if (score.compareTo(BAND_EXCELLENT) >= 0) {
            return ReadinessLevel.EXCELLENT;
        } else if (score.compareTo(BAND_GOOD) >= 0) {
            return ReadinessLevel.GOOD;
        } else if (score.compareTo(BAND_FAIR) >= 0) {
            return ReadinessLevel.FAIR;
        } else if (score.compareTo(BAND_POOR) >= 0) {
            return ReadinessLevel.POOR;
        } else {
            return ReadinessLevel.NOT_READY;
        }
    }

    private List<ReadinessFactor> generateReadinessFactors(
            BigDecimal score,
            RiskSummary riskSummary,
            String completeness
    ) {
        List<ReadinessFactor> factors = new ArrayList<>();

        if (score != null && score.compareTo(BAND_EXCELLENT) >= 0) {
            factors.add(new ReadinessFactor(
                    "Strong Release Readiness",
                    "Observed static analysis findings and risk profile indicate strong release readiness.",
                    "LOW",
                    "GENERAL"
            ));
        }

        if (riskSummary.getHighFindings() > 0) {
            factors.add(new ReadinessFactor(
                    "High Severity Findings Present",
                    riskSummary.getHighFindings() + " high-severity finding(s) detected across project workspace.",
                    "HIGH",
                    "GENERAL"
            ));
        }

        String riskLevelStr = riskSummary.getOverallRiskLevel() != null ? riskSummary.getOverallRiskLevel().name() : "LOW";
        if ("HIGH".equalsIgnoreCase(riskLevelStr) || "CRITICAL".equalsIgnoreCase(riskLevelStr)) {
            factors.add(new ReadinessFactor(
                    "Elevated Release Risk",
                    "Static risk calculation indicates elevated release risk.",
                    "HIGH",
                    "GENERAL"
            ));
        }

        if (riskSummary.getCategoryRisk() != null) {
            CategoryRisk secRisk = riskSummary.getCategoryRisk().get("SECURITY");
            if (secRisk != null && secRisk.getHighFindings() > 0) {
                factors.add(new ReadinessFactor(
                        "High-Security Findings Present",
                        secRisk.getHighFindings() + " high-severity security finding(s) detected in source code or configuration.",
                        "HIGH",
                        "SECURITY"
                ));
            }

            CategoryRisk testRisk = riskSummary.getCategoryRisk().get("TESTING");
            if (testRisk != null && testRisk.getFindingCount() > 0) {
                factors.add(new ReadinessFactor(
                        "Testing Gaps Detected",
                        testRisk.getFindingCount() + " static testing structure or coverage gap finding(s) detected.",
                        "MEDIUM",
                        "TESTING"
                ));
            }

            CategoryRisk depRisk = riskSummary.getCategoryRisk().get("DEPENDENCY");
            if (depRisk != null && depRisk.getFindingCount() > 0) {
                factors.add(new ReadinessFactor(
                        "Dependency Risk Detected",
                        depRisk.getFindingCount() + " static finding(s) detected in manifest dependency declarations.",
                        "MEDIUM",
                        "DEPENDENCY"
                ));
            }

            CategoryRisk perfRisk = riskSummary.getCategoryRisk().get("PERFORMANCE");
            if (perfRisk != null && perfRisk.getFindingCount() > 0) {
                factors.add(new ReadinessFactor(
                        "Performance Risk Detected",
                        perfRisk.getFindingCount() + " static performance smell(s) detected in source files.",
                        "MEDIUM",
                        "PERFORMANCE"
                ));
            }
        }

        if ("PARTIAL".equalsIgnoreCase(completeness)) {
            factors.add(new ReadinessFactor(
                    "Partial Analysis Coverage",
                    "Analysis coverage is partial; unanalyzed content may present additional risks.",
                    "MEDIUM",
                    "COMPLETENESS"
            ));
        } else if ("UNKNOWN".equalsIgnoreCase(completeness)) {
            factors.add(new ReadinessFactor(
                    "Unknown Analysis Coverage",
                    "Analysis coverage is unknown or unsupported, preventing reliable readiness scoring.",
                    "HIGH",
                    "COMPLETENESS"
            ));
        }

        return factors;
    }
}
