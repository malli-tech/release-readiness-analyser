package com.aireadiness.recommendation.dto;

import com.aireadiness.recommendation.model.Recommendation;
import com.aireadiness.recommendation.model.RecommendationEffort;
import com.aireadiness.recommendation.model.RecommendationPriority;
import com.aireadiness.recommendation.model.RecommendationStatus;

import java.time.Instant;

public class RecommendationResponse {

    private String id;
    private String analysisId;
    private String findingId;
    private String ruleId;
    private String category;
    private String severity;
    private String title;
    private String summary;
    private String recommendedAction;
    private RecommendationPriority priority;
    private RecommendationEffort effort;
    private RecommendationStatus status;
    private String filePath;
    private Integer lineNumber;
    private Instant createdAt;
    private Instant updatedAt;

    public RecommendationResponse() {
    }

    public static RecommendationResponse fromDomain(Recommendation rec) {
        if (rec == null) {
            return null;
        }
        RecommendationResponse res = new RecommendationResponse();
        res.setId(rec.getId());
        res.setAnalysisId(rec.getAnalysisId());
        res.setFindingId(rec.getFindingId());
        res.setRuleId(rec.getRuleId());
        res.setCategory(rec.getCategory());
        res.setSeverity(rec.getSeverity());
        res.setTitle(rec.getTitle());
        res.setSummary(rec.getSummary());
        res.setRecommendedAction(rec.getRecommendedAction());
        res.setPriority(rec.getPriority());
        res.setEffort(rec.getEffort());
        res.setStatus(rec.getStatus());
        res.setFilePath(rec.getFilePath());
        res.setLineNumber(rec.getLineNumber());
        res.setCreatedAt(rec.getCreatedAt());
        res.setUpdatedAt(rec.getUpdatedAt());
        return res;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
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

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public RecommendationPriority getPriority() {
        return priority;
    }

    public void setPriority(RecommendationPriority priority) {
        this.priority = priority;
    }

    public RecommendationEffort getEffort() {
        return effort;
    }

    public void setEffort(RecommendationEffort effort) {
        this.effort = effort;
    }

    public RecommendationStatus getStatus() {
        return status;
    }

    public void setStatus(RecommendationStatus status) {
        this.status = status;
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
