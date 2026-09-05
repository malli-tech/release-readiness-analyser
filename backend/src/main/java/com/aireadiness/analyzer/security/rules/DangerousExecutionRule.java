package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DangerousExecutionRule implements SecurityRule {

    private static final Pattern DANGEROUS_EXEC_PATTERN = Pattern.compile(
            "(?i)(Runtime\\.getRuntime\\(\\)\\.exec|new\\s+ProcessBuilder|eval\\s*\\(|new\\s+Function\\s*\\(|" +
                    "child_process\\.(?:exec|spawn|fork)|os\\.system\\s*\\(|subprocess\\.(?:call|Popen|run)|" +
                    "shell_exec\\s*\\(|passthru\\s*\\(|system\\s*\\(|Process\\.Start\\s*\\(|exec\\.Command\\s*\\()"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_DANGEROUS_EXECUTION";
    }

    @Override
    public String getName() {
        return "Dangerous Code or Command Execution API";
    }

    @Override
    public List<Finding> evaluate(SecurityContext context) {
        List<Finding> findings = new ArrayList<>();

        if (context.getParsedFiles() == null) return findings;

        for (ParsedSourceFile file : context.getParsedFiles()) {
            List<String> lines = file.getLines();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().startsWith("//") || line.trim().startsWith("#") || line.trim().startsWith("/*") || line.trim().startsWith("*")) continue;

                if (DANGEROUS_EXEC_PATTERN.matcher(line).find()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("HIGH");
                    finding.setTitle("Dangerous dynamic execution API invoked");
                    finding.setDescription("A dynamic process or code execution API was detected. Dynamically evaluating code or spawning shell commands introduces execution risk if input is unvalidated.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Execution API: " + line.trim());
                    finding.setConfidence("HIGH");
                    finding.setImpact("Dynamic execution APIs can be leveraged for arbitrary code or command execution.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setDangerousExecutionFindings(context.getSummary().getDangerousExecutionFindings() + 1);
                }
            }
        }

        return findings;
    }
}
