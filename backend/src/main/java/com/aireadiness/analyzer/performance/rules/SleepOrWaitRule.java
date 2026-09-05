package com.aireadiness.analyzer.performance.rules;

import com.aireadiness.analyzer.performance.PerformanceContext;
import com.aireadiness.analyzer.performance.PerformanceRule;
import com.aireadiness.analyzer.performance.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SleepOrWaitRule implements PerformanceRule {

    private static final Pattern SLEEP_PATTERN = Pattern.compile(
            "(?i)\\b(Thread\\.sleep|time\\.sleep|TimeUnit\\.\\w+\\.sleep|usleep|Task\\.Delay|sleep)\\s*\\("
    );

    @Override
    public String getRuleId() {
        return "PERFORMANCE_SLEEP_OR_WAIT";
    }

    @Override
    public String getName() {
        return "Artificial Sleep or Wait Delay";
    }

    @Override
    public List<Finding> evaluate(PerformanceContext context) {
        List<Finding> findings = new ArrayList<>();

        for (ParsedSourceFile file : context.getParsedFiles()) {
            List<String> lines = file.getLines();
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

                // Check if sleep pattern matches
                if (SLEEP_PATTERN.matcher(trimmed).find()) {
                    // Ignore string literal matches e.g. "Thread.sleep is bad"
                    if (isInsideStringLiteral(trimmed, "sleep")) {
                        continue;
                    }

                    Finding f = new Finding();
                    f.setAnalysisId(context.getAnalysisId());
                    f.setCategory("PERFORMANCE");
                    f.setRuleId(getRuleId());
                    f.setSeverity("MEDIUM");
                    f.setTitle("Artificial Sleep or Blocking Delay Detected");
                    f.setDescription("Artificial sleep or thread wait call detected. Explicit sleep calls hold execution threads idle, reducing system throughput and tying up worker pool capacity.");
                    f.setFilePath(file.getRelativePath());
                    f.setLineNumber(i + 1);
                    f.setEvidence(trimmed);
                    f.setConfidence("HIGH");
                    f.setImpact("Hardcoded delays cause unnecessary thread blocking and degrade application concurrency performance.");
                    f.setStatus("OPEN");
                    findings.add(f);
                }
            }
        }

        return findings;
    }

    private boolean isInsideStringLiteral(String line, String keyword) {
        int idx = line.toLowerCase().indexOf(keyword.toLowerCase());
        if (idx < 0) return false;
        int doubleQuotesBefore = 0;
        int singleQuotesBefore = 0;
        for (int i = 0; i < idx; i++) {
            if (line.charAt(i) == '"' && (i == 0 || line.charAt(i - 1) != '\\')) doubleQuotesBefore++;
            if (line.charAt(i) == '\'' && (i == 0 || line.charAt(i - 1) != '\\')) singleQuotesBefore++;
        }
        return (doubleQuotesBefore % 2 != 0) || (singleQuotesBefore % 2 != 0);
    }
}
