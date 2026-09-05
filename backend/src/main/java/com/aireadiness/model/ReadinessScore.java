package com.aireadiness.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReadinessScore {

    private BigDecimal readinessScore;
    private ReadinessLevel readinessLevel;
    private ReadinessConfidence confidence;
    private BigDecimal weightedRiskPoints;
    private BigDecimal baseRiskPoints;

    private int totalFindings;
    private int highFindings;
    private int mediumFindings;
    private int lowFindings;
    private int infoFindings;

    private String completeness;
    private String riskLevel;
    private String calculationVersion = "readiness-v1";

    private List<String> readinessWarnings = new ArrayList<>();
    private List<ReadinessFactor> readinessFactors = new ArrayList<>();

    public ReadinessScore() {
    }

    public BigDecimal getReadinessScore() {
        return readinessScore;
    }

    public void setReadinessScore(BigDecimal readinessScore) {
        this.readinessScore = readinessScore;
    }

    public ReadinessLevel getReadinessLevel() {
        return readinessLevel;
    }

    public void setReadinessLevel(ReadinessLevel readinessLevel) {
        this.readinessLevel = readinessLevel;
    }

    public ReadinessConfidence getConfidence() {
        return confidence;
    }

    public void setConfidence(ReadinessConfidence confidence) {
        this.confidence = confidence;
    }

    public BigDecimal getWeightedRiskPoints() {
        return weightedRiskPoints;
    }

    public void setWeightedRiskPoints(BigDecimal weightedRiskPoints) {
        this.weightedRiskPoints = weightedRiskPoints;
    }

    public BigDecimal getBaseRiskPoints() {
        return baseRiskPoints;
    }

    public void setBaseRiskPoints(BigDecimal baseRiskPoints) {
        this.baseRiskPoints = baseRiskPoints;
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

    public String getCompleteness() {
        return completeness;
    }

    public void setCompleteness(String completeness) {
        this.completeness = completeness;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getCalculationVersion() {
        return calculationVersion;
    }

    public void setCalculationVersion(String calculationVersion) {
        this.calculationVersion = calculationVersion;
    }

    public List<String> getReadinessWarnings() {
        return readinessWarnings;
    }

    public void setReadinessWarnings(List<String> readinessWarnings) {
        this.readinessWarnings = readinessWarnings;
    }

    public List<ReadinessFactor> getReadinessFactors() {
        return readinessFactors;
    }

    public void setReadinessFactors(List<ReadinessFactor> readinessFactors) {
        this.readinessFactors = readinessFactors;
    }
}
