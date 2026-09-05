package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SqlInjectionRule implements SecurityRule {

    private static final Pattern SQL_CONCAT_PATTERN = Pattern.compile(
            "(?i)[\"']\\s*(?:SELECT|INSERT\\s+INTO|UPDATE|DELETE\\s+FROM|WHERE|JOIN)\\b.*[\"']\\s*\\+|" +
                    "f[\"']\\s*(?:SELECT|INSERT\\s+INTO|UPDATE|DELETE\\s+FROM|WHERE|JOIN)\\b.*\\{.*\\}"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_SQL_INJECTION_RISK";
    }

    @Override
    public String getName() {
        return "Unsafe SQL Query Construction";
    }

    @Override
    public List<Finding> evaluate(SecurityContext context) {
        List<Finding> findings = new ArrayList<>();

        if (context.getParsedFiles() == null) return findings;

        for (ParsedSourceFile file : context.getParsedFiles()) {
            List<String> lines = file.getLines();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().startsWith("//") || line.trim().startsWith("#") || line.trim().startsWith("/*")) continue;

                if (SQL_CONCAT_PATTERN.matcher(line).find()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("HIGH");
                    finding.setTitle("Unsafe SQL query concatenation detected");
                    finding.setDescription("Constructing SQL statements via dynamic string concatenation or unescaped string interpolation introduces SQL Injection risk. Use parameterized queries or prepared statements.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("SQL Query: " + line.trim());
                    finding.setConfidence("MEDIUM");
                    finding.setImpact("SQL injection allows attackers to read, modify, or delete database contents.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setInjectionRiskFindings(context.getSummary().getInjectionRiskFindings() + 1);
                }
            }
        }

        return findings;
    }
}
