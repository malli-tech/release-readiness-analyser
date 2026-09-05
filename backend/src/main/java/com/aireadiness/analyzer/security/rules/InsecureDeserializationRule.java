package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class InsecureDeserializationRule implements SecurityRule {

    private static final Pattern DESERIALIZATION_PATTERN = Pattern.compile(
            "(?i)(ObjectInputStream|\\.readObject\\s*\\(|pickle\\.loads?\\s*\\(|unserialize\\s*\\(|BinaryFormatter)"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_INSECURE_DESERIALIZATION";
    }

    @Override
    public String getName() {
        return "Insecure Object Deserialization";
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

                if (DESERIALIZATION_PATTERN.matcher(line).find()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("HIGH");
                    finding.setTitle("Dangerous object deserialization API detected");
                    finding.setDescription("Untrusted deserialization using " + line.trim() + " allows remote code execution when parsing malicious object streams.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Deserialization API: " + line.trim());
                    finding.setConfidence("HIGH");
                    finding.setImpact("Insecure deserialization is a primary vector for remote code execution vulnerabilities.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setDeserializationFindings(context.getSummary().getDeserializationFindings() + 1);
                }
            }
        }

        return findings;
    }
}
