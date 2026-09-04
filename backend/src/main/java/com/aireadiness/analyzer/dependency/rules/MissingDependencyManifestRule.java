package com.aireadiness.analyzer.dependency.rules;

import com.aireadiness.analyzer.dependency.DependencyContext;
import com.aireadiness.analyzer.dependency.DependencyRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;

public class MissingDependencyManifestRule implements DependencyRule {

    @Override
    public String getRuleId() {
        return "DEPENDENCY_NO_MANIFEST";
    }

    @Override
    public String getName() {
        return "No Dependency Manifest Found";
    }

    @Override
    public List<Finding> evaluate(DependencyContext context) {
        List<Finding> findings = new ArrayList<>();

        ProjectProfile profile = context.getProfile();

        // If unsupported or unknown ecosystem, do not generate false positive missing manifest finding
        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            return findings;
        }

        boolean hasManifests = context.getManifests() != null && !context.getManifests().isEmpty();

        if (!hasManifests) {
            if ("SELECTED_CONTENT".equalsIgnoreCase(context.getUploadMode())) {
                // In selected content mode, do NOT generate a DEPENDENCY_NO_MANIFEST finding.
                // Limitation warning is added to summary.
                return findings;
            }

            // COMPLETE_PROJECT mode with no manifests found
            Finding finding = new Finding();
            finding.setAnalysisId(context.getAnalysisId());
            finding.setCategory("DEPENDENCY");
            finding.setRuleId(getRuleId());
            finding.setSeverity("MEDIUM");
            finding.setTitle("No supported dependency manifest found");
            finding.setDescription("No supported dependency manifest was found for this project. Dependency completeness and version management cannot be established.");
            finding.setEvidence("Project directory contains source code but no supported manifest file (pom.xml, build.gradle, package.json, requirements.txt, pyproject.toml, Pipfile, go.mod, *.csproj, composer.json).");
            finding.setConfidence("HIGH");
            finding.setImpact("Lack of manifest declaration makes dependency reproducibility impossible.");
            finding.setStatus("OPEN");
            findings.add(finding);
        }

        return findings;
    }
}
