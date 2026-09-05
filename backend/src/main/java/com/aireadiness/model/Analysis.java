package com.aireadiness.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "analyses")
public class Analysis {

    @Id
    private String id;

    @Indexed
    private String projectId;

    @Indexed
    private String releaseId;

    @Indexed
    private String userId;

    private int runNumber = 1;

    private String status = "CREATED"; // CREATED, DETECTING, DETECTED, READY_FOR_ANALYSIS, FAILED

    private Instant startedAt = Instant.now();

    private Instant completedAt;

    private ProjectProfile projectProfile;

    private AnalysisPlan analysisPlan;

    private List<Finding> findings = new ArrayList<>();

    private Map<String, Double> categoryScores = new HashMap<>();

    private Map<String, Object> riskSummary = new HashMap<>();

    private Double readinessScore;

    private List<String> warnings = new ArrayList<>();

    private TestingSummary testingSummary;

    private DependencySummary dependencySummary;

    private SecuritySummary securitySummary;

    private PerformanceSummary performanceSummary;

    public Analysis() {
    }

    public Analysis(String projectId, String releaseId, String userId, int runNumber, String status) {
        this.projectId = projectId;
        this.releaseId = releaseId;
        this.userId = userId;
        this.runNumber = runNumber;
        this.status = status;
        this.startedAt = Instant.now();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public Map<String, Object> getRiskSummary() {
        return riskSummary;
    }

    public void setRiskSummary(Map<String, Object> riskSummary) {
        this.riskSummary = riskSummary;
    }

    public Double getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(Double readinessScore) {
        this.readinessScore = readinessScore;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
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

    public PerformanceSummary getPerformanceSummary() {
        return performanceSummary;
    }

    public void setPerformanceSummary(PerformanceSummary performanceSummary) {
        this.performanceSummary = performanceSummary;
    }
}
