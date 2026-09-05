package com.aireadiness.analyzer.performance.rules;

import com.aireadiness.analyzer.performance.PerformanceContext;
import com.aireadiness.analyzer.performance.PerformanceRule;
import com.aireadiness.analyzer.performance.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CollectionAllocationInLoopRule implements PerformanceRule {

    private static final Pattern LOOP_PATTERN = Pattern.compile(
            "(?i)\\b(for|while|forEach)\\b|\\.forEach\\b|\\bfor\\s+.*\\s+in\\b"
    );

    private static final Pattern COLLECTION_ALLOCATION_PATTERN = Pattern.compile(
            "(?i)new\\s+(ArrayList|HashMap|HashSet|LinkedList|TreeMap|TreeSet|ArrayDeque|Vector|Hashtable)\\b"
    );

    @Override
    public String getRuleId() {
        return "PERFORMANCE_LARGE_COLLECTION_ALLOCATION_IN_LOOP";
    }

    @Override
    public String getName() {
        return "Collection Allocation Inside Loop";
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
                    if (COLLECTION_ALLOCATION_PATTERN.matcher(trimmed).find()) {
                        Finding f = new Finding();
                        f.setAnalysisId(context.getAnalysisId());
                        f.setCategory("PERFORMANCE");
                        f.setRuleId(getRuleId());
                        f.setSeverity("LOW");
                        f.setTitle("Collection Object Allocation Inside Loop");
                        f.setDescription("Repeated collection instantiation (new ArrayList, HashMap, etc.) detected inside a loop. Instantiating new collection objects in loop iterations generates excessive short-lived objects and triggers frequent GC pauses.");
                        f.setFilePath(file.getRelativePath());
                        f.setLineNumber(i + 1);
                        f.setEvidence(trimmed);
                        f.setConfidence("HIGH");
                        f.setImpact("Creates unnecessary heap object allocations and increases JVM garbage collection overhead.");
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
