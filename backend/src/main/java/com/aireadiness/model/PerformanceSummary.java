package com.aireadiness.model;

import java.util.ArrayList;
import java.util.List;

public class PerformanceSummary {

    private int totalPerformanceFindings;
    private int highSeverityFindings;
    private int mediumSeverityFindings;
    private int lowSeverityFindings;
    private int detectedPerformanceIssues;
    private int affectedFiles;
    private int analyzedSourceFiles;
    private String performanceCompleteness = "UNKNOWN"; // COMPLETE, PARTIAL, UNKNOWN
    private List<String> performanceWarnings = new ArrayList<>();
    private String disclaimer = "Performance analysis is static and heuristic. It does not measure runtime CPU, memory, latency, throughput, or actual production performance.";

    public PerformanceSummary() {
    }

    public int getTotalPerformanceFindings() {
        return totalPerformanceFindings;
    }

    public void setTotalPerformanceFindings(int totalPerformanceFindings) {
        this.totalPerformanceFindings = totalPerformanceFindings;
    }

    public int getHighSeverityFindings() {
        return highSeverityFindings;
    }

    public void setHighSeverityFindings(int highSeverityFindings) {
        this.highSeverityFindings = highSeverityFindings;
    }

    public int getMediumSeverityFindings() {
        return mediumSeverityFindings;
    }

    public void setMediumSeverityFindings(int mediumSeverityFindings) {
        this.mediumSeverityFindings = mediumSeverityFindings;
    }

    public int getLowSeverityFindings() {
        return lowSeverityFindings;
    }

    public void setLowSeverityFindings(int lowSeverityFindings) {
        this.lowSeverityFindings = lowSeverityFindings;
    }

    public int getDetectedPerformanceIssues() {
        return detectedPerformanceIssues;
    }

    public void setDetectedPerformanceIssues(int detectedPerformanceIssues) {
        this.detectedPerformanceIssues = detectedPerformanceIssues;
    }

    public int getAffectedFiles() {
        return affectedFiles;
    }

    public void setAffectedFiles(int affectedFiles) {
        this.affectedFiles = affectedFiles;
    }

    public int getAnalyzedSourceFiles() {
        return analyzedSourceFiles;
    }

    public void setAnalyzedSourceFiles(int analyzedSourceFiles) {
        this.analyzedSourceFiles = analyzedSourceFiles;
    }

    public String getPerformanceCompleteness() {
        return performanceCompleteness;
    }

    public void setPerformanceCompleteness(String performanceCompleteness) {
        this.performanceCompleteness = performanceCompleteness;
    }

    public List<String> getPerformanceWarnings() {
        return performanceWarnings;
    }

    public void setPerformanceWarnings(List<String> performanceWarnings) {
        this.performanceWarnings = performanceWarnings != null ? performanceWarnings : new ArrayList<>();
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
