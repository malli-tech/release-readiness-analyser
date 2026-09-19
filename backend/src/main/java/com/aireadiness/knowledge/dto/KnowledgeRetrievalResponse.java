package com.aireadiness.knowledge.dto;

import java.util.List;

public class KnowledgeRetrievalResponse {

    private String query;
    private int topK;
    private List<KnowledgeRetrievalResult> results;

    public KnowledgeRetrievalResponse() {
    }

    public KnowledgeRetrievalResponse(String query, int topK, List<KnowledgeRetrievalResult> results) {
        this.query = query;
        this.topK = topK;
        this.results = results;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }

    public List<KnowledgeRetrievalResult> getResults() {
        return results;
    }

    public void setResults(List<KnowledgeRetrievalResult> results) {
        this.results = results;
    }
}
