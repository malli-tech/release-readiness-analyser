package com.aireadiness.risk;

import com.aireadiness.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class RiskEngine {

    public static final String CALCULATION_VERSION = "risk-v1";

    // Base Severity Weights (Part 14 Specification)
    public static final BigDecimal WEIGHT_HIGH = new BigDecimal("10.00");
    public static final BigDecimal WEIGHT_MEDIUM = new BigDecimal("5.00");
    public static final BigDecimal WEIGHT_LOW = new BigDecimal("1.00");
    public static final BigDecimal WEIGHT_INFO = new BigDecimal("0.00");

    // Category Multipliers (Part 14 Specification)
    public static final BigDecimal MULTIPLIER_SECURITY = new BigDecimal("1.50");
    public static final BigDecimal MULTIPLIER_DEPENDENCY = new BigDecimal("1.25");
    public static final BigDecimal MULTIPLIER_PERFORMANCE = new BigDecimal("1.10");
    public static final BigDecimal MULTIPLIER_TESTING = new BigDecimal("1.00");
    public static final BigDecimal MULTIPLIER_CODE_QUALITY = new BigDecimal("1.00");

    // Weighted Points Risk Thresholds (Part 14 Specification)
    // LOW: points < 10.00
    // MEDIUM: 10.00 <= points < 25.00
    // HIGH: 25.00 <= points < 50.00
    // CRITICAL: points >= 50.00
    public static final BigDecimal THRESHOLD_MEDIUM = new BigDecimal("10.00");
    public static final BigDecimal THRESHOLD_HIGH = new BigDecimal("25.00");
    public static final BigDecimal THRESHOLD_CRITICAL = new BigDecimal("50.00");

    public RiskSummary calculateRisk(UnifiedAnalysisSummary unifiedSummary, List<Finding> findings, ProjectProfile profile) {
        RiskSummary riskSummary = new RiskSummary();
        riskSummary.setCalculationVersion(CALCULATION_VERSION);

        Set<String> warningSet = new LinkedHashSet<>();
        if (unifiedSummary != null && unifiedSummary.getWarnings() != null) {
            warningSet.addAll(unifiedSummary.getWarnings());
        }

        String completeness = unifiedSummary != null ? unifiedSummary.getCompleteness() : "UNKNOWN";
        riskSummary.setCompleteness(completeness);

        // Check for unsupported project (no supported source code available)
        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            riskSummary.setOverallRiskLevel(RiskLevel.UNKNOWN);
            riskSummary.setWeightedRiskPoints(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            riskSummary.setBaseRiskPoints(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            warningSet.add("Risk could not be reliably determined because no supported source content was available for analysis.");
            riskSummary.setRiskWarnings(new ArrayList<>(warningSet));
            riskSummary.setRiskFactors(List.of(new RiskFactor(
                    "Unsupported Ecosystem",
                    "No supported source files or project manifests were available to evaluate release risk.",
                    "HIGH",
                    "GENERAL"
            )));
            return riskSummary;
        }

        List<Finding> validFindings = findings != null ? findings : Collections.emptyList();
        int total = validFindings.size();

        // 1. Calculate Base Points & Weighted Points per finding using BigDecimal (scale 2, HALF_UP)
        BigDecimal totalBasePoints = BigDecimal.ZERO;
        BigDecimal totalWeightedPoints = BigDecimal.ZERO;

        int highCount = 0, medCount = 0, lowCount = 0, infoCount = 0;
        int highSecurityCount = 0;

        Map<String, List<Finding>> categoryMap = new LinkedHashMap<>();
        categoryMap.put("CODE_QUALITY", new ArrayList<>());
        categoryMap.put("TESTING", new ArrayList<>());
        categoryMap.put("DEPENDENCY", new ArrayList<>());
        categoryMap.put("SECURITY", new ArrayList<>());
        categoryMap.put("PERFORMANCE", new ArrayList<>());

        for (Finding f : validFindings) {
            String sev = f.getSeverity() != null ? f.getSeverity().toUpperCase() : "MEDIUM";
            String cat = f.getCategory() != null ? f.getCategory().toUpperCase() : "CODE_QUALITY";
            if ("DEPENDENCIES".equals(cat)) cat = "DEPENDENCY";

            BigDecimal baseWeight = getSeverityWeight(sev);
            BigDecimal multiplier = getCategoryMultiplier(cat);
            BigDecimal weightedPoints = baseWeight.multiply(multiplier);

            totalBasePoints = totalBasePoints.add(baseWeight);
            totalWeightedPoints = totalWeightedPoints.add(weightedPoints);

            if ("HIGH".equals(sev) || "CRITICAL".equals(sev)) {
                highCount++;
                if ("SECURITY".equals(cat)) highSecurityCount++;
            } else if ("MEDIUM".equals(sev)) {
                medCount++;
            } else if ("LOW".equals(sev)) {
                lowCount++;
            } else if ("INFO".equals(sev)) {
                infoCount++;
            }

            categoryMap.computeIfAbsent(cat, k -> new ArrayList<>()).add(f);
        }

        totalBasePoints = totalBasePoints.setScale(2, RoundingMode.HALF_UP);
        totalWeightedPoints = totalWeightedPoints.setScale(2, RoundingMode.HALF_UP);

        riskSummary.setTotalFindings(total);
        riskSummary.setHighFindings(highCount);
        riskSummary.setMediumFindings(medCount);
        riskSummary.setLowFindings(lowCount);
        riskSummary.setInfoFindings(infoCount);
        riskSummary.setBaseRiskPoints(totalBasePoints);
        riskSummary.setWeightedRiskPoints(totalWeightedPoints);

        // 2. Classify Risk Level from Thresholds
        RiskLevel calculatedLevel = classifyRiskLevel(totalWeightedPoints);

        // 3. Critical Security Override: A HIGH-severity SECURITY finding forces overall risk to at least HIGH
        if (highSecurityCount >= 1) {
            if (calculatedLevel == RiskLevel.LOW || calculatedLevel == RiskLevel.MEDIUM) {
                calculatedLevel = RiskLevel.HIGH;
            }
        }

        // 4. Completeness Handling
        if ("UNKNOWN".equalsIgnoreCase(completeness)) {
            calculatedLevel = RiskLevel.UNKNOWN;
            warningSet.add("Risk assessment is marked UNKNOWN due to insufficient analysis coverage.");
        } else if ("PARTIAL".equalsIgnoreCase(completeness)) {
            warningSet.add("Risk calculation is based only on selected uploaded content. Additional unanalyzed files may contain further risk.");
        }

        // 5. Failed Analyzers Warning Handling
        if (unifiedSummary != null && unifiedSummary.getFailedAnalyzers() != null && !unifiedSummary.getFailedAnalyzers().isEmpty()) {
            warningSet.add("Risk calculation is based on incomplete analyzer coverage because one or more analyzers failed: " + String.join(", ", unifiedSummary.getFailedAnalyzers()));
        }

        riskSummary.setOverallRiskLevel(calculatedLevel);

        // 6. Calculate CategoryRisk Breakdown
        Map<String, CategoryRisk> categoryRiskMap = new LinkedHashMap<>();
        for (String catKey : List.of("CODE_QUALITY", "TESTING", "DEPENDENCY", "SECURITY", "PERFORMANCE")) {
            List<Finding> catFindings = categoryMap.getOrDefault(catKey, Collections.emptyList());
            CategoryRisk catRisk = calculateCategoryRisk(catKey, catFindings);
            categoryRiskMap.put(catKey, catRisk);
        }
        riskSummary.setCategoryRisk(categoryRiskMap);

        // 7. Calculate SeverityRisk Breakdown (base points contributed per severity level)
        Map<String, BigDecimal> severityRiskMap = new LinkedHashMap<>();
        severityRiskMap.put("HIGH", new BigDecimal(highCount).multiply(WEIGHT_HIGH).setScale(2, RoundingMode.HALF_UP));
        severityRiskMap.put("MEDIUM", new BigDecimal(medCount).multiply(WEIGHT_MEDIUM).setScale(2, RoundingMode.HALF_UP));
        severityRiskMap.put("LOW", new BigDecimal(lowCount).multiply(WEIGHT_LOW).setScale(2, RoundingMode.HALF_UP));
        severityRiskMap.put("INFO", new BigDecimal(infoCount).multiply(WEIGHT_INFO).setScale(2, RoundingMode.HALF_UP));
        riskSummary.setSeverityRisk(severityRiskMap);

        // 8. Generate Deterministic Risk Factors derived from actual data
        List<RiskFactor> factors = generateRiskFactors(highCount, highSecurityCount, totalWeightedPoints, categoryRiskMap, completeness);
        riskSummary.setRiskFactors(factors);

        riskSummary.setRiskWarnings(new ArrayList<>(warningSet));
        return riskSummary;
    }

    public RiskLevel classifyRiskLevel(BigDecimal points) {
        if (points == null) return RiskLevel.LOW;
        if (points.compareTo(THRESHOLD_CRITICAL) >= 0) {
            return RiskLevel.CRITICAL;
        } else if (points.compareTo(THRESHOLD_HIGH) >= 0) {
            return RiskLevel.HIGH;
        } else if (points.compareTo(THRESHOLD_MEDIUM) >= 0) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    /**
     * Maps finding severity to base weight.
     * Note: If CRITICAL findings exist in existing input data, they are treated consistently
     * with HIGH (weight = 10.00) because the Part 14 weighting model specifies no separate CRITICAL weight.
     */
    public BigDecimal getSeverityWeight(String severity) {
        if (severity == null) return WEIGHT_MEDIUM;
        switch (severity.toUpperCase()) {
            case "CRITICAL":
            case "HIGH":
                return WEIGHT_HIGH;
            case "MEDIUM":
                return WEIGHT_MEDIUM;
            case "LOW":
                return WEIGHT_LOW;
            case "INFO":
            default:
                return WEIGHT_INFO;
        }
    }

    public BigDecimal getCategoryMultiplier(String category) {
        if (category == null) return MULTIPLIER_CODE_QUALITY;
        switch (category.toUpperCase()) {
            case "SECURITY":
                return MULTIPLIER_SECURITY;
            case "DEPENDENCY":
            case "DEPENDENCIES":
                return MULTIPLIER_DEPENDENCY;
            case "PERFORMANCE":
                return MULTIPLIER_PERFORMANCE;
            case "TESTING":
                return MULTIPLIER_TESTING;
            case "CODE_QUALITY":
            default:
                return MULTIPLIER_CODE_QUALITY;
        }
    }

    private CategoryRisk calculateCategoryRisk(String category, List<Finding> findings) {
        CategoryRisk cr = new CategoryRisk(category);
        cr.setFindingCount(findings.size());

        int high = 0, med = 0, low = 0, info = 0;
        BigDecimal points = BigDecimal.ZERO;

        for (Finding f : findings) {
            String sev = f.getSeverity() != null ? f.getSeverity().toUpperCase() : "MEDIUM";
            BigDecimal w = getSeverityWeight(sev).multiply(getCategoryMultiplier(category));
            points = points.add(w);

            if ("HIGH".equals(sev) || "CRITICAL".equals(sev)) high++;
            else if ("MEDIUM".equals(sev)) med++;
            else if ("LOW".equals(sev)) low++;
            else if ("INFO".equals(sev)) info++;
        }

        points = points.setScale(2, RoundingMode.HALF_UP);
        cr.setHighFindings(high);
        cr.setMediumFindings(med);
        cr.setLowFindings(low);
        cr.setInfoFindings(info);
        cr.setWeightedRiskPoints(points);

        RiskLevel catLevel = classifyRiskLevel(points);
        if ("SECURITY".equalsIgnoreCase(category) && high >= 1) {
            if (catLevel == RiskLevel.LOW || catLevel == RiskLevel.MEDIUM) {
                catLevel = RiskLevel.HIGH;
            }
        }
        cr.setRiskLevel(catLevel);

        return cr;
    }

    private List<RiskFactor> generateRiskFactors(
            int totalHigh,
            int highSecurity,
            BigDecimal weightedPoints,
            Map<String, CategoryRisk> categoryRiskMap,
            String completeness
    ) {
        List<RiskFactor> factors = new ArrayList<>();

        if (highSecurity > 0) {
            factors.add(new RiskFactor(
                    "High Security Findings Detected",
                    highSecurity + " High-severity security finding(s) detected in source code or configuration.",
                    "HIGH",
                    "SECURITY"
            ));
        }

        if (totalHigh > 0) {
            factors.add(new RiskFactor(
                    "High Severity Issues Concentration",
                    totalHigh + " High-severity finding(s) detected across project workspace.",
                    "HIGH",
                    "GENERAL"
            ));
        }

        CategoryRisk depRisk = categoryRiskMap.get("DEPENDENCY");
        if (depRisk != null && depRisk.getFindingCount() > 0) {
            factors.add(new RiskFactor(
                    "Dependency Declarations Risk",
                    depRisk.getFindingCount() + " static finding(s) detected in manifest dependency declarations.",
                    depRisk.getHighFindings() > 0 ? "HIGH" : "MEDIUM",
                    "DEPENDENCY"
            ));
        }

        CategoryRisk perfRisk = categoryRiskMap.get("PERFORMANCE");
        if (perfRisk != null && perfRisk.getFindingCount() > 0) {
            factors.add(new RiskFactor(
                    "Static Performance Smells",
                    perfRisk.getFindingCount() + " static performance smell(s) detected in project source files.",
                    perfRisk.getHighFindings() > 0 ? "HIGH" : "MEDIUM",
                    "PERFORMANCE"
            ));
        }

        CategoryRisk testRisk = categoryRiskMap.get("TESTING");
        if (testRisk != null && testRisk.getFindingCount() > 0) {
            factors.add(new RiskFactor(
                    "Testing Coverage & Structure Risk",
                    testRisk.getFindingCount() + " static testing structure or coverage gap finding(s) detected.",
                    testRisk.getHighFindings() > 0 ? "HIGH" : "MEDIUM",
                    "TESTING"
            ));
        }

        if ("PARTIAL".equalsIgnoreCase(completeness)) {
            factors.add(new RiskFactor(
                    "Partial Upload Coverage",
                    "Selected-content upload mode means unuploaded files may contain additional unanalyzed risks.",
                    "MEDIUM",
                    "COMPLETENESS"
            ));
        } else if ("UNKNOWN".equalsIgnoreCase(completeness)) {
            factors.add(new RiskFactor(
                    "Incomplete Analysis Coverage",
                    "Analysis coverage is unknown or unsupported, preventing reliable risk quantification.",
                    "HIGH",
                    "COMPLETENESS"
            ));
        }

        if (factors.isEmpty()) {
            factors.add(new RiskFactor(
                    "Low Risk Profile",
                    "No major static risk factors or high-severity findings were detected in analyzed files.",
                    "LOW",
                    "GENERAL"
            ));
        }

        return factors;
    }
}
