package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;

public class DeepNestingRule implements QualityRule {

    private static final int MAX_NESTING_DEPTH = 4;

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_DEEP_NESTING";
    }

    @Override
    public String getName() {
        return "Deep Control Flow Nesting";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return findings;

        int currentDepth = 0;
        int maxSeenInBlock = 0;
        int maxSeenLine = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#") || line.startsWith("/*") || line.startsWith("*")) {
                continue;
            }

            boolean isControlFlow = isControlFlowStatement(line);
            if (isControlFlow) {
                currentDepth++;
                if (currentDepth > MAX_NESTING_DEPTH && currentDepth > maxSeenInBlock) {
                    maxSeenInBlock = currentDepth;
                    maxSeenLine = i + 1;
                    
                    Finding finding = new Finding();
                    finding.setAnalysisId(analysisId);
                    finding.setCategory("CODE_QUALITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("MEDIUM");
                    finding.setTitle("Deep Control Flow Nesting (Level " + currentDepth + ")");
                    finding.setDescription("Control-flow nesting depth reached level " + currentDepth + " at line " + (i + 1) + ", exceeding maximum limit of " + MAX_NESTING_DEPTH + ". Deeply nested code is error-prone.");
                    finding.setFilePath(relativePath);
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Control statement '" + sanitizeLine(line) + "' nested at depth " + currentDepth + ".");
                    finding.setConfidence("MEDIUM");
                    finding.setImpact("Significantly increases cyclomatic complexity.");
                    finding.setStatus("OPEN");
                    findings.add(finding);
                }
            }

            for (char c : line.toCharArray()) {
                if (c == '}') {
                    currentDepth = Math.max(0, currentDepth - 1);
                }
            }
        }

        return findings;
    }

    private boolean isControlFlowStatement(String line) {
        return line.startsWith("if ") || line.startsWith("if(") ||
                line.startsWith("else if") || line.startsWith("for ") ||
                line.startsWith("for(") || line.startsWith("while ") ||
                line.startsWith("while(") || line.startsWith("switch ") ||
                line.startsWith("switch(") || line.startsWith("try ") ||
                line.startsWith("try{") || line.startsWith("except ") ||
                line.startsWith("except:");
    }

    private String sanitizeLine(String line) {
        if (line.length() > 60) {
            return line.substring(0, 60) + "...";
        }
        return line;
    }
}
