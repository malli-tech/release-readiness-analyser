package com.aireadiness.analyzer;

import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.nio.file.Path;
import java.util.List;

public interface Analyzer {

    String getType();

    List<Finding> analyze(Path workspaceDir, ProjectProfile profile, String analysisId, String uploadMode, List<String> warnings);
}
