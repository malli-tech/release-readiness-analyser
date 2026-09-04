package com.aireadiness.model;

import java.util.ArrayList;
import java.util.List;

public class ProjectProfile {

    private String primaryLanguage;
    private List<String> languages = new ArrayList<>();
    private String framework;
    private List<String> frameworks = new ArrayList<>();
    private String buildSystem;
    private String projectType; // BACKEND, FRONTEND, FULL_STACK, MOBILE, LIBRARY, CLI, UNKNOWN
    private List<String> testFrameworks = new ArrayList<>();
    private String database;
    private List<String> databases = new ArrayList<>();
    private String packageManager;
    private List<String> entryPoints = new ArrayList<>();
    private List<String> detectedManifests = new ArrayList<>();
    private ProjectStructure projectStructure;
    private String analysisCompleteness; // COMPLETE, PARTIAL, UNKNOWN
    private List<String> detectionWarnings = new ArrayList<>();
    private List<DetectionEvidence> detectionEvidences = new ArrayList<>();

    public ProjectProfile() {
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public List<String> getFrameworks() {
        return frameworks;
    }

    public void setFrameworks(List<String> frameworks) {
        this.frameworks = frameworks;
    }

    public String getBuildSystem() {
        return buildSystem;
    }

    public void setBuildSystem(String buildSystem) {
        this.buildSystem = buildSystem;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public List<String> getTestFrameworks() {
        return testFrameworks;
    }

    public void setTestFrameworks(List<String> testFrameworks) {
        this.testFrameworks = testFrameworks;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public List<String> getDatabases() {
        return databases;
    }

    public void setDatabases(List<String> databases) {
        this.databases = databases;
    }

    public String getPackageManager() {
        return packageManager;
    }

    public void setPackageManager(String packageManager) {
        this.packageManager = packageManager;
    }

    public List<String> getEntryPoints() {
        return entryPoints;
    }

    public void setEntryPoints(List<String> entryPoints) {
        this.entryPoints = entryPoints;
    }

    public List<String> getDetectedManifests() {
        return detectedManifests;
    }

    public void setDetectedManifests(List<String> detectedManifests) {
        this.detectedManifests = detectedManifests;
    }

    public ProjectStructure getProjectStructure() {
        return projectStructure;
    }

    public void setProjectStructure(ProjectStructure projectStructure) {
        this.projectStructure = projectStructure;
    }

    public String getAnalysisCompleteness() {
        return analysisCompleteness;
    }

    public void setAnalysisCompleteness(String analysisCompleteness) {
        this.analysisCompleteness = analysisCompleteness;
    }

    public List<String> getDetectionWarnings() {
        return detectionWarnings;
    }

    public void setDetectionWarnings(List<String> detectionWarnings) {
        this.detectionWarnings = detectionWarnings;
    }

    public List<DetectionEvidence> getDetectionEvidences() {
        return detectionEvidences;
    }

    public void setDetectionEvidences(List<DetectionEvidence> detectionEvidences) {
        this.detectionEvidences = detectionEvidences;
    }
}
