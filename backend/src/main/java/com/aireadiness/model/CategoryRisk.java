package com.aireadiness.model;

import java.math.BigDecimal;

public class CategoryRisk {

    private String category;
    private int findingCount = 0;
    private int highFindings = 0;
    private int mediumFindings = 0;
    private int lowFindings = 0;
    private int infoFindings = 0;
    private BigDecimal weightedRiskPoints = BigDecimal.ZERO;
    private RiskLevel riskLevel = RiskLevel.LOW;

    public CategoryRisk() {
    }

    public CategoryRisk(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getFindingCount() {
        return findingCount;
    }

    public void setFindingCount(int findingCount) {
        this.findingCount = findingCount;
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

    public BigDecimal getWeightedRiskPoints() {
        return weightedRiskPoints;
    }

    public void setWeightedRiskPoints(BigDecimal weightedRiskPoints) {
        this.weightedRiskPoints = weightedRiskPoints;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(RiskLevel riskLevel) {
        this.riskLevel = riskLevel;
    }
}
