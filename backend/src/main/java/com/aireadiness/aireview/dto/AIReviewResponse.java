package com.aireadiness.aireview.dto;

import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.model.AIReviewConfidence;
import com.aireadiness.aireview.model.AIReviewStatus;

import java.time.Instant;
import java.util.List;

public class AIReviewResponse {

    private String id;
    private String analysisId;
    private String findingId;
    private String ruleId;
    private String category;
    private AIReviewStatus status;
    private String summary;
    private String whyItMatters;
    private List<String> whatToReview;
    private String suggestedFix;
    private AIReviewConfidence confidence;
    private String model;
    private String promptVersion;
    private String errorMessage;
    private Instant createdAt;
    private Instant updatedAt;
    private String disclaimer = "AI Explanation & Review Guidance (Non-Authoritative Explanation). Deterministic findings and risk scores remain authoritative.";

    public AIReviewResponse() {
    }

    public static AIReviewResponse fromDomain(AIReview review) {
        AIReviewResponse res = new AIReviewResponse();
        res.setId(review.getId());
        res.setAnalysisId(review.getAnalysisId());
        res.setFindingId(review.getFindingId());
        res.setRuleId(review.getRuleId());
        res.setCategory(review.getCategory());
        res.setStatus(review.getStatus());
        res.setSummary(review.getSummary());
        res.setWhyItMatters(review.getWhyItMatters());
        res.setWhatToReview(review.getWhatToReview());
        res.setSuggestedFix(review.getSuggestedFix());
        res.setConfidence(review.getConfidence());
        res.setModel(review.getModel());
        res.setPromptVersion(review.getPromptVersion());
        res.setErrorMessage(review.getErrorMessage());
        res.setCreatedAt(review.getCreatedAt());
        res.setUpdatedAt(review.getUpdatedAt());
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
        this.whatToReview = whatToReview;
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

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
