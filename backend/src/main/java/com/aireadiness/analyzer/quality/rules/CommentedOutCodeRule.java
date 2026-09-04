package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;

public class CommentedOutCodeRule implements QualityRule {

    private static final int MIN_COMMENTED_LINES = 3;

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_COMMENTED_OUT_CODE";
    }

    @Override
    public String getName() {
        return "Commented-Out Code";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return findings;

        int consecutiveCommentedCodeLines = 0;
        int blockStartLine = -1;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (isCommentedOutCodeLine(line)) {
                if (consecutiveCommentedCodeLines == 0) {
                    blockStartLine = i + 1;
                }
                consecutiveCommentedCodeLines++;
            } else {
                if (consecutiveCommentedCodeLines >= MIN_COMMENTED_LINES) {
                    findings.add(createFinding(analysisId, relativePath, blockStartLine, consecutiveCommentedCodeLines));
                }
                consecutiveCommentedCodeLines = 0;
                blockStartLine = -1;
            }
        }

        if (consecutiveCommentedCodeLines >= MIN_COMMENTED_LINES) {
            findings.add(createFinding(analysisId, relativePath, blockStartLine, consecutiveCommentedCodeLines));
        }

        return findings;
    }

    private boolean isCommentedOutCodeLine(String line) {
        if (line.startsWith("/**") || line.startsWith("*") || line.startsWith("///")) {
            return false; // Exclude Javadoc / JSDoc / Docstrings
        }
        if (line.startsWith("//") || line.startsWith("#")) {
            String content = line.substring(line.startsWith("//") ? 2 : 1).trim();
            // Check for code structures
            return content.contains(";") || content.contains("{") || content.contains("}") ||
                    content.startsWith("if ") || content.startsWith("if(") ||
                    content.startsWith("for ") || content.startsWith("for(") ||
                    content.startsWith("return ") || content.startsWith("var ") ||
                    content.startsWith("let ") || content.startsWith("const ") ||
                    content.startsWith("import ") || content.startsWith("System.out.");
        }
        return false;
    }

    private Finding createFinding(String analysisId, String relativePath, int startLine, int lineCount) {
        Finding finding = new Finding();
        finding.setAnalysisId(analysisId);
        finding.setCategory("CODE_QUALITY");
        finding.setRuleId(getRuleId());
        finding.setSeverity("LOW");
        finding.setTitle("Commented-Out Code Block");
        finding.setDescription("A block of " + lineCount + " commented-out code lines starts at line " + startLine + ". Dead/commented code adds clutter and should be removed prior to release.");
        finding.setFilePath(relativePath);
        finding.setLineNumber(startLine);
        finding.setEvidence("Block of " + lineCount + " commented code lines detected starting at line " + startLine + ".");
        finding.setConfidence("MEDIUM");
        finding.setImpact("Increases code debt and clutter.");
        finding.setStatus("OPEN");
        return finding;
    }
}
