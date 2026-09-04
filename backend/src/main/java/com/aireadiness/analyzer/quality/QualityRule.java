package com.aireadiness.analyzer.quality;

import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.List;

public interface QualityRule {

    String getRuleId();

    String getName();

    List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId);
}
