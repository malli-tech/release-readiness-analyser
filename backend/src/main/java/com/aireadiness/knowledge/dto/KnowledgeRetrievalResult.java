package com.aireadiness.knowledge.dto;

import com.aireadiness.knowledge.model.KnowledgeCategory;
import com.aireadiness.knowledge.model.KnowledgeChunk;
import com.aireadiness.knowledge.model.SourceType;

public class KnowledgeRetrievalResult {

    private String chunkId;
    private String documentId;
    private int chunkIndex;
    private String text;
    private String title;
    private String source;
    private SourceType sourceType;
    private KnowledgeCategory category;
    private String technology;
    private String version;
    private double similarityScore;

    public KnowledgeRetrievalResult() {
    }

    public static KnowledgeRetrievalResult fromChunk(KnowledgeChunk chunk, double similarityScore) {
        KnowledgeRetrievalResult res = new KnowledgeRetrievalResult();
        res.setChunkId(chunk.getId());
        res.setDocumentId(chunk.getDocumentId());
        res.setChunkIndex(chunk.getChunkIndex());
        res.setText(chunk.getText());
        res.setTitle(chunk.getTitle());
        res.setSource(chunk.getSource());
        res.setSourceType(chunk.getSourceType());
        res.setCategory(chunk.getCategory());
        res.setTechnology(chunk.getTechnology());
        res.setVersion(chunk.getVersion());
        res.setSimilarityScore(similarityScore);
        return res;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
