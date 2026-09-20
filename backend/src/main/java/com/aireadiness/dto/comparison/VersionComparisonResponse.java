package com.aireadiness.dto.comparison;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VersionComparisonResponse {

    private ReleaseComparisonOverview baseRelease;
    private ReleaseComparisonOverview targetRelease;

    private FindingsComparisonDto findingsComparison;
    private SeverityComparisonDto severityComparison;
    private CategoryComparisonDto categoryComparison;
    private AffectedFilesComparisonDto affectedFilesComparison;
    private RiskComparisonDto riskComparison;
    private ReadinessComparisonDto readinessComparison;
    private RecommendationComparisonDto recommendationComparison;
    private AIReviewComparisonDto aiReviewComparison;
    private CoverageComparisonDto coverageComparison;
    private FindingChangesDto findingChanges;

    public VersionComparisonResponse() {
    }

    public VersionComparisonResponse(
            ReleaseComparisonOverview baseRelease,
            ReleaseComparisonOverview targetRelease,
            FindingsComparisonDto findingsComparison,
            SeverityComparisonDto severityComparison,
            CategoryComparisonDto categoryComparison,
            AffectedFilesComparisonDto affectedFilesComparison,
            RiskComparisonDto riskComparison,
            ReadinessComparisonDto readinessComparison,
            RecommendationComparisonDto recommendationComparison,
            AIReviewComparisonDto aiReviewComparison,
            CoverageComparisonDto coverageComparison,
            FindingChangesDto findingChanges
    ) {
        this.baseRelease = baseRelease;
        this.targetRelease = targetRelease;
        this.findingsComparison = findingsComparison;
        this.severityComparison = severityComparison;
        this.categoryComparison = categoryComparison;
        this.affectedFilesComparison = affectedFilesComparison;
        this.riskComparison = riskComparison;
        this.readinessComparison = readinessComparison;
        this.recommendationComparison = recommendationComparison;
        this.aiReviewComparison = aiReviewComparison;
        this.coverageComparison = coverageComparison;
        this.findingChanges = findingChanges;
    }

    public static class ReleaseComparisonOverview {
        private String releaseId;
        private String projectId;
        private String projectName;
        private String version;
        private String name;
        private String status;
        private String analysisId;
        private Integer runNumber;
        private String completeness;

        public ReleaseComparisonOverview() {
        }

        public ReleaseComparisonOverview(String releaseId, String projectId, String projectName, String version, String name, String status, String analysisId, Integer runNumber, String completeness) {
            this.releaseId = releaseId;
            this.projectId = projectId;
            this.projectName = projectName;
            this.version = version;
            this.name = name;
            this.status = status;
            this.analysisId = analysisId;
            this.runNumber = runNumber;
            this.completeness = completeness;
        }

        public String getReleaseId() {
            return releaseId;
        }

        public void setReleaseId(String releaseId) {
            this.releaseId = releaseId;
        }

        public String getProjectId() {
            return projectId;
        }

        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }

        public String getProjectName() {
            return projectName;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getAnalysisId() {
            return analysisId;
        }

        public void setAnalysisId(String analysisId) {
            this.analysisId = analysisId;
        }

        public Integer getRunNumber() {
            return runNumber;
        }

        public void setRunNumber(Integer runNumber) {
            this.runNumber = runNumber;
        }

        public String getCompleteness() {
            return completeness;
        }

        public void setCompleteness(String completeness) {
            this.completeness = completeness;
        }
    }

    public static class FindingsComparisonDto {
        private int baseTotal;
        private int targetTotal;
        private int delta;

        public FindingsComparisonDto() {
        }

        public FindingsComparisonDto(int baseTotal, int targetTotal, int delta) {
            this.baseTotal = baseTotal;
            this.targetTotal = targetTotal;
            this.delta = delta;
        }

        public int getBaseTotal() {
            return baseTotal;
        }

        public void setBaseTotal(int baseTotal) {
            this.baseTotal = baseTotal;
        }

        public int getTargetTotal() {
            return targetTotal;
        }

        public void setTargetTotal(int targetTotal) {
            this.targetTotal = targetTotal;
        }

        public int getDelta() {
            return delta;
        }

        public void setDelta(int delta) {
            this.delta = delta;
        }
    }

    public static class SeverityComparisonDto {
        private Map<String, Integer> baseCounts = new LinkedHashMap<>();
        private Map<String, Integer> targetCounts = new LinkedHashMap<>();
        private Map<String, Integer> deltas = new LinkedHashMap<>();

        public SeverityComparisonDto() {
        }

        public SeverityComparisonDto(Map<String, Integer> baseCounts, Map<String, Integer> targetCounts, Map<String, Integer> deltas) {
            this.baseCounts = baseCounts;
            this.targetCounts = targetCounts;
            this.deltas = deltas;
        }

        public Map<String, Integer> getBaseCounts() {
            return baseCounts;
        }

        public void setBaseCounts(Map<String, Integer> baseCounts) {
            this.baseCounts = baseCounts;
        }

        public Map<String, Integer> getTargetCounts() {
            return targetCounts;
        }

        public void setTargetCounts(Map<String, Integer> targetCounts) {
            this.targetCounts = targetCounts;
        }

        public Map<String, Integer> getDeltas() {
            return deltas;
        }

        public void setDeltas(Map<String, Integer> deltas) {
            this.deltas = deltas;
        }
    }

    public static class CategoryComparisonDto {
        private Map<String, Integer> baseCounts = new LinkedHashMap<>();
        private Map<String, Integer> targetCounts = new LinkedHashMap<>();
        private Map<String, Integer> deltas = new LinkedHashMap<>();

        public CategoryComparisonDto() {
        }

        public CategoryComparisonDto(Map<String, Integer> baseCounts, Map<String, Integer> targetCounts, Map<String, Integer> deltas) {
            this.baseCounts = baseCounts;
            this.targetCounts = targetCounts;
            this.deltas = deltas;
        }

        public Map<String, Integer> getBaseCounts() {
            return baseCounts;
        }

        public void setBaseCounts(Map<String, Integer> baseCounts) {
            this.baseCounts = baseCounts;
        }

        public Map<String, Integer> getTargetCounts() {
            return targetCounts;
        }

        public void setTargetCounts(Map<String, Integer> targetCounts) {
            this.targetCounts = targetCounts;
        }

        public Map<String, Integer> getDeltas() {
            return deltas;
        }

        public void setDeltas(Map<String, Integer> deltas) {
            this.deltas = deltas;
        }
    }

    public static class AffectedFilesComparisonDto {
        private int baseAffectedFiles;
        private int targetAffectedFiles;
        private int delta;

        public AffectedFilesComparisonDto() {
        }

        public AffectedFilesComparisonDto(int baseAffectedFiles, int targetAffectedFiles, int delta) {
            this.baseAffectedFiles = baseAffectedFiles;
            this.targetAffectedFiles = targetAffectedFiles;
            this.delta = delta;
        }

        public int getBaseAffectedFiles() {
            return baseAffectedFiles;
        }

        public void setBaseAffectedFiles(int baseAffectedFiles) {
            this.baseAffectedFiles = baseAffectedFiles;
        }

        public int getTargetAffectedFiles() {
            return targetAffectedFiles;
        }

        public void setTargetAffectedFiles(int targetAffectedFiles) {
            this.targetAffectedFiles = targetAffectedFiles;
        }

        public int getDelta() {
            return delta;
        }

        public void setDelta(int delta) {
            this.delta = delta;
        }
    }

    public static class RiskComparisonDto {
        private String baseRiskLevel;
        private String targetRiskLevel;
        private BigDecimal baseWeightedRiskPoints;
        private BigDecimal targetWeightedRiskPoints;
        private BigDecimal deltaWeightedRiskPoints;

        public RiskComparisonDto() {
        }

        public RiskComparisonDto(String baseRiskLevel, String targetRiskLevel, BigDecimal baseWeightedRiskPoints, BigDecimal targetWeightedRiskPoints, BigDecimal deltaWeightedRiskPoints) {
            this.baseRiskLevel = baseRiskLevel;
            this.targetRiskLevel = targetRiskLevel;
            this.baseWeightedRiskPoints = baseWeightedRiskPoints;
            this.targetWeightedRiskPoints = targetWeightedRiskPoints;
            this.deltaWeightedRiskPoints = deltaWeightedRiskPoints;
        }

        public String getBaseRiskLevel() {
            return baseRiskLevel;
        }

        public void setBaseRiskLevel(String baseRiskLevel) {
            this.baseRiskLevel = baseRiskLevel;
        }

        public String getTargetRiskLevel() {
            return targetRiskLevel;
        }

        public void setTargetRiskLevel(String targetRiskLevel) {
            this.targetRiskLevel = targetRiskLevel;
        }

        public BigDecimal getBaseWeightedRiskPoints() {
            return baseWeightedRiskPoints;
        }

        public void setBaseWeightedRiskPoints(BigDecimal baseWeightedRiskPoints) {
            this.baseWeightedRiskPoints = baseWeightedRiskPoints;
        }

        public BigDecimal getTargetWeightedRiskPoints() {
            return targetWeightedRiskPoints;
        }

        public void setTargetWeightedRiskPoints(BigDecimal targetWeightedRiskPoints) {
            this.targetWeightedRiskPoints = targetWeightedRiskPoints;
        }

        public BigDecimal getDeltaWeightedRiskPoints() {
            return deltaWeightedRiskPoints;
        }

        public void setDeltaWeightedRiskPoints(BigDecimal deltaWeightedRiskPoints) {
            this.deltaWeightedRiskPoints = deltaWeightedRiskPoints;
        }
    }

    public static class ReadinessComparisonDto {
        private BigDecimal baseScore;
        private BigDecimal targetScore;
        private BigDecimal scoreDelta;
        private String baseLevel;
        private String targetLevel;
        private String baseConfidence;
        private String targetConfidence;

        public ReadinessComparisonDto() {
        }

        public ReadinessComparisonDto(BigDecimal baseScore, BigDecimal targetScore, BigDecimal scoreDelta, String baseLevel, String targetLevel, String baseConfidence, String targetConfidence) {
            this.baseScore = baseScore;
            this.targetScore = targetScore;
            this.scoreDelta = scoreDelta;
            this.baseLevel = baseLevel;
            this.targetLevel = targetLevel;
            this.baseConfidence = baseConfidence;
            this.targetConfidence = targetConfidence;
        }

        public BigDecimal getBaseScore() {
            return baseScore;
        }

        public void setBaseScore(BigDecimal baseScore) {
            this.baseScore = baseScore;
        }

        public BigDecimal getTargetScore() {
            return targetScore;
        }

        public void setTargetScore(BigDecimal targetScore) {
            this.targetScore = targetScore;
        }

        public BigDecimal getScoreDelta() {
            return scoreDelta;
        }

        public void setScoreDelta(BigDecimal scoreDelta) {
            this.scoreDelta = scoreDelta;
        }

        public String getBaseLevel() {
            return baseLevel;
        }

        public void setBaseLevel(String baseLevel) {
            this.baseLevel = baseLevel;
        }

        public String getTargetLevel() {
            return targetLevel;
        }

        public void setTargetLevel(String targetLevel) {
            this.targetLevel = targetLevel;
        }

        public String getBaseConfidence() {
            return baseConfidence;
        }

        public void setBaseConfidence(String baseConfidence) {
            this.baseConfidence = baseConfidence;
        }

        public String getTargetConfidence() {
            return targetConfidence;
        }

        public void setTargetConfidence(String targetConfidence) {
            this.targetConfidence = targetConfidence;
        }
    }

    public static class RecommendationComparisonDto {
        private int baseTotal;
        private int targetTotal;
        private int totalDelta;
        private Map<String, Integer> basePriorityCounts = new LinkedHashMap<>();
        private Map<String, Integer> targetPriorityCounts = new LinkedHashMap<>();
        private Map<String, Integer> baseStatusCounts = new LinkedHashMap<>();
        private Map<String, Integer> targetStatusCounts = new LinkedHashMap<>();
        private Map<String, Integer> baseCategoryCounts = new LinkedHashMap<>();
        private Map<String, Integer> targetCategoryCounts = new LinkedHashMap<>();

        public RecommendationComparisonDto() {
        }

        public int getBaseTotal() {
            return baseTotal;
        }

        public void setBaseTotal(int baseTotal) {
            this.baseTotal = baseTotal;
        }

        public int getTargetTotal() {
            return targetTotal;
        }

        public void setTargetTotal(int targetTotal) {
            this.targetTotal = targetTotal;
        }

        public int getTotalDelta() {
            return totalDelta;
        }

        public void setTotalDelta(int totalDelta) {
            this.totalDelta = totalDelta;
        }

        public Map<String, Integer> getBasePriorityCounts() {
            return basePriorityCounts;
        }

        public void setBasePriorityCounts(Map<String, Integer> basePriorityCounts) {
            this.basePriorityCounts = basePriorityCounts;
        }

        public Map<String, Integer> getTargetPriorityCounts() {
            return targetPriorityCounts;
        }

        public void setTargetPriorityCounts(Map<String, Integer> targetPriorityCounts) {
            this.targetPriorityCounts = targetPriorityCounts;
        }

        public Map<String, Integer> getBaseStatusCounts() {
            return baseStatusCounts;
        }

        public void setBaseStatusCounts(Map<String, Integer> baseStatusCounts) {
            this.baseStatusCounts = baseStatusCounts;
        }

        public Map<String, Integer> getTargetStatusCounts() {
            return targetStatusCounts;
        }

        public void setTargetStatusCounts(Map<String, Integer> targetStatusCounts) {
            this.targetStatusCounts = targetStatusCounts;
        }

        public Map<String, Integer> getBaseCategoryCounts() {
            return baseCategoryCounts;
        }

        public void setBaseCategoryCounts(Map<String, Integer> baseCategoryCounts) {
            this.baseCategoryCounts = baseCategoryCounts;
        }

        public Map<String, Integer> getTargetCategoryCounts() {
            return targetCategoryCounts;
        }

        public void setTargetCategoryCounts(Map<String, Integer> targetCategoryCounts) {
            this.targetCategoryCounts = targetCategoryCounts;
        }
    }

    public static class AIReviewComparisonDto {
        private String baseOverallStatus;
        private String targetOverallStatus;
        private String baseConfidence;
        private String targetConfidence;
        private int baseTotalReviews;
        private int targetTotalReviews;
        private int baseCompletedReviews;
        private int targetCompletedReviews;

        public AIReviewComparisonDto() {
        }

        public String getBaseOverallStatus() {
            return baseOverallStatus;
        }

        public void setBaseOverallStatus(String baseOverallStatus) {
            this.baseOverallStatus = baseOverallStatus;
        }

        public String getTargetOverallStatus() {
            return targetOverallStatus;
        }

        public void setTargetOverallStatus(String targetOverallStatus) {
            this.targetOverallStatus = targetOverallStatus;
        }

        public String getBaseConfidence() {
            return baseConfidence;
        }

        public void setBaseConfidence(String baseConfidence) {
            this.baseConfidence = baseConfidence;
        }

        public String getTargetConfidence() {
            return targetConfidence;
        }

        public void setTargetConfidence(String targetConfidence) {
            this.targetConfidence = targetConfidence;
        }

        public int getBaseTotalReviews() {
            return baseTotalReviews;
        }

        public void setBaseTotalReviews(int baseTotalReviews) {
            this.baseTotalReviews = baseTotalReviews;
        }

        public int getTargetTotalReviews() {
            return targetTotalReviews;
        }

        public void setTargetTotalReviews(int targetTotalReviews) {
            this.targetTotalReviews = targetTotalReviews;
        }

        public int getBaseCompletedReviews() {
            return baseCompletedReviews;
        }

        public void setBaseCompletedReviews(int baseCompletedReviews) {
            this.baseCompletedReviews = baseCompletedReviews;
        }

        public int getTargetCompletedReviews() {
            return targetCompletedReviews;
        }

        public void setTargetCompletedReviews(int targetCompletedReviews) {
            this.targetCompletedReviews = targetCompletedReviews;
        }
    }

    public static class CoverageComparisonDto {
        private String baseCompleteness;
        private String targetCompleteness;
        private List<String> baseCompletedAnalyzers = new ArrayList<>();
        private List<String> targetCompletedAnalyzers = new ArrayList<>();
        private List<String> baseFailedAnalyzers = new ArrayList<>();
        private List<String> targetFailedAnalyzers = new ArrayList<>();
        private List<String> baseSkippedAnalyzers = new ArrayList<>();
        private List<String> targetSkippedAnalyzers = new ArrayList<>();

        public CoverageComparisonDto() {
        }

        public String getBaseCompleteness() {
            return baseCompleteness;
        }

        public void setBaseCompleteness(String baseCompleteness) {
            this.baseCompleteness = baseCompleteness;
        }

        public String getTargetCompleteness() {
            return targetCompleteness;
        }

        public void setTargetCompleteness(String targetCompleteness) {
            this.targetCompleteness = targetCompleteness;
        }

        public List<String> getBaseCompletedAnalyzers() {
            return baseCompletedAnalyzers;
        }

        public void setBaseCompletedAnalyzers(List<String> baseCompletedAnalyzers) {
            this.baseCompletedAnalyzers = baseCompletedAnalyzers;
        }

        public List<String> getTargetCompletedAnalyzers() {
            return targetCompletedAnalyzers;
        }

        public void setTargetCompletedAnalyzers(List<String> targetCompletedAnalyzers) {
            this.targetCompletedAnalyzers = targetCompletedAnalyzers;
        }

        public List<String> getBaseFailedAnalyzers() {
            return baseFailedAnalyzers;
        }

        public void setBaseFailedAnalyzers(List<String> baseFailedAnalyzers) {
            this.baseFailedAnalyzers = baseFailedAnalyzers;
        }

        public List<String> getTargetFailedAnalyzers() {
            return targetFailedAnalyzers;
        }

        public void setTargetFailedAnalyzers(List<String> targetFailedAnalyzers) {
            this.targetFailedAnalyzers = targetFailedAnalyzers;
        }

        public List<String> getBaseSkippedAnalyzers() {
            return baseSkippedAnalyzers;
        }

        public void setBaseSkippedAnalyzers(List<String> baseSkippedAnalyzers) {
            this.baseSkippedAnalyzers = baseSkippedAnalyzers;
        }

        public List<String> getTargetSkippedAnalyzers() {
            return targetSkippedAnalyzers;
        }

        public void setTargetSkippedAnalyzers(List<String> targetSkippedAnalyzers) {
            this.targetSkippedAnalyzers = targetSkippedAnalyzers;
        }
    }

    public static class FindingChangeItem {
        private String findingId;
        private String ruleId;
        private String category;
        private String severity;
        private String title;
        private String filePath;
        private Integer lineNumber;
        private String changeType; // NEW, RESOLVED, UNCHANGED

        public FindingChangeItem() {
        }

        public FindingChangeItem(String findingId, String ruleId, String category, String severity, String title, String filePath, Integer lineNumber, String changeType) {
            this.findingId = findingId;
            this.ruleId = ruleId;
            this.category = category;
            this.severity = severity;
            this.title = title;
            this.filePath = filePath;
            this.lineNumber = lineNumber;
            this.changeType = changeType;
        }

        public String getFindingId() {
            return findingId;
        }

        public void setFindingId(String findingId) {
            this.findingId = findingId;
        }

        public String getRuleId() {
            return ruleId;
        }

        public void setRuleId(String ruleId) {
            this.ruleId = ruleId;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getSeverity() {
            return severity;
        }

        public void setSeverity(String severity) {
            this.severity = severity;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public Integer getLineNumber() {
            return lineNumber;
        }

        public void setLineNumber(Integer lineNumber) {
            this.lineNumber = lineNumber;
        }

        public String getChangeType() {
            return changeType;
        }

        public void setChangeType(String changeType) {
            this.changeType = changeType;
        }
    }

    public static class FindingChangesDto {
        private int newCount;
        private int resolvedCount;
        private int unchangedCount;
        private List<FindingChangeItem> items = new ArrayList<>();
        private String limitationNote;

        public FindingChangesDto() {
        }

        public FindingChangesDto(int newCount, int resolvedCount, int unchangedCount, List<FindingChangeItem> items, String limitationNote) {
            this.newCount = newCount;
            this.resolvedCount = resolvedCount;
            this.unchangedCount = unchangedCount;
            this.items = items;
            this.limitationNote = limitationNote;
        }

        public int getNewCount() {
            return newCount;
        }

        public void setNewCount(int newCount) {
            this.newCount = newCount;
        }

        public int getResolvedCount() {
            return resolvedCount;
        }

        public void setResolvedCount(int resolvedCount) {
            this.resolvedCount = resolvedCount;
        }

        public int getUnchangedCount() {
            return unchangedCount;
        }

        public void setUnchangedCount(int unchangedCount) {
            this.unchangedCount = unchangedCount;
        }

        public List<FindingChangeItem> getItems() {
            return items;
        }

        public void setItems(List<FindingChangeItem> items) {
            this.items = items;
        }

        public String getLimitationNote() {
            return limitationNote;
        }

        public void setLimitationNote(String limitationNote) {
            this.limitationNote = limitationNote;
        }
    }

    public ReleaseComparisonOverview getBaseRelease() {
        return baseRelease;
    }

    public void setBaseRelease(ReleaseComparisonOverview baseRelease) {
        this.baseRelease = baseRelease;
    }

    public ReleaseComparisonOverview getTargetRelease() {
        return targetRelease;
    }

    public void setTargetRelease(ReleaseComparisonOverview targetRelease) {
        this.targetRelease = targetRelease;
    }

    public FindingsComparisonDto getFindingsComparison() {
        return findingsComparison;
    }

    public void setFindingsComparison(FindingsComparisonDto findingsComparison) {
        this.findingsComparison = findingsComparison;
    }

    public SeverityComparisonDto getSeverityComparison() {
        return severityComparison;
    }

    public void setSeverityComparison(SeverityComparisonDto severityComparison) {
        this.severityComparison = severityComparison;
    }

    public CategoryComparisonDto getCategoryComparison() {
        return categoryComparison;
    }

    public void setCategoryComparison(CategoryComparisonDto categoryComparison) {
        this.categoryComparison = categoryComparison;
    }

    public AffectedFilesComparisonDto getAffectedFilesComparison() {
        return affectedFilesComparison;
    }

    public void setAffectedFilesComparison(AffectedFilesComparisonDto affectedFilesComparison) {
        this.affectedFilesComparison = affectedFilesComparison;
    }

    public RiskComparisonDto getRiskComparison() {
        return riskComparison;
    }

    public void setRiskComparison(RiskComparisonDto riskComparison) {
        this.riskComparison = riskComparison;
    }

    public ReadinessComparisonDto getReadinessComparison() {
        return readinessComparison;
    }

    public void setReadinessComparison(ReadinessComparisonDto readinessComparison) {
        this.readinessComparison = readinessComparison;
    }

    public RecommendationComparisonDto getRecommendationComparison() {
        return recommendationComparison;
    }

    public void setRecommendationComparison(RecommendationComparisonDto recommendationComparison) {
        this.recommendationComparison = recommendationComparison;
    }

    public AIReviewComparisonDto getAiReviewComparison() {
        return aiReviewComparison;
    }

    public void setAiReviewComparison(AIReviewComparisonDto aiReviewComparison) {
        this.aiReviewComparison = aiReviewComparison;
    }

    public CoverageComparisonDto getCoverageComparison() {
        return coverageComparison;
    }

    public void setCoverageComparison(CoverageComparisonDto coverageComparison) {
        this.coverageComparison = coverageComparison;
    }

    public FindingChangesDto getFindingChanges() {
        return findingChanges;
    }

    public void setFindingChanges(FindingChangesDto findingChanges) {
        this.findingChanges = findingChanges;
    }
}
