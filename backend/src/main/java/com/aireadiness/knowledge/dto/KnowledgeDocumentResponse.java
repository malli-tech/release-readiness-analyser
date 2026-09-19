package com.aireadiness.knowledge.dto;

import com.aireadiness.knowledge.model.KnowledgeCategory;
import com.aireadiness.knowledge.model.KnowledgeDocument;
import com.aireadiness.knowledge.model.KnowledgeDocumentStatus;
import com.aireadiness.knowledge.model.SourceType;

import java.time.Instant;

public class KnowledgeDocumentResponse {

    private String id;
    private String title;
    private String source;
    private SourceType sourceType;
    private KnowledgeCategory category;
    private String technology;
    private String version;
    private String description;
    private KnowledgeDocumentStatus status;
    private int chunkCount;
    private Instant createdAt;
    private Instant updatedAt;

    public KnowledgeDocumentResponse() {
    }

    public static KnowledgeDocumentResponse fromDomain(KnowledgeDocument doc) {
        if (doc == null) return null;
        KnowledgeDocumentResponse res = new KnowledgeDocumentResponse();
        res.setId(doc.getId());
        res.setTitle(doc.getTitle());
        res.setSource(doc.getSource());
        res.setSourceType(doc.getSourceType());
        res.setCategory(doc.getCategory());
        res.setTechnology(doc.getTechnology());
        res.setVersion(doc.getVersion());
        res.setDescription(doc.getDescription());
        res.setStatus(doc.getStatus());
        res.setChunkCount(doc.getChunkCount());
        res.setCreatedAt(doc.getCreatedAt());
        res.setUpdatedAt(doc.getUpdatedAt());
        return res;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public KnowledgeDocumentStatus getStatus() {
        return status;
    }

    public void setStatus(KnowledgeDocumentStatus status) {
        this.status = status;
    }

    public int getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(int chunkCount) {
        this.chunkCount = chunkCount;
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
