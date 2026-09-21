package com.aireadiness.model.report;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "release_reports")
@CompoundIndexes({
    @CompoundIndex(name = "proj_rel_report_idx", def = "{'projectId': 1, 'releaseId': 1}", unique = true)
})
public class ReleaseReport {

    @Id
    private String id;

    @Indexed
    private String projectId;

    @Indexed
    private String releaseId;

    @Indexed
    private String userId;

    private String version;

    private String reportNumber;

    private String releaseName;

    private String status = "NOT_ANALYZED";

    private Double readinessScore;

    private String readinessLevel;

    private String riskLevel;

    private String latestAnalysisId;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public ReleaseReport() {
    }

    public ReleaseReport(String projectId, String releaseId, String userId, String version, String reportNumber, String releaseName, String status) {
        this.projectId = projectId;
        this.releaseId = releaseId;
        this.userId = userId;
        this.version = version;
        this.reportNumber = reportNumber;
        this.releaseName = releaseName;
        this.status = status != null ? status : "NOT_ANALYZED";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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
