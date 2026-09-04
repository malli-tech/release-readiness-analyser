package com.aireadiness.analyzer.dependency.rules;

import com.aireadiness.analyzer.dependency.DependencyContext;
import com.aireadiness.analyzer.dependency.DependencyRule;
import com.aireadiness.analyzer.dependency.model.DependencyInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuplicateDependencyRule implements DependencyRule {

    @Override
    public String getRuleId() {
        return "DEPENDENCY_DUPLICATE";
    }

    @Override
    public String getName() {
        return "Duplicate Dependency Declaration";
    }

    @Override
    public List<Finding> evaluate(DependencyContext context) {
        List<Finding> findings = new ArrayList<>();

        if (context.getAllDependencies() == null) return findings;

        // Key: manifestPath + ":" + scope + ":" + name.toLowerCase()
        Map<String, List<DependencyInfo>> map = new HashMap<>();

        for (DependencyInfo dep : context.getAllDependencies()) {
            if (dep.getName() == null) continue;
            String key = (dep.getManifestPath() != null ? dep.getManifestPath() : "") + ":" +
                    (dep.getScope() != null ? dep.getScope() : "") + ":" +
                    dep.getName().toLowerCase();
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(dep);
        }

        for (List<DependencyInfo> duplicates : map.values()) {
            if (duplicates.size() > 1) {
                DependencyInfo first = duplicates.get(0);
                Finding finding = new Finding();
                finding.setAnalysisId(context.getAnalysisId());
                finding.setCategory("DEPENDENCY");
                finding.setRuleId(getRuleId());
                finding.setSeverity("MEDIUM");
                finding.setTitle("Duplicate dependency declaration found");
                finding.setDescription("The dependency '" + first.getName() + "' is declared " + duplicates.size() + " times in scope '" + first.getScope() + "' within manifest '" + first.getManifestPath() + "'.");
                finding.setFilePath(first.getManifestPath());
                finding.setLineNumber(first.getLineNumber() > 0 ? first.getLineNumber() : null);
                finding.setEvidence("Dependency: " + first.getName() + "\nScope: " + first.getScope() + "\nManifest: " + first.getManifestPath() + "\nOccurrences: " + duplicates.size());
                finding.setConfidence("HIGH");
                finding.setImpact("Duplicate dependency entries create build tool confusion and maintenance overhead.");
                finding.setStatus("OPEN");
                findings.add(finding);
            }
        }

        return findings;
    }
}
