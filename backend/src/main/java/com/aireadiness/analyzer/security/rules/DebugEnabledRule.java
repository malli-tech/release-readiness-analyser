package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DebugEnabledRule implements SecurityRule {

    private static final Pattern DEBUG_PATTERN = Pattern.compile(
            "(?i)(^\\s*DEBUG\\s*=\\s*True\\b|app\\.debug\\s*=\\s*true|server\\.error\\.include-stacktrace\\s*=\\s*always|" +
                    "display_errors\\s*=\\s*On|NODE_ENV\\s*=\\s*['\"]development['\"])"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_DEBUG_ENABLED";
    }

    @Override
    public String getName() {
        return "Insecure Debug Mode Configuration";
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

                if (DEBUG_PATTERN.matcher(line).find()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("MEDIUM");
                    finding.setTitle("Debug mode enabled in configuration");
                    finding.setDescription("Debug mode or verbose stack trace output is explicitly enabled (" + line.trim() + "). Debug modes leak internal application details and sensitive stack traces.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Debug Config: " + line.trim());
                    finding.setConfidence("HIGH");
                    finding.setImpact("Leaking stack traces and internal variables aids attackers during reconnaissance.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setConfigurationFindings(context.getSummary().getConfigurationFindings() + 1);
                }
            }
        }

        return findings;
    }
}
