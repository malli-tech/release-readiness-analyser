package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NamingRule implements QualityRule {

    private static final Set<String> ALLOWED_SHORT_VARS = Set.of(
            "i", "j", "k", "n", "x", "y", "z", "w", "e", "ex", "id", "os", "is", "db", "ip"
    );

    private static final Pattern VAR_DECLARATION = Pattern.compile(
            "\\b(int|long|double|float|String|boolean|var|let|const|auto)\\s+([a-zA-Z])\\s*(=|;)"
    );

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_POOR_NAMING";
    }

    @Override
    public String getName() {
        return "Poor Variable Naming";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return findings;

        int count = 0;

        for (int i = 0; i < lines.size(); i++) {
            if (count >= 5) break; // Bounded finding noise

            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#") || line.contains("for(") || line.contains("for ")) {
                continue;
            }

            Matcher m = VAR_DECLARATION.matcher(line);
            if (m.find()) {
                String varName = m.group(2);
                if (!ALLOWED_SHORT_VARS.contains(varName.toLowerCase())) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(analysisId);
                    finding.setCategory("CODE_QUALITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("LOW");
                    finding.setTitle("Poor Variable Naming ('" + varName + "')");
                    finding.setDescription("Single-letter variable name '" + varName + "' declared at line " + (i + 1) + ". Use descriptive variable names to improve self-documentation.");
                    finding.setFilePath(relativePath);
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Variable declaration '" + sanitize(line) + "' uses non-descriptive single letter name '" + varName + "'.");
                    finding.setConfidence("MEDIUM");
                    finding.setImpact("Reduces code readability.");
                    finding.setStatus("OPEN");
                    findings.add(finding);
                    count++;
                }
            }
        }

        return findings;
    }

    private String sanitize(String line) {
        return line.length() > 60 ? line.substring(0, 60) + "..." : line;
    }
}
