package com.aireadiness.aireview.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ai_reviews")
@CompoundIndex(name = "analysis_finding_idx", def = "{'analysisId': 1, 'findingId': 1}", unique = true)
public class AIReview {

    @Id
    private String id;

    @Indexed
    private String analysisId;

    @Indexed
    private String findingId;

    @Indexed
    private String releaseId;

    @Indexed
    private String userId;

    private String ruleId;
    private String category;

    @Indexed
    private AIReviewStatus status = AIReviewStatus.PENDING;

    private String summary;
    private String whyItMatters;
    private List<String> whatToReview = new ArrayList<>();
    private String suggestedFix;
    private AIReviewConfidence confidence;

    private String model;
    private String promptVersion = "ai-review-v1";
    private String errorMessage;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public AIReview() {
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

    public AIReviewStatus getStatus() {
        return status;
    }

    public void setStatus(AIReviewStatus status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getWhyItMatters() {
        return whyItMatters;
    }

    public void setWhyItMatters(String whyItMatters) {
        this.whyItMatters = whyItMatters;
    }

    public List<String> getWhatToReview() {
        return whatToReview;
    }

    public void setWhatToReview(List<String> whatToReview) {
        this.whatToReview = whatToReview != null ? whatToReview : new ArrayList<>();
    }

    public String getSuggestedFix() {
        return suggestedFix;
    }

    public void setSuggestedFix(String suggestedFix) {
        this.suggestedFix = suggestedFix;
    }

    public AIReviewConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(AIReviewConfidence confidence) {
        this.confidence = confidence;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPromptVersion() {
        return promptVersion;
    }

    public void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
