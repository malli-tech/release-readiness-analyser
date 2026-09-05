package com.aireadiness.model;

public class RiskFactor {

    private String title;
    private String description;
    private String severity; // HIGH, MEDIUM, LOW
    private String impactCategory;

    public RiskFactor() {
    }

    public RiskFactor(String title, String description, String severity, String impactCategory) {
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.impactCategory = impactCategory;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getImpactCategory() {
        return impactCategory;
    }

    public void setImpactCategory(String impactCategory) {
        this.impactCategory = impactCategory;
    }
}
