package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandInjectionRule implements SecurityRule {

    // Matches shell command building: e.g. "sh -c " + var, "cmd /c " + var, "ping " + var, "curl " + var
    private static final Pattern COMMAND_BUILD_PATTERN = Pattern.compile(
            "(?i)[\"']\\s*(?:sh\\s+-c|bash\\s+-c|cmd\\s+/c|ping\\s+|curl\\s+|wget\\s+|chmod\\s+|chown\\s+)[^\"']*[\"']\\s*\\+|" +
                    "f[\"']\\s*(?:sh\\s+-c|bash\\s+-c|cmd\\s+/c|ping\\s+|curl\\s+|wget\\s+).*\\{.*\\}"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_COMMAND_INJECTION_RISK";
    }

    @Override
    public String getName() {
        return "Unsafe Command String Construction";
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

                if (COMMAND_BUILD_PATTERN.matcher(line).find()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("HIGH");
                    finding.setTitle("Unsafe shell command string construction");
                    finding.setDescription("Constructing shell command strings dynamically using string concatenation or variable interpolation introduces Command Injection risk.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Command String: " + line.trim());
                    finding.setConfidence("MEDIUM");
                    finding.setImpact("Command injection allows unauthorized command execution on the host operating system.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setInjectionRiskFindings(context.getSummary().getInjectionRiskFindings() + 1);
                }
            }
        }

        return findings;
    }
}
