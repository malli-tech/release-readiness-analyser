package com.aireadiness.analyzer.dependency.model;

public class DependencyInfo {

    private String name;
    private String version;
    private String manifestPath;
    private String ecosystem;
    private String scope; // DIRECT, DEV, PEER, OPTIONAL, UNKNOWN
    private String versionType; // EXACT, RANGE, BROAD_RANGE, UNPINNED, UNKNOWN
    private int lineNumber;

    public DependencyInfo() {
    }

    public DependencyInfo(String name, String version, String manifestPath, String ecosystem, String scope, String versionType, int lineNumber) {
        this.name = name;
        this.version = version;
        this.manifestPath = manifestPath;
        this.ecosystem = ecosystem;
        this.scope = scope;
        this.versionType = versionType;
        this.lineNumber = lineNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
