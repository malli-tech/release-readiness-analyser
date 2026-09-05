package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PermissiveCorsRule implements SecurityRule {

    private static final Pattern CORS_WILDCARD_PATTERN = Pattern.compile(
            "(?i)(Access-Control-Allow-Origin\\s*[:=,]\\s*[\"']\\*[\"']|allowedOrigins\\s*\\(\\s*[\"']\\*[\"']\\s*\\)|" +
                    "cors\\s*\\(\\s*\\{\\s*origin\\s*:\\s*[\"']\\*[\"']|cors\\(\\s*[\"']\\*[\"']\\s*\\)|setHeader\\s*\\(\\s*[\"']Access-Control-Allow-Origin[\"']\\s*,\\s*[\"']\\*[\"']\\s*\\))"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_PERMISSIVE_CORS";
    }

    @Override
    public String getName() {
        return "Permissive CORS Wildcard Policy";
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

                if (CORS_WILDCARD_PATTERN.matcher(line).find()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("MEDIUM");
                    finding.setTitle("Permissive CORS wildcard policy detected");
                    finding.setDescription("The application configures a wildcard CORS policy ('Access-Control-Allow-Origin: *'). Allowing any origin to read API responses can lead to unauthorized data disclosure across domains.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("CORS Policy: " + line.trim());
                    finding.setConfidence("HIGH");
                    finding.setImpact("Wildcard CORS policies permit malicious third-party websites to make cross-origin requests to read user data.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setConfigurationFindings(context.getSummary().getConfigurationFindings() + 1);
                }
            }
        }

        return findings;
    }
}
