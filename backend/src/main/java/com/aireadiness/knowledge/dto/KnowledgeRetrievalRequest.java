package com.aireadiness.knowledge.dto;

import com.aireadiness.knowledge.model.KnowledgeCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class KnowledgeRetrievalRequest {

    @NotBlank(message = "Retrieval query must not be blank")
    private String query;

    private KnowledgeCategory category;
    private String technology;
    private String version;

    @Min(value = 1, message = "topK must be at least 1")
    @Max(value = 20, message = "topK must not exceed 20")
    private Integer topK = 5;

    public KnowledgeRetrievalRequest() {
    }

    public KnowledgeRetrievalRequest(String query, KnowledgeCategory category, String technology, String version, Integer topK) {
        this.query = query;
        this.category = category;
        this.technology = technology;
        this.version = version;
        if (topK != null) {
            this.topK = topK;
        }
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public KnowledgeCategory getCategory() {
        return category;
    }

    public void setCategory(KnowledgeCategory category) {
        this.category = category;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Integer getTopK() {
        return topK != null ? topK : 5;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }
}
