package com.aireadiness.service;

import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.repository.AIReviewRepository;
import com.aireadiness.dto.comparison.VersionComparisonResponse;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.*;
import com.aireadiness.recommendation.model.Recommendation;
import com.aireadiness.recommendation.repository.RecommendationRepository;
import com.aireadiness.repository.AnalysisRepository;
import com.aireadiness.repository.ProjectRepository;
import com.aireadiness.repository.ReleaseRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VersionComparisonService {

    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final AnalysisRepository analysisRepository;
    private final AIReviewRepository aiReviewRepository;
    private final RecommendationRepository recommendationRepository;

    public VersionComparisonService(
            ReleaseRepository releaseRepository,
            ProjectRepository projectRepository,
            AnalysisRepository analysisRepository,
            AIReviewRepository aiReviewRepository,
            RecommendationRepository recommendationRepository
    ) {
        this.releaseRepository = releaseRepository;
        this.projectRepository = projectRepository;
        this.analysisRepository = analysisRepository;
        this.aiReviewRepository = aiReviewRepository;
        this.recommendationRepository = recommendationRepository;
    }

    public VersionComparisonResponse compareReleases(String projectId, String baseReleaseId, String targetReleaseId, String userId) {
        // 1. Validation: not null and not same release
        if (baseReleaseId == null || targetReleaseId == null || baseReleaseId.isBlank() || targetReleaseId.isBlank()) {
            throw new IllegalArgumentException("Base release ID and target release ID must be provided");
        }
        if (baseReleaseId.equals(targetReleaseId)) {
            throw new IllegalArgumentException("Base release and target release cannot be the same release");
        }

        // 2. Ownership & existence check
        Release baseRelease = releaseRepository.findByIdAndUserId(baseReleaseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + baseReleaseId));

        Release targetRelease = releaseRepository.findByIdAndUserId(targetReleaseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + targetReleaseId));

        // 3. Same project check
        if (!baseRelease.getProjectId().equals(projectId) || !targetRelease.getProjectId().equals(projectId) || !baseRelease.getProjectId().equals(targetRelease.getProjectId())) {
            throw new IllegalArgumentException("Releases belong to different projects or do not match the specified project");
        }

        String projectName = projectRepository.findByIdAndUserId(projectId, userId)
                .map(Project::getName)
                .orElse(null);

        // 4. Fetch latest analyses
        Optional<Analysis> optBaseAns = analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(baseReleaseId, userId);
        Optional<Analysis> optTargetAns = analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(targetReleaseId, userId);

        // 5. Release Overview
        VersionComparisonResponse.ReleaseComparisonOverview baseOverview = new VersionComparisonResponse.ReleaseComparisonOverview(
                baseRelease.getId(),
                baseRelease.getProjectId(),
                projectName,
                baseRelease.getVersion(),
                baseRelease.getName(),
                baseRelease.getStatus(),
                optBaseAns.map(Analysis::getId).orElse(null),
                optBaseAns.map(Analysis::getRunNumber).orElse(null),
                optBaseAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getCompleteness() : "NOT_AVAILABLE").orElse("NOT_AVAILABLE")
        );

        VersionComparisonResponse.ReleaseComparisonOverview targetOverview = new VersionComparisonResponse.ReleaseComparisonOverview(
                targetRelease.getId(),
                targetRelease.getProjectId(),
                projectName,
                targetRelease.getVersion(),
                targetRelease.getName(),
                targetRelease.getStatus(),
                optTargetAns.map(Analysis::getId).orElse(null),
                optTargetAns.map(Analysis::getRunNumber).orElse(null),
                optTargetAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getCompleteness() : "NOT_AVAILABLE").orElse("NOT_AVAILABLE")
        );

        // 6. Findings Summary Comparison
        int baseTotal = optBaseAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getTotalFindings() : (a.getFindings() != null ? a.getFindings().size() : 0)).orElse(0);
        int targetTotal = optTargetAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getTotalFindings() : (a.getFindings() != null ? a.getFindings().size() : 0)).orElse(0);
        VersionComparisonResponse.FindingsComparisonDto findingsComparison = new VersionComparisonResponse.FindingsComparisonDto(baseTotal, targetTotal, targetTotal - baseTotal);

        // 7. Severity Comparison
        Map<String, Integer> baseSev = extractSeverityMap(optBaseAns.orElse(null));
        Map<String, Integer> targetSev = extractSeverityMap(optTargetAns.orElse(null));
        Map<String, Integer> sevDeltas = new LinkedHashMap<>();
        Set<String> allSeverities = new LinkedHashSet<>(List.of("CRITICAL", "HIGH", "MEDIUM", "LOW", "INFO"));
        allSeverities.addAll(baseSev.keySet());
        allSeverities.addAll(targetSev.keySet());
        for (String sev : allSeverities) {
            int bCount = baseSev.getOrDefault(sev, 0);
            int tCount = targetSev.getOrDefault(sev, 0);
            sevDeltas.put(sev, tCount - bCount);
        }
        VersionComparisonResponse.SeverityComparisonDto severityComparison = new VersionComparisonResponse.SeverityComparisonDto(baseSev, targetSev, sevDeltas);

        // 8. Category Comparison
        Map<String, Integer> baseCat = extractCategoryMap(optBaseAns.orElse(null));
        Map<String, Integer> targetCat = extractCategoryMap(optTargetAns.orElse(null));
        Map<String, Integer> catDeltas = new LinkedHashMap<>();
        Set<String> allCats = new LinkedHashSet<>(baseCat.keySet());
        allCats.addAll(targetCat.keySet());
        for (String cat : allCats) {
            int bCount = baseCat.getOrDefault(cat, 0);
            int tCount = targetCat.getOrDefault(cat, 0);
            catDeltas.put(cat, tCount - bCount);
        }
        VersionComparisonResponse.CategoryComparisonDto categoryComparison = new VersionComparisonResponse.CategoryComparisonDto(baseCat, targetCat, catDeltas);

        // 9. Affected Files Comparison
        int baseFiles = optBaseAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getAffectedFiles() : extractAffectedFiles(a)).orElse(0);
        int targetFiles = optTargetAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getAffectedFiles() : extractAffectedFiles(a)).orElse(0);
        VersionComparisonResponse.AffectedFilesComparisonDto affectedFilesComparison = new VersionComparisonResponse.AffectedFilesComparisonDto(baseFiles, targetFiles, targetFiles - baseFiles);

        // 10. Risk Comparison (Persisted RiskSummary)
        RiskSummary baseRisk = optBaseAns.map(Analysis::getRiskSummary).orElse(null);
        RiskSummary targetRisk = optTargetAns.map(Analysis::getRiskSummary).orElse(null);
        String baseRiskLevel = baseRisk != null && baseRisk.getOverallRiskLevel() != null ? baseRisk.getOverallRiskLevel().name() : "UNKNOWN";
        String targetRiskLevel = targetRisk != null && targetRisk.getOverallRiskLevel() != null ? targetRisk.getOverallRiskLevel().name() : "UNKNOWN";
        BigDecimal basePts = baseRisk != null && baseRisk.getWeightedRiskPoints() != null ? baseRisk.getWeightedRiskPoints() : BigDecimal.ZERO;
        BigDecimal targetPts = targetRisk != null && targetRisk.getWeightedRiskPoints() != null ? targetRisk.getWeightedRiskPoints() : BigDecimal.ZERO;
        BigDecimal deltaPts = targetPts.subtract(basePts);
        VersionComparisonResponse.RiskComparisonDto riskComparison = new VersionComparisonResponse.RiskComparisonDto(baseRiskLevel, targetRiskLevel, basePts, targetPts, deltaPts);

        // 11. Readiness Comparison (Persisted ReadinessScore)
        ReadinessScore baseReadiness = optBaseAns.map(Analysis::getReadinessScore).orElse(null);
        ReadinessScore targetReadiness = optTargetAns.map(Analysis::getReadinessScore).orElse(null);
        BigDecimal baseScore = baseReadiness != null && baseReadiness.getReadinessScore() != null ? baseReadiness.getReadinessScore() : BigDecimal.ZERO;
        BigDecimal targetScore = targetReadiness != null && targetReadiness.getReadinessScore() != null ? targetReadiness.getReadinessScore() : BigDecimal.ZERO;
        BigDecimal scoreDelta = targetScore.subtract(baseScore);
        String baseLevel = baseReadiness != null && baseReadiness.getReadinessLevel() != null ? baseReadiness.getReadinessLevel().name() : "UNKNOWN";
        String targetLevel = targetReadiness != null && targetReadiness.getReadinessLevel() != null ? targetReadiness.getReadinessLevel().name() : "UNKNOWN";
        String baseConf = baseReadiness != null && baseReadiness.getConfidence() != null ? baseReadiness.getConfidence().name() : "UNKNOWN";
        String targetConf = targetReadiness != null && targetReadiness.getConfidence() != null ? targetReadiness.getConfidence().name() : "UNKNOWN";
        VersionComparisonResponse.ReadinessComparisonDto readinessComparison = new VersionComparisonResponse.ReadinessComparisonDto(baseScore, targetScore, scoreDelta, baseLevel, targetLevel, baseConf, targetConf);

        // 12. Recommendation Comparison
        VersionComparisonResponse.RecommendationComparisonDto recComparison = compareRecommendations(
                optBaseAns.map(Analysis::getId).orElse(null),
                optTargetAns.map(Analysis::getId).orElse(null)
        );

        // 13. AI Review Comparison
        VersionComparisonResponse.AIReviewComparisonDto aiReviewComparison = compareAIReviews(
                optBaseAns.map(Analysis::getId).orElse(null),
                optTargetAns.map(Analysis::getId).orElse(null)
        );

        // 14. Coverage Comparison
        VersionComparisonResponse.CoverageComparisonDto coverageComparison = new VersionComparisonResponse.CoverageComparisonDto();
        coverageComparison.setBaseCompleteness(optBaseAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getCompleteness() : "NOT_AVAILABLE").orElse("NOT_AVAILABLE"));
        coverageComparison.setTargetCompleteness(optTargetAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getCompleteness() : "NOT_AVAILABLE").orElse("NOT_AVAILABLE"));
        coverageComparison.setBaseCompletedAnalyzers(optBaseAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getCompletedAnalyzers() : Collections.<String>emptyList()).orElse(Collections.emptyList()));
        coverageComparison.setTargetCompletedAnalyzers(optTargetAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getCompletedAnalyzers() : Collections.<String>emptyList()).orElse(Collections.emptyList()));
        coverageComparison.setBaseFailedAnalyzers(optBaseAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getFailedAnalyzers() : Collections.<String>emptyList()).orElse(Collections.emptyList()));
        coverageComparison.setTargetFailedAnalyzers(optTargetAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getFailedAnalyzers() : Collections.<String>emptyList()).orElse(Collections.emptyList()));
        coverageComparison.setBaseSkippedAnalyzers(optBaseAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getSkippedAnalyzers() : Collections.<String>emptyList()).orElse(Collections.emptyList()));
        coverageComparison.setTargetSkippedAnalyzers(optTargetAns.map(a -> a.getUnifiedAnalysisSummary() != null ? a.getUnifiedAnalysisSummary().getSkippedAnalyzers() : Collections.<String>emptyList()).orElse(Collections.emptyList()));

        // 15. Finding-Level Changes (Traceable identity: ruleId + category + filePath + lineNumber)
        VersionComparisonResponse.FindingChangesDto findingChanges = compareFindingChanges(
                optBaseAns.map(Analysis::getFindings).orElse(Collections.emptyList()),
                optTargetAns.map(Analysis::getFindings).orElse(Collections.emptyList())
        );

        return new VersionComparisonResponse(
                baseOverview,
                targetOverview,
                findingsComparison,
                severityComparison,
                categoryComparison,
                affectedFilesComparison,
                riskComparison,
                readinessComparison,
                recComparison,
                aiReviewComparison,
                coverageComparison,
                findingChanges
        );
    }

    private Map<String, Integer> extractSeverityMap(Analysis analysis) {
        if (analysis == null) return Collections.emptyMap();
        if (analysis.getUnifiedAnalysisSummary() != null && analysis.getUnifiedAnalysisSummary().getFindingsBySeverity() != null) {
            return new LinkedHashMap<>(analysis.getUnifiedAnalysisSummary().getFindingsBySeverity());
        }
        if (analysis.getFindings() != null) {
            Map<String, Integer> map = new LinkedHashMap<>();
            for (Finding f : analysis.getFindings()) {
                if (f.getSeverity() != null) {
                    String s = f.getSeverity().toUpperCase();
                    map.put(s, map.getOrDefault(s, 0) + 1);
                }
            }
            return map;
        }
        return Collections.emptyMap();
    }

    private Map<String, Integer> extractCategoryMap(Analysis analysis) {
        if (analysis == null) return Collections.emptyMap();
        if (analysis.getUnifiedAnalysisSummary() != null && analysis.getUnifiedAnalysisSummary().getFindingsByCategory() != null) {
            return new LinkedHashMap<>(analysis.getUnifiedAnalysisSummary().getFindingsByCategory());
        }
        if (analysis.getFindings() != null) {
            Map<String, Integer> map = new LinkedHashMap<>();
            for (Finding f : analysis.getFindings()) {
                if (f.getCategory() != null) {
                    map.put(f.getCategory(), map.getOrDefault(f.getCategory(), 0) + 1);
                }
            }
            return map;
        }
        return Collections.emptyMap();
    }

    private int extractAffectedFiles(Analysis analysis) {
        if (analysis == null || analysis.getFindings() == null) return 0;
        Set<String> files = new HashSet<>();
        for (Finding f : analysis.getFindings()) {
            if (f.getFilePath() != null && !f.getFilePath().isBlank()) {
                files.add(f.getFilePath());
            }
        }
        return files.size();
    }

    private VersionComparisonResponse.RecommendationComparisonDto compareRecommendations(String baseAnsId, String targetAnsId) {
        List<Recommendation> baseRecs = baseAnsId != null ? recommendationRepository.findByAnalysisId(baseAnsId) : Collections.emptyList();
        List<Recommendation> targetRecs = targetAnsId != null ? recommendationRepository.findByAnalysisId(targetAnsId) : Collections.emptyList();

        VersionComparisonResponse.RecommendationComparisonDto dto = new VersionComparisonResponse.RecommendationComparisonDto();
        dto.setBaseTotal(baseRecs.size());
        dto.setTargetTotal(targetRecs.size());
        dto.setTotalDelta(targetRecs.size() - baseRecs.size());

        dto.setBasePriorityCounts(countRecsByPriority(baseRecs));
        dto.setTargetPriorityCounts(countRecsByPriority(targetRecs));
        dto.setBaseStatusCounts(countRecsByStatus(baseRecs));
        dto.setTargetStatusCounts(countRecsByStatus(targetRecs));
        dto.setBaseCategoryCounts(countRecsByCategory(baseRecs));
        dto.setTargetCategoryCounts(countRecsByCategory(targetRecs));

        return dto;
    }

    private Map<String, Integer> countRecsByPriority(List<Recommendation> recs) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Recommendation r : recs) {
            if (r.getPriority() != null) {
                String p = r.getPriority().name();
                map.put(p, map.getOrDefault(p, 0) + 1);
            }
        }
        return map;
    }

    private Map<String, Integer> countRecsByStatus(List<Recommendation> recs) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Recommendation r : recs) {
            if (r.getStatus() != null) {
                String s = r.getStatus().name();
                map.put(s, map.getOrDefault(s, 0) + 1);
            }
        }
        return map;
    }

    private Map<String, Integer> countRecsByCategory(List<Recommendation> recs) {
        Map<String, Integer> map = new LinkedHashMap<>();
        for (Recommendation r : recs) {
            if (r.getCategory() != null) {
                map.put(r.getCategory(), map.getOrDefault(r.getCategory(), 0) + 1);
            }
        }
        return map;
    }

    private VersionComparisonResponse.AIReviewComparisonDto compareAIReviews(String baseAnsId, String targetAnsId) {
        List<AIReview> baseReviews = baseAnsId != null ? aiReviewRepository.findByAnalysisId(baseAnsId) : Collections.emptyList();
        List<AIReview> targetReviews = targetAnsId != null ? aiReviewRepository.findByAnalysisId(targetAnsId) : Collections.emptyList();

        VersionComparisonResponse.AIReviewComparisonDto dto = new VersionComparisonResponse.AIReviewComparisonDto();
        dto.setBaseTotalReviews(baseReviews.size());
        dto.setTargetTotalReviews(targetReviews.size());
        dto.setBaseCompletedReviews((int) baseReviews.stream().filter(r -> r.getStatus() == com.aireadiness.aireview.model.AIReviewStatus.COMPLETED).count());
        dto.setTargetCompletedReviews((int) targetReviews.stream().filter(r -> r.getStatus() == com.aireadiness.aireview.model.AIReviewStatus.COMPLETED).count());

        dto.setBaseOverallStatus(baseReviews.isEmpty() ? "NO_REVIEWS" : (baseReviews.stream().anyMatch(r -> r.getStatus() == com.aireadiness.aireview.model.AIReviewStatus.FAILED) ? "FAILED" : "COMPLETED"));
        dto.setTargetOverallStatus(targetReviews.isEmpty() ? "NO_REVIEWS" : (targetReviews.stream().anyMatch(r -> r.getStatus() == com.aireadiness.aireview.model.AIReviewStatus.FAILED) ? "FAILED" : "COMPLETED"));

        dto.setBaseConfidence(baseReviews.stream().map(r -> r.getConfidence() != null ? r.getConfidence().name() : null).filter(Objects::nonNull).findFirst().orElse("UNKNOWN"));
        dto.setTargetConfidence(targetReviews.stream().map(r -> r.getConfidence() != null ? r.getConfidence().name() : null).filter(Objects::nonNull).findFirst().orElse("UNKNOWN"));

        return dto;
    }

    private VersionComparisonResponse.FindingChangesDto compareFindingChanges(List<Finding> baseFindings, List<Finding> targetFindings) {
        Map<String, Finding> baseMap = new LinkedHashMap<>();
        if (baseFindings != null) {
            for (Finding f : baseFindings) {
                String key = buildFindingKey(f);
                baseMap.putIfAbsent(key, f);
            }
        }

        Map<String, Finding> targetMap = new LinkedHashMap<>();
        if (targetFindings != null) {
            for (Finding f : targetFindings) {
                String key = buildFindingKey(f);
                targetMap.putIfAbsent(key, f);
            }
        }

        List<VersionComparisonResponse.FindingChangeItem> items = new ArrayList<>();
        int newCount = 0;
        int resolvedCount = 0;
        int unchangedCount = 0;

        // Process Target findings -> NEW or UNCHANGED
        for (Map.Entry<String, Finding> entry : targetMap.entrySet()) {
            String key = entry.getKey();
            Finding targetF = entry.getValue();
            if (baseMap.containsKey(key)) {
                unchangedCount++;
                items.add(new VersionComparisonResponse.FindingChangeItem(
                        targetF.getId(), targetF.getRuleId(), targetF.getCategory(),
                        targetF.getSeverity(), targetF.getTitle(), targetF.getFilePath(),
                        targetF.getLineNumber(), "UNCHANGED"
                ));
            } else {
                newCount++;
                items.add(new VersionComparisonResponse.FindingChangeItem(
                        targetF.getId(), targetF.getRuleId(), targetF.getCategory(),
                        targetF.getSeverity(), targetF.getTitle(), targetF.getFilePath(),
                        targetF.getLineNumber(), "NEW"
                ));
            }
        }

        // Process Base findings missing in Target -> RESOLVED
        for (Map.Entry<String, Finding> entry : baseMap.entrySet()) {
            String key = entry.getKey();
            Finding baseF = entry.getValue();
            if (!targetMap.containsKey(key)) {
                resolvedCount++;
                items.add(new VersionComparisonResponse.FindingChangeItem(
                        baseF.getId(), baseF.getRuleId(), baseF.getCategory(),
                        baseF.getSeverity(), baseF.getTitle(), baseF.getFilePath(),
                        baseF.getLineNumber(), "RESOLVED"
                ));
            }
        }

        // Deterministic sorting of items: NEW first, then RESOLVED, then UNCHANGED
        items.sort(Comparator.comparing(VersionComparisonResponse.FindingChangeItem::getChangeType)
                .thenComparing(f -> f.getSeverity() != null ? f.getSeverity() : "")
                .thenComparing(f -> f.getCategory() != null ? f.getCategory() : "")
                .thenComparing(f -> f.getRuleId() != null ? f.getRuleId() : ""));

        return new VersionComparisonResponse.FindingChangesDto(
                newCount,
                resolvedCount,
                unchangedCount,
                items,
                "Finding-level comparison uses stable composite keys (ruleId + category + filePath + lineNumber)."
        );
    }

    private String buildFindingKey(Finding f) {
        String r = f.getRuleId() != null ? f.getRuleId() : "UNKNOWN_RULE";
        String c = f.getCategory() != null ? f.getCategory() : "UNKNOWN_CAT";
        String p = f.getFilePath() != null ? f.getFilePath() : "";
        String l = f.getLineNumber() != null ? f.getLineNumber().toString() : "";
        return r + ":" + c + ":" + p + ":" + l;
    }
}
