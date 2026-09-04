package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodoFixmeRule implements QualityRule {

    private static final Pattern TODO_PATTERN = Pattern.compile("(?i)\\b(TODO|FIXME|XXX)\\b");

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_TODO_FIXME";
    }

    @Override
    public String getName() {
        return "TODO / FIXME / XXX Comment Marker";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return findings;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            if (trimmed.startsWith("class ") || trimmed.startsWith("public class") || trimmed.startsWith("interface ") || trimmed.startsWith("def ") || trimmed.startsWith("function ")) {
                continue; // Ignore type declarations containing TODO/FIXME in name
            }

            Matcher matcher = TODO_PATTERN.matcher(trimmed);
            if (matcher.find()) {
                String marker = matcher.group(1).toUpperCase();

                Finding finding = new Finding();
                finding.setAnalysisId(analysisId);
                finding.setCategory("CODE_QUALITY");
                finding.setRuleId(getRuleId());
                finding.setSeverity("LOW");
                finding.setTitle("Unresolved " + marker + " Comment");
                finding.setDescription("Unresolved code task marker (" + marker + ") found at line " + (i + 1) + ". Unfinished tasks or workaround markers should be resolved before release.");
                finding.setFilePath(relativePath);
                finding.setLineNumber(i + 1);
                finding.setEvidence("Line " + (i + 1) + ": " + sanitizeSnippet(trimmed));
                finding.setConfidence("HIGH");
                finding.setImpact("Indicates technical debt or incomplete implementation.");
                finding.setStatus("OPEN");
                findings.add(finding);
            }
        }

        return findings;
    }

    private String sanitizeSnippet(String snippet) {
        if (snippet.length() > 80) {
            return snippet.substring(0, 80) + "...";
        }
        return snippet;
    }
}
