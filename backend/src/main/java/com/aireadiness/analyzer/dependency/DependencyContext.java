package com.aireadiness.analyzer.dependency;

import com.aireadiness.analyzer.dependency.model.DependencyInfo;
import com.aireadiness.analyzer.dependency.model.DependencyManifestInfo;
import com.aireadiness.model.DependencySummary;
import com.aireadiness.model.ProjectProfile;

import java.nio.file.Path;
import java.util.List;

public class DependencyContext {

    private final Path workspaceDir;
    private final ProjectProfile profile;
    private final String analysisId;
    private final String uploadMode;
    private final List<DependencyManifestInfo> manifests;
    private final List<DependencyInfo> allDependencies;
    private final DependencySummary summary;
    private final List<String> warnings;

    public DependencyContext(
            Path workspaceDir,
            ProjectProfile profile,
            String analysisId,
            String uploadMode,
            List<DependencyManifestInfo> manifests,
            List<DependencyInfo> allDependencies,
            DependencySummary summary,
            List<String> warnings
    ) {
        this.workspaceDir = workspaceDir;
        this.profile = profile;
        this.analysisId = analysisId;
        this.uploadMode = uploadMode;
        this.manifests = manifests;
        this.allDependencies = allDependencies;
        this.summary = summary;
        this.warnings = warnings;
    }

    public Path getWorkspaceDir() {
        return workspaceDir;
    }

    public ProjectProfile getProfile() {
        return profile;
    }

    public String getAnalysisId() {
        return analysisId;
    }

    public String getUploadMode() {
        return uploadMode;
    }

    public List<DependencyManifestInfo> getManifests() {
        return manifests;
    }

    public List<DependencyInfo> getAllDependencies() {
        return allDependencies;
    }

    public DependencySummary getSummary() {
        return summary;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
