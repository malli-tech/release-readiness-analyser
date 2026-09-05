package com.aireadiness.analyzer.performance.rules;

import com.aireadiness.analyzer.performance.PerformanceContext;
import com.aireadiness.analyzer.performance.PerformanceRule;
import com.aireadiness.analyzer.performance.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NPlusOneQueryRule implements PerformanceRule {

    private static final Pattern LOOP_PATTERN = Pattern.compile(
            "(?i)\\b(for|while|forEach)\\b|\\.forEach\\b|\\bfor\\s+.*\\s+in\\b"
    );

    private static final Pattern N_PLUS_ONE_PATTERN = Pattern.compile(
            "(?i)\\b(repository|dao|entitymanager|userrepository|itemrepository|productrepository|accountrepository|orderrepository)\\.(find|get|select|query|fetch)\\w*\\s*\\(|\\b(user|product|account|order|item)model\\.(find|query|get)\\w*\\s*\\(|\\bobjects\\.(get|filter)\\s*\\(|\\bselect\\s+.*\\s+from\\b"
    );

    @Override
    public String getRuleId() {
        return "PERFORMANCE_N_PLUS_ONE_QUERY";
    }

    @Override
    public String getName() {
        return "Potential N+1 Query Pattern";
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
                    if (N_PLUS_ONE_PATTERN.matcher(trimmed).find()) {
                        Finding f = new Finding();
                        f.setAnalysisId(context.getAnalysisId());
                        f.setCategory("PERFORMANCE");
                        f.setRuleId(getRuleId());
                        f.setSeverity("HIGH");
                        f.setTitle("Potential N+1 Query Pattern Detected");
                        f.setDescription("Potential N+1 database query execution detected inside a loop. Querying the database inside an iteration leads to N+1 network roundtrips and severe latency degradation.");
                        f.setFilePath(file.getRelativePath());
                        f.setLineNumber(i + 1);
                        f.setEvidence(trimmed);
                        f.setConfidence("HIGH");
                        f.setImpact("Executing individual database queries inside loops causes exponential database round-trips (O(N)), impacting API response time and database CPU.");
                        f.setStatus("OPEN");
                        findings.add(f);
                    }
                }

                // Simple brace tracking for loop exit heuristic
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
