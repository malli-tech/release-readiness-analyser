package com.aireadiness.analyzer.performance.rules;

import com.aireadiness.analyzer.performance.PerformanceContext;
import com.aireadiness.analyzer.performance.PerformanceRule;
import com.aireadiness.analyzer.performance.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DatabaseCallInLoopRule implements PerformanceRule {

    private static final Pattern LOOP_PATTERN = Pattern.compile(
            "(?i)\\b(for|while|forEach)\\b|\\.forEach\\b|\\bfor\\s+.*\\s+in\\b"
    );

    private static final Pattern DB_CALL_PATTERN = Pattern.compile(
            "(?i)\\b(\\w*repository|\\w*dao|jdbcTemplate|mongoTemplate|db|entityManager|session|\\w*model)\\.(save|update|delete|insert|execute|persist|merge|remove|create|upsert)\\w*\\s*\\("
    );

    @Override
    public String getRuleId() {
        return "PERFORMANCE_DATABASE_CALL_IN_LOOP";
    }

    @Override
    public String getName() {
        return "Database Operation Inside Loop";
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
                    if (DB_CALL_PATTERN.matcher(trimmed).find()) {
                        Finding f = new Finding();
                        f.setAnalysisId(context.getAnalysisId());
                        f.setCategory("PERFORMANCE");
                        f.setRuleId(getRuleId());
                        f.setSeverity("MEDIUM");
                        f.setTitle("Database Call Inside Loop");
                        f.setDescription("Potential database call inside a loop. Executing persistence operations in loops can cause transaction overhead and excessive database network calls.");
                        f.setFilePath(file.getRelativePath());
                        f.setLineNumber(i + 1);
                        f.setEvidence(trimmed);
                        f.setConfidence("HIGH");
                        f.setImpact("Performing save, update, or delete operations repeatedly inside loops leads to resource contention and high response latency.");
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
