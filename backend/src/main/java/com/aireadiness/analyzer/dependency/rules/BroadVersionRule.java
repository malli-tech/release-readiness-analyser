package com.aireadiness.analyzer.dependency.rules;

import com.aireadiness.analyzer.dependency.DependencyContext;
import com.aireadiness.analyzer.dependency.DependencyRule;
import com.aireadiness.analyzer.dependency.model.DependencyInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class BroadVersionRule implements DependencyRule {

    @Override
    public String getRuleId() {
        return "DEPENDENCY_BROAD_VERSION_RANGE";
    }

    @Override
    public String getName() {
        return "Broad Dependency Version Range";
    }

    @Override
    public List<Finding> evaluate(DependencyContext context) {
        List<Finding> findings = new ArrayList<>();

        if (context.getAllDependencies() == null) return findings;

        for (DependencyInfo dep : context.getAllDependencies()) {
            if ("BROAD_RANGE".equalsIgnoreCase(dep.getVersionType())) {
                Finding finding = new Finding();
                finding.setAnalysisId(context.getAnalysisId());
                finding.setCategory("DEPENDENCY");
                finding.setRuleId(getRuleId());
                finding.setSeverity("LOW");
                finding.setTitle("Broad version constraint used for dependency");
                finding.setDescription("The dependency '" + dep.getName() + "' uses an overly broad or unbounded version range (" + dep.getVersion() + "), which can introduce non-deterministic build behavior.");
                finding.setFilePath(dep.getManifestPath());
                finding.setLineNumber(dep.getLineNumber() > 0 ? dep.getLineNumber() : null);
                finding.setEvidence("Dependency: " + dep.getName() + "\nManifest: " + dep.getManifestPath() + "\nDeclared Version: " + dep.getVersion());
                finding.setConfidence("MEDIUM");
                finding.setImpact("Broad ranges make builds sensitive to unintended major/minor version updates.");
                finding.setStatus("OPEN");
                findings.add(finding);
            }
        }

        return findings;
    }
}
