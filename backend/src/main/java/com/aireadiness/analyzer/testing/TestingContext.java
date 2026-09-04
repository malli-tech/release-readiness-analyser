package com.aireadiness.analyzer.testing;

import com.aireadiness.analyzer.testing.model.ParsedFileInfo;
import com.aireadiness.model.ProjectProfile;
import com.aireadiness.model.TestingSummary;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TestingContext {

    private final Path workspaceDir;
    private final ProjectProfile profile;
    private final String analysisId;
    private final String uploadMode;
    private final List<ParsedFileInfo> allFiles;
    private final List<ParsedFileInfo> testFiles;
    private final List<ParsedFileInfo> sourceFiles;
    private final TestingSummary summary;
    private final List<String> warnings;

    public TestingContext(
            Path workspaceDir,
            ProjectProfile profile,
            String analysisId,
            String uploadMode,
            List<ParsedFileInfo> allFiles,
            List<ParsedFileInfo> testFiles,
            List<ParsedFileInfo> sourceFiles,
            TestingSummary summary,
            List<String> warnings
    ) {
        this.workspaceDir = workspaceDir;
        this.profile = profile;
        this.analysisId = analysisId;
        this.uploadMode = uploadMode != null ? uploadMode : "COMPLETE_PROJECT";
        this.allFiles = allFiles != null ? allFiles : new ArrayList<>();
        this.testFiles = testFiles != null ? testFiles : new ArrayList<>();
        this.sourceFiles = sourceFiles != null ? sourceFiles : new ArrayList<>();
        this.summary = summary != null ? summary : new TestingSummary();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
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

    public boolean isCompleteProject() {
        return "COMPLETE_PROJECT".equalsIgnoreCase(uploadMode);
    }

    public boolean isSelectedContent() {
        return "SELECTED_CONTENT".equalsIgnoreCase(uploadMode);
    }

    public List<ParsedFileInfo> getAllFiles() {
        return allFiles;
    }

    public List<ParsedFileInfo> getTestFiles() {
        return testFiles;
    }

    public List<ParsedFileInfo> getSourceFiles() {
        return sourceFiles;
    }

    public TestingSummary getSummary() {
        return summary;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
