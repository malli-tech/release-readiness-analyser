package com.aireadiness.analyzer.quality.rules;

import com.aireadiness.analyzer.quality.QualityRule;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MagicNumberRule implements QualityRule {

    private static final Set<String> ALLOWED_NUMBERS = Set.of(
            "0", "1", "2", "-1", "0.0", "1.0", "100", "1000", "0L", "1L", "0x00", "0xFF"
    );

    private static final Pattern NUMERIC_LITERAL_PATTERN = Pattern.compile("\\b(\\d{2,}|[3-9])\\b");

    @Override
    public String getRuleId() {
        return "CODE_QUALITY_MAGIC_NUMBER";
    }

    @Override
    public String getName() {
        return "Magic Number Literal";
    }

    @Override
    public List<Finding> evaluate(String relativePath, List<String> lines, ProjectProfile profile, String analysisId) {
        List<Finding> findings = new ArrayList<>();
        if (lines == null || lines.isEmpty()) return findings;

        int magicNumberCount = 0;

        for (int i = 0; i < lines.size(); i++) {
            if (magicNumberCount >= 5) break; // Limit finding noise per file

            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("//") || line.startsWith("#") || line.startsWith("/*") || line.startsWith("*") || line.contains("static final") || line.contains("const ") || line.contains("enum ")) {
                continue;
            }

            Matcher matcher = NUMERIC_LITERAL_PATTERN.matcher(line);
            while (matcher.find()) {
                String val = matcher.group(1);
                if (!ALLOWED_NUMBERS.contains(val)) {
                    // Filter out numbers in array index, loop, or version string contexts
                    if (line.contains("for(") || line.contains("for ") || line.contains("version") || line.contains("port") || line.contains("http")) {
                        continue;
                    }

                    Finding finding = new Finding();
                    finding.setAnalysisId(analysisId);
                    finding.setCategory("CODE_QUALITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("LOW");
                    finding.setTitle("Magic Number Literal (" + val + ")");
                    finding.setDescription("Unnamed numeric literal '" + val + "' found at line " + (i + 1) + ". Replace magic numbers with named constants to improve code readability.");
                    finding.setFilePath(relativePath);
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Numeric literal '" + val + "' used directly in line: '" + sanitize(line) + "'.");
                    finding.setConfidence("MEDIUM");
                    finding.setImpact("Obscures business logic intent and hinders refactoring.");
                    finding.setStatus("OPEN");
                    findings.add(finding);
                    magicNumberCount++;
                    break;
                }
            }
        }

        return findings;
    }

    private String sanitize(String line) {
        return line.length() > 60 ? line.substring(0, 60) + "..." : line;
    }
}
