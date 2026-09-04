package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.*;

public class DuplicateCodeRule implements QualityRule {

    private static final int DUPLICATE_BLOCK_SIZE = 6;

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_DUPLICATED_CODE";
    }

    @Override
    public String getName() {
        return "Duplicated Code Sequence";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.size() < DUPLICATE_BLOCK_SIZE) return findings;

        // Clean & normalize lines
        List<NormalizedLine> normLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String raw = lines.get(i).trim();
            if (!raw.isEmpty() && !raw.startsWith("//") && !raw.startsWith("#") && !raw.startsWith("/*") && !raw.startsWith("*")) {
                normLines.add(new NormalizedLine(i + 1, raw.replaceAll("\\s+", " ")));
            }
        }

        if (normLines.size() < DUPLICATE_BLOCK_SIZE) return findings;

        Map<String, Integer> seenBlocks = new HashMap<>();
        Set<Integer> reportedLines = new HashSet<>();

        for (int i = 0; i <= normLines.size() - DUPLICATE_BLOCK_SIZE; i++) {
            StringBuilder blockKey = new StringBuilder();
            for (int j = 0; j < DUPLICATE_BLOCK_SIZE; j++) {
                blockKey.append(normLines.get(i + j).content).append("\n");
            }
            String key = blockKey.toString();

            if (seenBlocks.containsKey(key)) {
                int firstOccurrenceLine = seenBlocks.get(key);
                int currentOccurrenceLine = normLines.get(i).lineNum;

                if (!reportedLines.contains(currentOccurrenceLine)) {
                    reportedLines.add(currentOccurrenceLine);

                    Finding finding = new Finding();
                    finding.setAnalysisId(analysisId);
                    finding.setCategory("CODE_QUALITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("MEDIUM");
                    finding.setTitle("Duplicated Code Block");
                    finding.setDescription("A sequence of " + DUPLICATE_BLOCK_SIZE + " identical lines at line " + currentOccurrenceLine + " duplicates code found earlier at line " + firstOccurrenceLine + ". Consider refactoring into a reusable helper method.");
                    finding.setFilePath(relativePath);
                    finding.setLineNumber(currentOccurrenceLine);
                    finding.setEvidence("Identical 6-line code block matches lines starting at line " + firstOccurrenceLine + ".");
                    finding.setConfidence("MEDIUM");
                    finding.setImpact("Increases code maintenance overhead.");
                    finding.setStatus("OPEN");
                    findings.add(finding);
                }
            } else {
                seenBlocks.put(key, normLines.get(i).lineNum);
            }
        }

        return findings;
    }

    private static class NormalizedLine {
        final int lineNum;
        final String content;

        NormalizedLine(int lineNum, String content) {
            this.lineNum = lineNum;
            this.content = content;
        }
    }
}
