package com.aireadiness.aireview.dto;

import com.aireadiness.knowledge.dto.KnowledgeRetrievalResult;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;

public class AIReviewPromptContext {

    private ProjectProfile projectProfile;
    private Finding finding;
    private String evidenceSnippet;
    private List<KnowledgeRetrievalResult> ragChunks = new ArrayList<>();

    public AIReviewPromptContext() {
    }

    public AIReviewPromptContext(ProjectProfile projectProfile, Finding finding, String evidenceSnippet, List<KnowledgeRetrievalResult> ragChunks) {
        this.projectProfile = projectProfile;
        this.finding = finding;
        this.evidenceSnippet = evidenceSnippet;
        if (ragChunks != null) {
            this.ragChunks = ragChunks;
        }
    }

    public ProjectProfile getProjectProfile() {
        return projectProfile;
    }

    public void setProjectProfile(ProjectProfile projectProfile) {
        this.projectProfile = projectProfile;
    }

    public Finding getFinding() {
        return finding;
    }

    public void setFinding(Finding finding) {
        this.finding = finding;
    }

    public String getEvidenceSnippet() {
        return evidenceSnippet;
    }

    public void setEvidenceSnippet(String evidenceSnippet) {
        this.evidenceSnippet = evidenceSnippet;
    }

    public List<KnowledgeRetrievalResult> getRagChunks() {
        return ragChunks;
    }

    public void setRagChunks(List<KnowledgeRetrievalResult> ragChunks) {
        this.ragChunks = ragChunks != null ? ragChunks : new ArrayList<>();
    }
}
