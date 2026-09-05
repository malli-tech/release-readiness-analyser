package com.aireadiness.analyzer.performance.rules;

import com.aireadiness.analyzer.performance.PerformanceContext;
import com.aireadiness.analyzer.performance.PerformanceRule;
import com.aireadiness.analyzer.performance.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class RepeatedExpensiveOperationRule implements PerformanceRule {

    private static final Pattern LOOP_PATTERN = Pattern.compile(
            "(?i)\\b(for|while|forEach)\\b|\\.forEach\\b|\\bfor\\s+.*\\s+in\\b"
    );

    private static final Pattern EXPENSIVE_OP_PATTERN = Pattern.compile(
            "(?i)\\b(\\w*objectmapper|mapper|gson|jackson|json)\\.(writevalueasstring|readvalue|tojson|fromjson|stringify|parse)\\b|\\b(files|fs)\\.(readalllines|readallsample|readfile|readfilesync|write)\\b|new\\s+SimpleDateFormat\\b|\\b(class\\.forname|classloader|method\\.invoke)\\b"
    );

    @Override
    public String getRuleId() {
        return "PERFORMANCE_REPEATED_EXPENSIVE_OPERATION";
    }

    @Override
    public String getName() {
        return "Repeated Expensive Operation in Loop";
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
                    if (EXPENSIVE_OP_PATTERN.matcher(trimmed).find()) {
                        Finding f = new Finding();
                        f.setAnalysisId(context.getAnalysisId());
                        f.setCategory("PERFORMANCE");
                        f.setRuleId(getRuleId());
                        f.setSeverity("MEDIUM");
                        f.setTitle("Repeated Expensive Operation Inside Loop");
                        f.setDescription("Expensive static operation (JSON serialization, disk file I/O, date formatter initialization, or reflection) detected inside a loop iteration.");
                        f.setFilePath(file.getRelativePath());
                        f.setLineNumber(i + 1);
                        f.setEvidence(trimmed);
                        f.setConfidence("HIGH");
                        f.setImpact("Performing heavy CPU/IO operations inside loops degrades execution performance. Move serialization or file initialization outside loop blocks.");
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

    private int countChar(String str, char ch) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == ch) count++;
        }
        return count;
    }
}
