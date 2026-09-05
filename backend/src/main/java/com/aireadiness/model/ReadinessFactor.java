package com.aireadiness.model;

public class ReadinessFactor {

    private String factor;
    private String description;
    private String impact; // HIGH, MEDIUM, LOW
    private String category; // GENERAL, SECURITY, TESTING, DEPENDENCY, PERFORMANCE, COMPLETENESS

    public ReadinessFactor() {
    }

    public ReadinessFactor(String factor, String description, String impact, String category) {
        this.factor = factor;
        this.description = description;
        this.impact = impact;
        this.category = category;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
