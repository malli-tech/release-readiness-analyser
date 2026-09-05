package com.aireadiness.analyzer.performance;

import com.aireadiness.analyzer.performance.model.ParsedSourceFile;
import com.aireadiness.model.PerformanceSummary;
import com.aireadiness.model.ProjectProfile;

import java.nio.file.Path;
import java.util.List;

public class PerformanceContext {

    private final Path workspaceDir;
    private final ProjectProfile profile;
    private final String analysisId;
    private final String uploadMode;
    private final List<ParsedSourceFile> parsedFiles;
    private final PerformanceSummary summary;
    private final List<String> warnings;

    public PerformanceContext(
            Path workspaceDir,
            ProjectProfile profile,
            String analysisId,
            String uploadMode,
            List<ParsedSourceFile> parsedFiles,
            PerformanceSummary summary,
            List<String> warnings
    ) {
        this.workspaceDir = workspaceDir;
        this.profile = profile;
        this.analysisId = analysisId;
        this.uploadMode = uploadMode;
        this.parsedFiles = parsedFiles;
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

    public List<ParsedSourceFile> getParsedFiles() {
        return parsedFiles;
    }

    public PerformanceSummary getSummary() {
        return summary;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean isCompleteProject() {
        return "COMPLETE_PROJECT".equalsIgnoreCase(uploadMode);
    }
}
