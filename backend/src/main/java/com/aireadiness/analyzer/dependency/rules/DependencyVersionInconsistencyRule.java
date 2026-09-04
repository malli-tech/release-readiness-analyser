package com.aireadiness.analyzer.dependency.rules;

import com.aireadiness.analyzer.dependency.DependencyContext;
import com.aireadiness.analyzer.dependency.DependencyRule;
import com.aireadiness.analyzer.dependency.model.DependencyInfo;
import com.aireadiness.model.Finding;

import java.util.*;

public class DependencyVersionInconsistencyRule implements DependencyRule {

    @Override
    public String getRuleId() {
        return "DEPENDENCY_VERSION_INCONSISTENCY";
    }

    @Override
    public String getName() {
        return "Dependency Version Inconsistency";
    }

    @Override
    public List<Finding> evaluate(DependencyContext context) {
        List<Finding> findings = new ArrayList<>();

        if (context.getAllDependencies() == null) return findings;

        // Group by dependency name (case-insensitive) -> Map of manifestPath -> declared version
        Map<String, Map<String, String>> nameToManifestVersions = new HashMap<>();
        Map<String, String> originalNames = new HashMap<>();

        for (DependencyInfo dep : context.getAllDependencies()) {
            if (dep.getName() == null || dep.getVersion() == null || dep.getVersion().isBlank()) continue;
            if ("UNPINNED".equalsIgnoreCase(dep.getVersionType()) || "UNKNOWN".equalsIgnoreCase(dep.getVersionType())) continue;

            String lowerName = dep.getName().toLowerCase();
            originalNames.putIfAbsent(lowerName, dep.getName());

            nameToManifestVersions.computeIfAbsent(lowerName, k -> new HashMap<>())
                    .put(dep.getManifestPath(), dep.getVersion());
        }

        for (Map.Entry<String, Map<String, String>> entry : nameToManifestVersions.entrySet()) {
            Map<String, String> manifestVersions = entry.getValue();

            // Only evaluate if declared across multiple distinct manifests with differing versions
            if (manifestVersions.size() > 1) {
                Set<String> distinctVersions = new HashSet<>(manifestVersions.values());
                if (distinctVersions.size() > 1) {
                    String name = originalNames.get(entry.getKey());
                    StringBuilder evidence = new StringBuilder("Dependency: ").append(name).append("\nDeclared Versions across Manifests:\n");
                    for (Map.Entry<String, String> mv : manifestVersions.entrySet()) {
                        evidence.append(" - ").append(mv.getKey()).append(": ").append(mv.getValue()).append("\n");
                    }

                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("DEPENDENCY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("MEDIUM");
                    finding.setTitle("Inconsistent dependency versions across manifests");
                    finding.setDescription("The dependency '" + name + "' has inconsistent version declarations across multiple project manifests: " + distinctVersions);
                    finding.setFilePath(manifestVersions.keySet().iterator().next());
                    finding.setLineNumber(null);
                    finding.setEvidence(evidence.toString().trim());
                    finding.setConfidence("HIGH");
                    finding.setImpact("Inconsistent versions across microservices or multi-module projects can lead to unexpected runtime behavior or API incompatibilities.");
                    finding.setStatus("OPEN");
                    findings.add(finding);
                }
            }
        }

        return findings;
    }
}
