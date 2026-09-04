package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LongMethodRule implements QualityRule {

    private static final int MAX_METHOD_LINES = 100;
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "\\b(public|private|protected|static|async|def|func|function)?\\s+([\\w<>\\[\\]]+\\s+)?(\\w+)\\s*\\([^)]*\\)\\s*(\\{|:|->)?"
    );

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_LONG_METHOD";
    }

    @Override
    public String getName() {
        return "Long Method or Function";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return findings;

        int currentMethodStartLine = -1;
        String currentMethodName = null;
        int nonBlankLineCount = 0;
        int braceDepth = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("/*") || trimmed.startsWith("*")) {
                continue;
            }

            Matcher m = METHOD_PATTERN.matcher(trimmed);
            if (m.find() && (trimmed.contains("{") || trimmed.endsWith(":") || trimmed.contains("def ") || trimmed.contains("func ") || trimmed.contains("function"))) {
                String methodName = m.group(3);
                if (methodName != null && !Set.of("if", "for", "while", "switch", "catch", "else", "try").contains(methodName)) {
                    if (currentMethodStartLine != -1 && nonBlankLineCount > MAX_METHOD_LINES) {
                        findings.add(createFinding(analysisId, relativePath, currentMethodStartLine, currentMethodName, nonBlankLineCount));
                    }
                    currentMethodStartLine = i + 1;
                    currentMethodName = methodName;
                    nonBlankLineCount = 0;
                }
            }

            if (currentMethodStartLine != -1) {
                nonBlankLineCount++;
                for (char c : trimmed.toCharArray()) {
                    if (c == '{') braceDepth++;
                    else if (c == '}') braceDepth--;
                }
                if (braceDepth <= 0 && line.contains("}") && nonBlankLineCount > 1) {
                    if (nonBlankLineCount > MAX_METHOD_LINES) {
                        findings.add(createFinding(analysisId, relativePath, currentMethodStartLine, currentMethodName, nonBlankLineCount));
                    }
                    currentMethodStartLine = -1;
                    currentMethodName = null;
                    nonBlankLineCount = 0;
                }
            }
        }

        if (currentMethodStartLine != -1 && nonBlankLineCount > MAX_METHOD_LINES) {
            findings.add(createFinding(analysisId, relativePath, currentMethodStartLine, currentMethodName, nonBlankLineCount));
        }

        return findings;
    }

    private Finding createFinding(String analysisId, String relativePath, int lineNum, String methodName, int lineCount) {
        Finding finding = new Finding();
        finding.setAnalysisId(analysisId);
        finding.setCategory("CODE_QUALITY");
        finding.setRuleId(getRuleId());
        finding.setSeverity("MEDIUM");
        finding.setTitle("Long Method/Function (" + (methodName != null ? methodName : "anonymous") + ")");
        finding.setDescription("Method '" + (methodName != null ? methodName : "anonymous") + "' contains " + lineCount + " non-blank lines, exceeding the recommended limit of " + MAX_METHOD_LINES + " lines. Large methods are harder to understand, test, and maintain.");
        finding.setFilePath(relativePath);
        finding.setLineNumber(lineNum);
        finding.setEvidence("Method '" + (methodName != null ? methodName : "anonymous") + "' at line " + lineNum + " contains approximately " + lineCount + " non-blank lines.");
        finding.setConfidence("HIGH");
        finding.setImpact("Reduces maintainability and testability.");
        finding.setStatus("OPEN");
        return finding;
    }
}
