package com.aireadiness.model;

import java.util.ArrayList;
import java.util.List;

public class DetectionEvidence {

    private String technology;
    private String confidence; // HIGH | MEDIUM | LOW
    private List<String> evidence = new ArrayList<>();

    public DetectionEvidence() {
    }

    public DetectionEvidence(String technology, String confidence, List<String> evidence) {
        this.technology = technology;
        this.confidence = confidence;
        this.evidence = evidence != null ? evidence : new ArrayList<>();
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public List<String> getEvidence() {
        return evidence;
    }

    public void setEvidence(List<String> evidence) {
        this.evidence = evidence;
    }
}
