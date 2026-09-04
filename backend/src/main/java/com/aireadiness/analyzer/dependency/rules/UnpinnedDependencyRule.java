package com.aireadiness.analyzer.dependency.rules;

import com.aireadiness.analyzer.dependency.DependencyContext;
import com.aireadiness.analyzer.dependency.DependencyRule;
import com.aireadiness.analyzer.dependency.model.DependencyInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class UnpinnedDependencyRule implements DependencyRule {

    @Override
    public String getRuleId() {
        return "DEPENDENCY_UNPINNED_VERSION";
    }

    @Override
    public String getName() {
        return "Unpinned Dependency Version";
    }

    @Override
    public List<Finding> evaluate(DependencyContext context) {
        List<Finding> findings = new ArrayList<>();

        if (context.getAllDependencies() == null) return findings;

        for (DependencyInfo dep : context.getAllDependencies()) {
            if ("UNPINNED".equalsIgnoreCase(dep.getVersionType()) || dep.getVersion() == null || dep.getVersion().isBlank()) {
                Finding finding = new Finding();
                finding.setAnalysisId(context.getAnalysisId());
                finding.setCategory("DEPENDENCY");
                finding.setRuleId(getRuleId());
                finding.setSeverity("MEDIUM");
                finding.setTitle("Unpinned dependency version declared");
                finding.setDescription("The dependency '" + dep.getName() + "' does not specify an explicit version constraint, which may reduce build reproducibility and make builds more sensitive to upstream changes.");
                finding.setFilePath(dep.getManifestPath());
                finding.setLineNumber(dep.getLineNumber() > 0 ? dep.getLineNumber() : null);
                finding.setEvidence("Dependency: " + dep.getName() + "\nManifest: " + dep.getManifestPath() + "\nDeclared Version: None specified");
                finding.setConfidence("HIGH");
                finding.setImpact("Unpinned dependencies can cause unexpected build failures when upstream releases breaking changes.");
                finding.setStatus("OPEN");
                findings.add(finding);
            }
        }

        return findings;
    }
}
