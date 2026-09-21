package com.aireadiness.dto.report;

import java.time.Instant;

public class ReleaseReportResponse {

    private String id;
    private String projectId;
    private String releaseId;
    private String userId;
    private String version;
    private String reportNumber;
    private String releaseName;
    private String projectName;
    private String status;
    private Double readinessScore;
    private String readinessLevel;
    private String riskLevel;
    private String latestAnalysisId;
    private Instant createdAt;
    private Instant updatedAt;

    public ReleaseReportResponse() {
    }

    public ReleaseReportResponse(
            String id,
            String projectId,
            String releaseId,
            String userId,
            String version,
            String reportNumber,
            String releaseName,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.projectId = projectId;
        this.releaseId = releaseId;
        this.userId = userId;
        this.version = version;
        this.reportNumber = reportNumber;
        this.releaseName = releaseName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getReportNumber() {
        return reportNumber;
    }

    public void setReportNumber(String reportNumber) {
        this.reportNumber = reportNumber;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(Double readinessScore) {
        this.readinessScore = readinessScore;
    }

    public String getReadinessLevel() {
        return readinessLevel;
    }

    public void setReadinessLevel(String readinessLevel) {
        this.readinessLevel = readinessLevel;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getLatestAnalysisId() {
        return latestAnalysisId;
    }

    public void setLatestAnalysisId(String latestAnalysisId) {
        this.latestAnalysisId = latestAnalysisId;
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
