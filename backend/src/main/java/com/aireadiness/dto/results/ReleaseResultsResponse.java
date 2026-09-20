package com.aireadiness.dto.results;

import com.aireadiness.model.ReadinessScore;
import com.aireadiness.model.RiskSummary;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleaseResultsResponse {

    private ReleaseInfo releaseInfo;
    private AnalysisSummaryDto analysisSummary;
    private RiskSummary riskSummary;
    private ReadinessScore readinessScore;
    private AIReviewSummaryDto aiReviewSummary;
    private RecommendationSummaryDto recommendationSummary;
    private FindingSummaryDto findingSummary;

    public ReleaseResultsResponse() {
    }

    public ReleaseResultsResponse(
            ReleaseInfo releaseInfo,
            AnalysisSummaryDto analysisSummary,
            RiskSummary riskSummary,
            ReadinessScore readinessScore,
            AIReviewSummaryDto aiReviewSummary,
            RecommendationSummaryDto recommendationSummary,
            FindingSummaryDto findingSummary
    ) {
        this.releaseInfo = releaseInfo;
        this.analysisSummary = analysisSummary;
        this.riskSummary = riskSummary;
        this.readinessScore = readinessScore;
        this.aiReviewSummary = aiReviewSummary;
        this.recommendationSummary = recommendationSummary;
        this.findingSummary = findingSummary;
    }

    public static class ReleaseInfo {
        private String releaseId;
        private String projectId;
        private String projectName;
        private String version;
        private String name;
        private String description;
        private String status;
        private Instant createdAt;
        private Instant updatedAt;

        public ReleaseInfo() {
        }

        public ReleaseInfo(String releaseId, String projectId, String projectName, String version, String name, String description, String status, Instant createdAt, Instant updatedAt) {
            this.releaseId = releaseId;
            this.projectId = projectId;
            this.projectName = projectName;
            this.version = version;
            this.name = name;
            this.description = description;
            this.status = status;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Instant getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }

        public Instant getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
        }
    }

    public static class AnalysisSummaryDto {
        private String analysisId;
        private int runNumber;
        private String status;
        private String completeness = "NOT_AVAILABLE";
        private int totalFindings;
        private Map<String, Integer> findingsBySeverity = new HashMap<>();
        private Map<String, Integer> findingsByCategory = new HashMap<>();
        private int affectedFiles;
        private List<String> completedAnalyzers = new ArrayList<>();
        private List<String> failedAnalyzers = new ArrayList<>();
        private List<String> skippedAnalyzers = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();
        private Instant startedAt;
        private Instant completedAt;

        public AnalysisSummaryDto() {
        }

        public String getAnalysisId() {
            return analysisId;
        }

        public void setAnalysisId(String analysisId) {
            this.analysisId = analysisId;
        }

        public int getRunNumber() {
            return runNumber;
        }

        public void setRunNumber(int runNumber) {
            this.runNumber = runNumber;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCompleteness() {
            return completeness;
        }

        public void setCompleteness(String completeness) {
            this.completeness = completeness;
        }

        public int getTotalFindings() {
            return totalFindings;
        }

        public void setTotalFindings(int totalFindings) {
            this.totalFindings = totalFindings;
        }

        public Map<String, Integer> getFindingsBySeverity() {
            return findingsBySeverity;
        }

        public void setFindingsBySeverity(Map<String, Integer> findingsBySeverity) {
            this.findingsBySeverity = findingsBySeverity;
        }

        public Map<String, Integer> getFindingsByCategory() {
            return findingsByCategory;
        }

        public void setFindingsByCategory(Map<String, Integer> findingsByCategory) {
            this.findingsByCategory = findingsByCategory;
        }

        public int getAffectedFiles() {
            return affectedFiles;
        }

        public void setAffectedFiles(int affectedFiles) {
            this.affectedFiles = affectedFiles;
        }

        public List<String> getCompletedAnalyzers() {
            return completedAnalyzers;
        }

        public void setCompletedAnalyzers(List<String> completedAnalyzers) {
            this.completedAnalyzers = completedAnalyzers;
        }

        public List<String> getFailedAnalyzers() {
            return failedAnalyzers;
        }

        public void setFailedAnalyzers(List<String> failedAnalyzers) {
            this.failedAnalyzers = failedAnalyzers;
        }

        public List<String> getSkippedAnalyzers() {
            return skippedAnalyzers;
        }

        public void setSkippedAnalyzers(List<String> skippedAnalyzers) {
            this.skippedAnalyzers = skippedAnalyzers;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }

        public Instant getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(Instant startedAt) {
            this.startedAt = startedAt;
        }

        public Instant getCompletedAt() {
            return completedAt;
        }

        public void setCompletedAt(Instant completedAt) {
            this.completedAt = completedAt;
        }
    }

    public static class AIReviewSummaryDto {
        private int totalReviews;
        private int completedReviews;
        private int failedReviews;
        private int pendingReviews;
        private String overallStatus = "NO_REVIEWS";
        private String model;
        private String confidence;
        private String summary;
        private String failureCategory;
        private Instant latestReviewTimestamp;

        public AIReviewSummaryDto() {
        }

        public int getTotalReviews() {
            return totalReviews;
        }

        public void setTotalReviews(int totalReviews) {
            this.totalReviews = totalReviews;
        }

        public int getCompletedReviews() {
            return completedReviews;
        }

        public void setCompletedReviews(int completedReviews) {
            this.completedReviews = completedReviews;
        }

        public int getFailedReviews() {
            return failedReviews;
        }

        public void setFailedReviews(int failedReviews) {
            this.failedReviews = failedReviews;
        }

        public int getPendingReviews() {
            return pendingReviews;
        }

        public void setPendingReviews(int pendingReviews) {
            this.pendingReviews = pendingReviews;
        }

        public String getOverallStatus() {
            return overallStatus;
        }

        public void setOverallStatus(String overallStatus) {
            this.overallStatus = overallStatus;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getConfidence() {
            return confidence;
        }

        public void setConfidence(String confidence) {
            this.confidence = confidence;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getFailureCategory() {
            return failureCategory;
        }

        public void setFailureCategory(String failureCategory) {
            this.failureCategory = failureCategory;
        }

        public Instant getLatestReviewTimestamp() {
            return latestReviewTimestamp;
        }

        public void setLatestReviewTimestamp(Instant latestReviewTimestamp) {
            this.latestReviewTimestamp = latestReviewTimestamp;
        }
    }

    public static class RecommendationSummaryDto {
        private int totalRecommendations;
        private Map<String, Integer> countsByPriority = new HashMap<>();
        private Map<String, Integer> countsByStatus = new HashMap<>();
        private Map<String, Integer> countsByCategory = new HashMap<>();

        public RecommendationSummaryDto() {
        }

        public int getTotalRecommendations() {
            return totalRecommendations;
        }

        public void setTotalRecommendations(int totalRecommendations) {
            this.totalRecommendations = totalRecommendations;
        }

        public Map<String, Integer> getCountsByPriority() {
            return countsByPriority;
        }

        public void setCountsByPriority(Map<String, Integer> countsByPriority) {
            this.countsByPriority = countsByPriority;
        }

        public Map<String, Integer> getCountsByStatus() {
            return countsByStatus;
        }

        public void setCountsByStatus(Map<String, Integer> countsByStatus) {
            this.countsByStatus = countsByStatus;
        }

        public Map<String, Integer> getCountsByCategory() {
            return countsByCategory;
        }

        public void setCountsByCategory(Map<String, Integer> countsByCategory) {
            this.countsByCategory = countsByCategory;
        }
    }

    public static class FindingSummaryDto {
        private int totalFindings;
        private int highFindings;
        private int mediumFindings;
        private int lowFindings;
        private int infoFindings;
        private int affectedFilesCount;
        private Map<String, Integer> categoryBreakdown = new HashMap<>();
        private Map<String, Integer> severityBreakdown = new HashMap<>();

        public FindingSummaryDto() {
        }

        public int getTotalFindings() {
            return totalFindings;
        }

        public void setTotalFindings(int totalFindings) {
            this.totalFindings = totalFindings;
        }

        public int getHighFindings() {
            return highFindings;
        }

        public void setHighFindings(int highFindings) {
            this.highFindings = highFindings;
        }

        public int getMediumFindings() {
            return mediumFindings;
        }

        public void setMediumFindings(int mediumFindings) {
            this.mediumFindings = mediumFindings;
        }

        public int getLowFindings() {
            return lowFindings;
        }

        public void setLowFindings(int lowFindings) {
            this.lowFindings = lowFindings;
        }

        public int getInfoFindings() {
            return infoFindings;
        }

        public void setInfoFindings(int infoFindings) {
            this.infoFindings = infoFindings;
        }

        public int getAffectedFilesCount() {
            return affectedFilesCount;
        }

        public void setAffectedFilesCount(int affectedFilesCount) {
            this.affectedFilesCount = affectedFilesCount;
        }

        public Map<String, Integer> getCategoryBreakdown() {
            return categoryBreakdown;
        }

        public void setCategoryBreakdown(Map<String, Integer> categoryBreakdown) {
            this.categoryBreakdown = categoryBreakdown;
        }

        public Map<String, Integer> getSeverityBreakdown() {
            return severityBreakdown;
        }

        public void setSeverityBreakdown(Map<String, Integer> severityBreakdown) {
            this.severityBreakdown = severityBreakdown;
        }
    }

    public ReleaseInfo getReleaseInfo() {
        return releaseInfo;
    }

    public void setReleaseInfo(ReleaseInfo releaseInfo) {
        this.releaseInfo = releaseInfo;
    }

    public AnalysisSummaryDto getAnalysisSummary() {
        return analysisSummary;
    }

    public void setAnalysisSummary(AnalysisSummaryDto analysisSummary) {
        this.analysisSummary = analysisSummary;
    }

    public RiskSummary getRiskSummary() {
        return riskSummary;
    }

    public void setRiskSummary(RiskSummary riskSummary) {
        this.riskSummary = riskSummary;
    }

    public ReadinessScore getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(ReadinessScore readinessScore) {
        this.readinessScore = readinessScore;
    }

    public AIReviewSummaryDto getAiReviewSummary() {
        return aiReviewSummary;
    }

    public void setAiReviewSummary(AIReviewSummaryDto aiReviewSummary) {
        this.aiReviewSummary = aiReviewSummary;
    }

    public RecommendationSummaryDto getRecommendationSummary() {
        return recommendationSummary;
    }

    public void setRecommendationSummary(RecommendationSummaryDto recommendationSummary) {
        this.recommendationSummary = recommendationSummary;
    }

    public FindingSummaryDto getFindingSummary() {
        return findingSummary;
    }

    public void setFindingSummary(FindingSummaryDto findingSummary) {
        this.findingSummary = findingSummary;
    }
}
