package com.aireadiness.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalysisPlan {

    private List<String> analyzers = new ArrayList<>(); // CODE_QUALITY, TESTING, DEPENDENCIES, SECURITY, PERFORMANCE
    private Map<String, String> rationale = new HashMap<>();

    public AnalysisPlan() {
    }

    public AnalysisPlan(List<String> analyzers, Map<String, String> rationale) {
        this.analyzers = analyzers != null ? analyzers : new ArrayList<>();
        this.rationale = rationale != null ? rationale : new HashMap<>();
    }

    public List<String> getAnalyzers() {
        return analyzers;
    }

    public void setAnalyzers(List<String> analyzers) {
        this.analyzers = analyzers;
    }

    public Map<String, String> getRationale() {
        return rationale;
    }

    public void setRationale(Map<String, String> rationale) {
        this.rationale = rationale;
    }
}
