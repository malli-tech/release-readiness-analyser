package com.aireadiness.dto.analysis;

import com.aireadiness.model.AnalysisPlan;
import com.aireadiness.model.DependencySummary;
import com.aireadiness.model.Finding;
import com.aireadiness.model.PerformanceSummary;
import com.aireadiness.model.ProjectProfile;
import com.aireadiness.model.ReadinessScore;
import com.aireadiness.model.RiskSummary;
import com.aireadiness.model.SecuritySummary;
import com.aireadiness.model.TestingSummary;
import com.aireadiness.model.UnifiedAnalysisSummary;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class AnalysisResponse {

    private String id;
    private String projectId;
    private String releaseId;
    private int runNumber;
    private String status;
    private Instant startedAt;
    private Instant completedAt;
    private ProjectProfile projectProfile;
    private AnalysisPlan analysisPlan;
    private List<Finding> findings;
    private Map<String, Double> categoryScores;
    private RiskSummary riskSummary;
    private ReadinessScore readinessScore;
    private List<String> warnings;
    private TestingSummary testingSummary;
    private DependencySummary dependencySummary;
    private SecuritySummary securitySummary;
    private PerformanceSummary performanceSummary;
    private String message;

    public AnalysisResponse() {
    }

    public AnalysisResponse(String id, String projectId, String releaseId, int runNumber, String status, Instant startedAt, Instant completedAt, ProjectProfile projectProfile, AnalysisPlan analysisPlan, List<Finding> findings, Map<String, Double> categoryScores, ReadinessScore readinessScore, List<String> warnings, String message) {
        this.id = id;
        this.projectId = projectId;
        this.releaseId = releaseId;
        this.runNumber = runNumber;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.projectProfile = projectProfile;
        this.analysisPlan = analysisPlan;
        this.findings = findings;
        this.categoryScores = categoryScores;
        this.readinessScore = readinessScore;
        this.warnings = warnings;
        this.message = message;
    }

    public AnalysisResponse(String id, String projectId, String releaseId, int runNumber, String status, Instant startedAt, Instant completedAt, ProjectProfile projectProfile, AnalysisPlan analysisPlan, List<Finding> findings, Map<String, Double> categoryScores, ReadinessScore readinessScore, List<String> warnings, TestingSummary testingSummary, String message) {
        this(id, projectId, releaseId, runNumber, status, startedAt, completedAt, projectProfile, analysisPlan, findings, categoryScores, readinessScore, warnings, message);
        this.testingSummary = testingSummary;
    }

    public AnalysisResponse(String id, String projectId, String releaseId, int runNumber, String status, Instant startedAt, Instant completedAt, ProjectProfile projectProfile, AnalysisPlan analysisPlan, List<Finding> findings, Map<String, Double> categoryScores, ReadinessScore readinessScore, List<String> warnings, TestingSummary testingSummary, DependencySummary dependencySummary, String message) {
        this(id, projectId, releaseId, runNumber, status, startedAt, completedAt, projectProfile, analysisPlan, findings, categoryScores, readinessScore, warnings, testingSummary, message);
        this.dependencySummary = dependencySummary;
    }

    public AnalysisResponse(String id, String projectId, String releaseId, int runNumber, String status, Instant startedAt, Instant completedAt, ProjectProfile projectProfile, AnalysisPlan analysisPlan, List<Finding> findings, Map<String, Double> categoryScores, ReadinessScore readinessScore, List<String> warnings, TestingSummary testingSummary, DependencySummary dependencySummary, SecuritySummary securitySummary, String message) {
        this(id, projectId, releaseId, runNumber, status, startedAt, completedAt, projectProfile, analysisPlan, findings, categoryScores, readinessScore, warnings, testingSummary, dependencySummary, message);
        this.securitySummary = securitySummary;
    }

    public AnalysisResponse(String id, String projectId, String releaseId, int runNumber, String status, Instant startedAt, Instant completedAt, ProjectProfile projectProfile, AnalysisPlan analysisPlan, List<Finding> findings, Map<String, Double> categoryScores, ReadinessScore readinessScore, List<String> warnings, TestingSummary testingSummary, DependencySummary dependencySummary, SecuritySummary securitySummary, PerformanceSummary performanceSummary, String message) {
        this(id, projectId, releaseId, runNumber, status, startedAt, completedAt, projectProfile, analysisPlan, findings, categoryScores, readinessScore, warnings, testingSummary, dependencySummary, securitySummary, message);
        this.performanceSummary = performanceSummary;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
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

    public ProjectProfile getProjectProfile() {
        return projectProfile;
    }

    public void setProjectProfile(ProjectProfile projectProfile) {
        this.projectProfile = projectProfile;
    }

    public AnalysisPlan getAnalysisPlan() {
        return analysisPlan;
    }

    public void setAnalysisPlan(AnalysisPlan analysisPlan) {
        this.analysisPlan = analysisPlan;
    }

    public List<Finding> getFindings() {
        return findings;
    }

    public void setFindings(List<Finding> findings) {
        this.findings = findings;
    }

    public Map<String, Double> getCategoryScores() {
        return categoryScores;
    }

    public void setCategoryScores(Map<String, Double> categoryScores) {
        this.categoryScores = categoryScores;
    }

    public ReadinessScore getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(ReadinessScore readinessScore) {
        this.readinessScore = readinessScore;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TestingSummary getTestingSummary() {
        return testingSummary;
    }

    public void setTestingSummary(TestingSummary testingSummary) {
        this.testingSummary = testingSummary;
    }

    public DependencySummary getDependencySummary() {
        return dependencySummary;
    }

    public void setDependencySummary(DependencySummary dependencySummary) {
        this.dependencySummary = dependencySummary;
    }

    public SecuritySummary getSecuritySummary() {
        return securitySummary;
    }

    public void setSecuritySummary(SecuritySummary securitySummary) {
        this.securitySummary = securitySummary;
    }

    private UnifiedAnalysisSummary unifiedAnalysisSummary;

    public PerformanceSummary getPerformanceSummary() {
        return performanceSummary;
    }

    public void setPerformanceSummary(PerformanceSummary performanceSummary) {
        this.performanceSummary = performanceSummary;
    }

    public UnifiedAnalysisSummary getUnifiedAnalysisSummary() {
        return unifiedAnalysisSummary;
    }

    public void setUnifiedAnalysisSummary(UnifiedAnalysisSummary unifiedAnalysisSummary) {
        this.unifiedAnalysisSummary = unifiedAnalysisSummary;
    }

    public RiskSummary getRiskSummary() {
        return riskSummary;
    }

    public void setRiskSummary(RiskSummary riskSummary) {
        this.riskSummary = riskSummary;
    }
}
