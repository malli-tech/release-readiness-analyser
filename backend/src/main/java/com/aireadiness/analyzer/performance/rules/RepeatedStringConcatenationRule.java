package com.aireadiness.analyzer.performance.rules;

import com.aireadiness.analyzer.performance.PerformanceContext;
import com.aireadiness.analyzer.performance.PerformanceRule;
import com.aireadiness.analyzer.performance.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RepeatedStringConcatenationRule implements PerformanceRule {

    private static final Pattern LOOP_PATTERN = Pattern.compile(
            "(?i)\\b(for|while|forEach)\\b|\\.forEach\\b|\\bfor\\s+.*\\s+in\\b"
    );

    private static final Pattern STRING_CONCAT_PATTERN = Pattern.compile(
            "\\b([a-zA-Z_]\\w*)\\s*\\+=\\s*|\\b([a-zA-Z_]\\w*)\\s*=\\s*\\1\\s*\\+"
    );

    @Override
    public String getRuleId() {
        return "PERFORMANCE_REPEATED_STRING_CONCATENATION";
    }

    @Override
    public String getName() {
        return "Repeated String Concatenation in Loop";
    }

    @Override
    public List<Finding> evaluate(PerformanceContext context) {
        List<Finding> findings = new ArrayList<>();

        for (ParsedSourceFile file : context.getParsedFiles()) {
            List<String> lines = file.getLines();
            int loopDepth = 0;
            boolean inBlockComment = false;

            for (int i = 0; i < lines.size(); i++) {
                String rawLine = lines.get(i);
                String trimmed = rawLine.trim();

                if (trimmed.startsWith("/*")) inBlockComment = true;
                if (inBlockComment) {
                    if (trimmed.endsWith("*/") || trimmed.contains("*/")) inBlockComment = false;
                    continue;
                }
                if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("*")) {
                    continue;
                }

                if (LOOP_PATTERN.matcher(trimmed).find()) {
                    loopDepth = Math.max(1, loopDepth + 1);
                }

                if (loopDepth > 0) {
                    // Ignore numeric loops e.g. i += 1, count += 1, sum += number
                    if (STRING_CONCAT_PATTERN.matcher(trimmed).find() && isLikelyStringConcat(trimmed)) {
                        Finding f = new Finding();
                        f.setAnalysisId(context.getAnalysisId());
                        f.setCategory("PERFORMANCE");
                        f.setRuleId(getRuleId());
                        f.setSeverity("LOW");
                        f.setTitle("Repeated String Concatenation Inside Loop");
                        f.setDescription("Repeated string concatenation detected inside a loop. String objects are immutable; repeated concatenation creates multiple intermediate string allocations in memory.");
                        f.setFilePath(file.getRelativePath());
                        f.setLineNumber(i + 1);
                        f.setEvidence(trimmed);
                        f.setConfidence("MEDIUM");
                        f.setImpact("Creates O(N^2) memory allocation pressure and GC overhead during large iterations.");
                        f.setStatus("OPEN");
                        findings.add(f);
                    }
                }

                int open = countChar(trimmed, '{');
                int close = countChar(trimmed, '}');
                if (close > open && loopDepth > 0) {
                    loopDepth = Math.max(0, loopDepth - (close - open));
                }
            }
        }

        return findings;
    }

    private boolean isLikelyStringConcat(String line) {
        String lower = line.toLowerCase();
        // Skip obvious numeric additions
        if (lower.matches(".*\\b(i|j|k|n|count|sum|total|index|idx|pos|size|length)\\s*\\+=.*")) {
            return false;
        }
        return lower.contains("\"") || lower.contains("'") || lower.contains("str") || lower.contains("text") || lower.contains("msg") || lower.contains("line") || lower.contains("result") || lower.contains("buffer") || lower.contains(".tostring()");
    }

    private int countChar(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) count++;
        }
        return count;
    }
}
