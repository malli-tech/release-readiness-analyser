package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TooManyParametersRule implements QualityRule {

    private static final int MAX_PARAMS = 6;
    private static final Pattern FUNC_SIGNATURE_PATTERN = Pattern.compile(
            "\\b(public|private|protected|static|async|def|func|function)?\\s+([\\w<>\\[\\]]+\\s+)?(\\w+)\\s*\\(([^)]*)\\)"
    );

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_TOO_MANY_PARAMETERS";
    }

    @Override
    public String getName() {
        return "Too Many Parameters";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return findings;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#")) continue;

            Matcher matcher = FUNC_SIGNATURE_PATTERN.matcher(line);
            if (matcher.find()) {
                String methodName = matcher.group(3);
                String paramListStr = matcher.group(4);

                if (methodName != null && !Set.of("if", "for", "while", "switch", "catch", "else", "try").contains(methodName)) {
                    int paramCount = countParameters(paramListStr);
                    if (paramCount > MAX_PARAMS) {
                        Finding finding = new Finding();
                        finding.setAnalysisId(analysisId);
                        finding.setCategory("CODE_QUALITY");
                        finding.setRuleId(getRuleId());
                        finding.setSeverity("MEDIUM");
                        finding.setTitle("Too Many Parameters (" + methodName + ")");
                        finding.setDescription("Function '" + methodName + "' accepts " + paramCount + " parameters, exceeding the threshold of " + MAX_PARAMS + ". Consider grouping parameters into an object or class DTO.");
                        finding.setFilePath(relativePath);
                        finding.setLineNumber(i + 1);
                        finding.setEvidence("Signature '" + methodName + "(" + paramListStr + ")' contains " + paramCount + " parameters.");
                        finding.setConfidence("HIGH");
                        finding.setImpact("Complicates invocation, API stability, and unit testing.");
                        finding.setStatus("OPEN");
                        findings.add(finding);
                    }
                }
            }
        }

        return findings;
    }

    private int countParameters(String paramStr) {
        if (paramStr == null || paramStr.trim().isEmpty()) return 0;
        String[] parts = paramStr.split(",");
        return parts.length;
    }
}
