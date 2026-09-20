package com.aireadiness.aireview.service;

import com.aireadiness.aireview.dto.AIReviewPromptContext;
import com.aireadiness.aireview.dto.AIReviewResult;

public interface LLMService {

    /**
     * Generates a structured AI review using an LLM provider based on assembled context.
     *
     * @param context Prompt context containing finding data, project profile, evidence, and RAG knowledge.
     * @return AIReviewResult containing structured summary, impact, review steps, fix guidance, and confidence.
     */
    AIReviewResult generateAIReview(AIReviewPromptContext context);

    String getModel();

    boolean isApiKeyConfigured();
}
