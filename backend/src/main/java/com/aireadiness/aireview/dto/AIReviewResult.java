package com.aireadiness.aireview.dto;

import com.aireadiness.aireview.model.AIReviewConfidence;

import java.util.ArrayList;
import java.util.List;

public class AIReviewResult {

    private String summary;
    private String whyItMatters;
    private List<String> whatToReview = new ArrayList<>();
    private String suggestedFix;
    private AIReviewConfidence confidence = AIReviewConfidence.MEDIUM;

    public AIReviewResult() {
    }

    public AIReviewResult(String summary, String whyItMatters, List<String> whatToReview, String suggestedFix, AIReviewConfidence confidence) {
        this.summary = summary;
        this.whyItMatters = whyItMatters;
        if (whatToReview != null) {
            this.whatToReview = whatToReview;
        }
        this.suggestedFix = suggestedFix;
        if (confidence != null) {
            this.confidence = confidence;
        }
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
}
