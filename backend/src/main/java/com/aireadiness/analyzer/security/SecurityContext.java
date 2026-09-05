package com.aireadiness.analyzer.security;

import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.ProjectProfile;
import com.aireadiness.model.SecuritySummary;

import java.nio.file.Path;
import java.util.List;

public class SecurityContext {

    private final Path workspaceDir;
    private final ProjectProfile profile;
    private final String analysisId;
    private final String uploadMode;
    private final List<ParsedSourceFile> parsedFiles;
    private final SecuritySummary summary;
    private final List<String> warnings;

    public SecurityContext(
            Path workspaceDir,
            ProjectProfile profile,
            String analysisId,
            String uploadMode,
            List<ParsedSourceFile> parsedFiles,
            SecuritySummary summary,
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

    public SecuritySummary getSummary() {
        return summary;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean isCompleteProject() {
        return "COMPLETE_PROJECT".equalsIgnoreCase(uploadMode);
    }
}
