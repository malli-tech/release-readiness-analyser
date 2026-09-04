package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;

public class LargeClassRule implements QualityRule {

    private static final int MAX_CLASS_LINES = 500;

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_LARGE_CLASS";
    }

    @Override
    public String getName() {
        return "Large Class or File";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.size() <= MAX_CLASS_LINES) return findings;

        int nonBlankCount = 0;
        int classDeclLine = 1;
        String className = null;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty()) {
                nonBlankCount++;
            }
            if (className == null && (line.contains("class ") || line.contains("interface ") || line.contains("type ") || line.contains("struct "))) {
                className = extractClassName(line);
                classDeclLine = i + 1;
            }
        }

        if (nonBlankCount > MAX_CLASS_LINES) {
            Finding finding = new Finding();
            finding.setAnalysisId(analysisId);
            finding.setCategory("CODE_QUALITY");
            finding.setRuleId(getRuleId());
            finding.setSeverity("MEDIUM");
            finding.setTitle("Large Class/File (" + (className != null ? className : relativePath) + ")");
            finding.setDescription("File/Class '" + (className != null ? className : relativePath) + "' contains " + nonBlankCount + " non-blank lines, exceeding the recommended limit of " + MAX_CLASS_LINES + " lines. Large files violate single-responsibility principle.");
            finding.setFilePath(relativePath);
            finding.setLineNumber(classDeclLine);
            finding.setEvidence("File contains " + lines.size() + " total lines (" + nonBlankCount + " non-blank lines).");
            finding.setConfidence("HIGH");
            finding.setImpact("Increases cognitive load and risk of regression bugs.");
            finding.setStatus("OPEN");
            findings.add(finding);
        }

        return findings;
    }

    private String extractClassName(String line) {
        String[] tokens = line.split("\\s+");
        for (int i = 0; i < tokens.length - 1; i++) {
            if (List.of("class", "interface", "struct", "type").contains(tokens[i])) {
                String name = tokens[i + 1];
                int idx = name.indexOf('{');
                if (idx != -1) name = name.substring(0, idx);
                idx = name.indexOf('(');
                if (idx != -1) name = name.substring(0, idx);
                idx = name.indexOf('<');
                if (idx != -1) name = name.substring(0, idx);
                return name;
            }
        }
        return null;
    }
}
