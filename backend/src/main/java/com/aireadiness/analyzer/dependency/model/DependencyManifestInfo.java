package com.aireadiness.analyzer.dependency.model;

import java.util.ArrayList;
import java.util.List;

public class DependencyManifestInfo {

    private String manifestPath;
    private String ecosystem;
    private String packageManager;
    private boolean isLockfile;
    private List<DependencyInfo> dependencies = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private boolean isMalformed;

    public DependencyManifestInfo() {
    }

    public DependencyManifestInfo(String manifestPath, String ecosystem, String packageManager, boolean isLockfile) {
        this.manifestPath = manifestPath;
        this.ecosystem = ecosystem;
        this.packageManager = packageManager;
        this.isLockfile = isLockfile;
    }

    public String getManifestPath() {
        return manifestPath;
    }

    public void setManifestPath(String manifestPath) {
        this.manifestPath = manifestPath;
    }

    public String getEcosystem() {
        return ecosystem;
    }

    public void setEcosystem(String ecosystem) {
        this.ecosystem = ecosystem;
    }

    public String getPackageManager() {
        return packageManager;
    }

    public void setPackageManager(String packageManager) {
        this.packageManager = packageManager;
    }

    public boolean isLockfile() {
        return isLockfile;
    }

    public void setLockfile(boolean lockfile) {
        isLockfile = lockfile;
    }

    public List<DependencyInfo> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<DependencyInfo> dependencies) {
        this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    public boolean isMalformed() {
        return isMalformed;
    }

    public void setMalformed(boolean malformed) {
        isMalformed = malformed;
    }
}
