package com.aireadiness.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiskSummary {

    private RiskLevel overallRiskLevel = RiskLevel.UNKNOWN;
    private BigDecimal weightedRiskPoints = BigDecimal.ZERO;
    private BigDecimal baseRiskPoints = BigDecimal.ZERO;

    private int totalFindings = 0;
    private int highFindings = 0;
    private int mediumFindings = 0;
    private int lowFindings = 0;
    private int infoFindings = 0;

    private Map<String, CategoryRisk> categoryRisk = new HashMap<>();
    private Map<String, BigDecimal> severityRisk = new HashMap<>();

    private List<RiskFactor> riskFactors = new ArrayList<>();
    private List<String> riskWarnings = new ArrayList<>();

    private String completeness = "UNKNOWN"; // COMPLETE, PARTIAL, UNKNOWN
    private String calculationVersion = "risk-v1";

    public RiskSummary() {
    }

    public RiskLevel getOverallRiskLevel() {
        return overallRiskLevel;
    }

    public void setOverallRiskLevel(RiskLevel overallRiskLevel) {
        this.overallRiskLevel = overallRiskLevel;
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

    public Map<String, CategoryRisk> getCategoryRisk() {
        return categoryRisk;
    }

    public void setCategoryRisk(Map<String, CategoryRisk> categoryRisk) {
        this.categoryRisk = categoryRisk;
    }

    public Map<String, BigDecimal> getSeverityRisk() {
        return severityRisk;
    }

    public void setSeverityRisk(Map<String, BigDecimal> severityRisk) {
        this.severityRisk = severityRisk;
    }

    public List<RiskFactor> getRiskFactors() {
        return riskFactors;
    }

    public void setRiskFactors(List<RiskFactor> riskFactors) {
        this.riskFactors = riskFactors;
    }

    public List<String> getRiskWarnings() {
        return riskWarnings;
    }

    public void setRiskWarnings(List<String> riskWarnings) {
        this.riskWarnings = riskWarnings;
    }

    public String getCompleteness() {
        return completeness;
    }

    public void setCompleteness(String completeness) {
        this.completeness = completeness;
    }

    public String getCalculationVersion() {
        return calculationVersion;
    }

    public void setCalculationVersion(String calculationVersion) {
        this.calculationVersion = calculationVersion;
    }
}
