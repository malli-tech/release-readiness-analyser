package com.aireadiness.recommendation.catalog;

import com.aireadiness.recommendation.model.RecommendationEffort;
import com.aireadiness.recommendation.model.RecommendationPriority;

public class RecommendationTemplate {

    private final String ruleId;
    private final String title;
    private final String summary;
    private final String recommendedAction;
    private final RecommendationPriority defaultPriority;
    private final RecommendationEffort defaultEffort;

    public RecommendationTemplate(
            String ruleId,
            String title,
            String summary,
            String recommendedAction,
            RecommendationPriority defaultPriority,
            RecommendationEffort defaultEffort
    ) {
        this.ruleId = ruleId;
        this.title = title;
        this.summary = summary;
        this.recommendedAction = recommendedAction;
        this.defaultPriority = defaultPriority;
        this.defaultEffort = defaultEffort;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public RecommendationPriority getDefaultPriority() {
        return defaultPriority;
    }

    public RecommendationEffort getDefaultEffort() {
        return defaultEffort;
    }
}
