package com.aireadiness.analyzer.dependency.rules;

import com.aireadiness.analyzer.dependency.DependencyContext;
import com.aireadiness.analyzer.dependency.DependencyRule;
import com.aireadiness.analyzer.dependency.model.DependencyManifestInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class DependencyManifestWarningRule implements DependencyRule {

    @Override
    public String getRuleId() {
        return "DEPENDENCY_MANIFEST_WARNING";
    }

    @Override
    public String getName() {
        return "Dependency Manifest Warning";
    }

    @Override
    public List<Finding> evaluate(DependencyContext context) {
        List<Finding> findings = new ArrayList<>();

        if (context.getManifests() == null) return findings;

        for (DependencyManifestInfo manifest : context.getManifests()) {
            if (manifest.isMalformed()) {
                Finding finding = new Finding();
                finding.setAnalysisId(context.getAnalysisId());
                finding.setCategory("DEPENDENCY");
                finding.setRuleId(getRuleId());
                finding.setSeverity("LOW");
                finding.setTitle("Dependency manifest parsing warning");
                finding.setDescription("The dependency manifest '" + manifest.getManifestPath() + "' could only be partially parsed due to syntax errors or unsupported constructs.");
                finding.setFilePath(manifest.getManifestPath());
                finding.setLineNumber(null);
                finding.setEvidence("Manifest: " + manifest.getManifestPath() + "\nWarnings: " + String.join("; ", manifest.getWarnings()));
                finding.setConfidence("MEDIUM");
                finding.setImpact("Partially parsed manifests may prevent full static dependency inspection.");
                finding.setStatus("OPEN");
                findings.add(finding);
            }
        }

        return findings;
    }
}
