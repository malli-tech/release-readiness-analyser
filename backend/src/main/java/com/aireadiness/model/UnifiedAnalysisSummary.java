package com.aireadiness.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnifiedAnalysisSummary {

    private int totalFindings = 0;
    private int highFindings = 0;
    private int mediumFindings = 0;
    private int lowFindings = 0;
    private int infoFindings = 0;

    private Map<String, Integer> findingsByCategory = new HashMap<>();
    private Map<String, Integer> findingsBySeverity = new HashMap<>();

    private int affectedFiles = 0;
    private int analyzedFiles = 0;

    private List<String> completedAnalyzers = new ArrayList<>();
    private List<String> failedAnalyzers = new ArrayList<>();
    private List<String> skippedAnalyzers = new ArrayList<>();

    private String completeness = "UNKNOWN"; // COMPLETE, PARTIAL, UNKNOWN
    private List<String> warnings = new ArrayList<>();
    private String disclaimer = "Unified analysis is static and heuristic. It aggregates findings across all executed static analyzers without measuring runtime performance, executing code, or scoring release readiness.";

    public UnifiedAnalysisSummary() {
    }

    public int getTotalFindings() {
        return totalFindings;
    }

    public void setTotalFindings(int totalFindings) {
        this.totalFindings = totalFindings;
    }

    public int getHighFindings() {
        return highFindings;
    }

    public void setHighFindings(int highFindings) {
        this.highFindings = highFindings;
    }

    public int getMediumFindings() {
        return mediumFindings;
    }

    public void setMediumFindings(int mediumFindings) {
        this.mediumFindings = mediumFindings;
    }

    public int getLowFindings() {
        return lowFindings;
    }

    public void setLowFindings(int lowFindings) {
        this.lowFindings = lowFindings;
    }

    public int getInfoFindings() {
        return infoFindings;
    }

    public void setInfoFindings(int infoFindings) {
        this.infoFindings = infoFindings;
    }

    public Map<String, Integer> getFindingsByCategory() {
        return findingsByCategory;
    }

    public void setFindingsByCategory(Map<String, Integer> findingsByCategory) {
        this.findingsByCategory = findingsByCategory;
    }

    public Map<String, Integer> getFindingsBySeverity() {
        return findingsBySeverity;
    }

    public void setFindingsBySeverity(Map<String, Integer> findingsBySeverity) {
        this.findingsBySeverity = findingsBySeverity;
    }

    public int getAffectedFiles() {
        return affectedFiles;
    }

    public void setAffectedFiles(int affectedFiles) {
        this.affectedFiles = affectedFiles;
    }

    public int getAnalyzedFiles() {
        return analyzedFiles;
    }

    public void setAnalyzedFiles(int analyzedFiles) {
        this.analyzedFiles = analyzedFiles;
    }

    public List<String> getCompletedAnalyzers() {
        return completedAnalyzers;
    }

    public void setCompletedAnalyzers(List<String> completedAnalyzers) {
        this.completedAnalyzers = completedAnalyzers;
    }

    public List<String> getFailedAnalyzers() {
        return failedAnalyzers;
    }

    public void setFailedAnalyzers(List<String> failedAnalyzers) {
        this.failedAnalyzers = failedAnalyzers;
    }

    public List<String> getSkippedAnalyzers() {
        return skippedAnalyzers;
    }

    public void setSkippedAnalyzers(List<String> skippedAnalyzers) {
        this.skippedAnalyzers = skippedAnalyzers;
    }

    public String getCompleteness() {
        return completeness;
    }

    public void setCompleteness(String completeness) {
        this.completeness = completeness;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
