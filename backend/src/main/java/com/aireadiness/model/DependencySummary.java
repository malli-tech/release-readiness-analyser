package com.aireadiness.model;

import java.util.ArrayList;
import java.util.List;

public class DependencySummary {

    private List<String> manifestFiles = new ArrayList<>();
    private List<String> detectedPackageManagers = new ArrayList<>();
    private int dependencyCount;
    private int directDependencyCount;
    private int devDependencyCount;
    private int unpinnedDependencyCount;
    private int broadVersionDependencyCount;
    private int duplicateDependencyCount;
    private List<String> dependencyManagementWarnings = new ArrayList<>();
    private String dependencyCompleteness = "UNKNOWN"; // COMPLETE, PARTIAL, UNKNOWN
    private List<String> dependencyWarnings = new ArrayList<>();
    private String disclaimer = "Static dependency management analysis based on uploaded manifest declarations. Does not perform external vulnerability database lookups or execute package managers.";

    public DependencySummary() {
    }

    public List<String> getManifestFiles() {
        return manifestFiles;
    }

    public void setManifestFiles(List<String> manifestFiles) {
        this.manifestFiles = manifestFiles != null ? manifestFiles : new ArrayList<>();
    }

    public List<String> getDetectedPackageManagers() {
        return detectedPackageManagers;
    }

    public void setDetectedPackageManagers(List<String> detectedPackageManagers) {
        this.detectedPackageManagers = detectedPackageManagers != null ? detectedPackageManagers : new ArrayList<>();
    }

    public int getDependencyCount() {
        return dependencyCount;
    }

    public void setDependencyCount(int dependencyCount) {
        this.dependencyCount = dependencyCount;
    }

    public int getDirectDependencyCount() {
        return directDependencyCount;
    }

    public void setDirectDependencyCount(int directDependencyCount) {
        this.directDependencyCount = directDependencyCount;
    }

    public int getDevDependencyCount() {
        return devDependencyCount;
    }

    public void setDevDependencyCount(int devDependencyCount) {
        this.devDependencyCount = devDependencyCount;
    }

    public int getUnpinnedDependencyCount() {
        return unpinnedDependencyCount;
    }

    public void setUnpinnedDependencyCount(int unpinnedDependencyCount) {
        this.unpinnedDependencyCount = unpinnedDependencyCount;
    }

    public int getBroadVersionDependencyCount() {
        return broadVersionDependencyCount;
    }

    public void setBroadVersionDependencyCount(int broadVersionDependencyCount) {
        this.broadVersionDependencyCount = broadVersionDependencyCount;
    }

    public int getDuplicateDependencyCount() {
        return duplicateDependencyCount;
    }

    public void setDuplicateDependencyCount(int duplicateDependencyCount) {
        this.duplicateDependencyCount = duplicateDependencyCount;
    }

    public List<String> getDependencyManagementWarnings() {
        return dependencyManagementWarnings;
    }

    public void setDependencyManagementWarnings(List<String> dependencyManagementWarnings) {
        this.dependencyManagementWarnings = dependencyManagementWarnings != null ? dependencyManagementWarnings : new ArrayList<>();
    }

    public String getDependencyCompleteness() {
        return dependencyCompleteness;
    }

    public void setDependencyCompleteness(String dependencyCompleteness) {
        this.dependencyCompleteness = dependencyCompleteness;
    }

    public List<String> getDependencyWarnings() {
        return dependencyWarnings;
    }

    public void setDependencyWarnings(List<String> dependencyWarnings) {
        this.dependencyWarnings = dependencyWarnings != null ? dependencyWarnings : new ArrayList<>();
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
