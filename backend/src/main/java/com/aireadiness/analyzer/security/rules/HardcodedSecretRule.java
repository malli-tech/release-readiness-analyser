package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardcodedSecretRule implements SecurityRule {

    private static final Pattern PEM_KEY_PATTERN = Pattern.compile("-----BEGIN (?:RSA|EC|DSA|OPENSSH)?\\s*PRIVATE KEY-----");

    // Matches suspicious assignments: keyName = "value"
    private static final Pattern SECRET_ASSIGN_PATTERN = Pattern.compile(
            "(?i)(password|passwd|api_key|apikey|secret|client_secret|access_token|private_key|auth_token)\\s*[:=]\\s*[\"']([^\"']+)[\"']"
    );

    private static final List<String> PLACEHOLDERS = List.of(
            "placeholder", "example", "your-key", "your_key", "your-secret", "your_secret", "your-password", "your_password",
            "your", "here", "xxxx", "****", "dummy", "test", "change_me", "changeme", "todo", "null", "undefined", "${"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_HARDCODED_SECRET";
    }

    @Override
    public String getName() {
        return "Hardcoded Credentials / Private Keys";
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

                // 1. Private Key Block Detection -> CRITICAL
                if (PEM_KEY_PATTERN.matcher(line).find()) {
                    Finding finding = createFinding(
                            context.getAnalysisId(),
                            "CRITICAL",
                            "Hardcoded private key detected",
                            "A PEM private key block was detected in the source code. Storing private key material in source files exposes critical system cryptographic material.",
                            file.getRelativePath(),
                            i + 1,
                            "Private Key Material: [REDACTED SECRET]",
                            "CRITICAL"
                    );
                    findings.add(finding);
                    context.getSummary().setHardcodedSecretsDetected(context.getSummary().getHardcodedSecretsDetected() + 1);
                    continue;
                }

                // 2. Secret / Password Assignment Heuristic -> HIGH
                Matcher matcher = SECRET_ASSIGN_PATTERN.matcher(line);
                if (matcher.find()) {
                    String keyName = matcher.group(1);
                    String secretVal = matcher.group(2);

                    if (isPlaceholder(secretVal)) continue;

                    Finding finding = createFinding(
                            context.getAnalysisId(),
                            "HIGH",
                            "Potential hardcoded credential detected",
                            "A hardcoded literal value was assigned to credential field '" + keyName + "'. Credentials should be injected securely via environment variables or secret vaults.",
                            file.getRelativePath(),
                            i + 1,
                            "Credential Field: " + keyName + " = [REDACTED SECRET]",
                            "HIGH"
                    );
                    findings.add(finding);
                    context.getSummary().setHardcodedSecretsDetected(context.getSummary().getHardcodedSecretsDetected() + 1);
                }
            }
        }

        return findings;
    }

    private boolean isPlaceholder(String val) {
        if (val == null || val.isBlank() || val.length() < 3) return true;
        String valLower = val.toLowerCase();

        // Repeating character placeholders like "XXXXX", "*****", "12345"
        if (valLower.matches("^(.)\\1+$")) return true;

        for (String p : PLACEHOLDERS) {
            if (valLower.contains(p)) return true;
        }
        return false;
    }

    private Finding createFinding(String analysisId, String severity, String title, String desc, String path, int line, String evidence, String confidence) {
        Finding finding = new Finding();
        finding.setAnalysisId(analysisId);
        finding.setCategory("SECURITY");
        finding.setRuleId(getRuleId());
        finding.setSeverity(severity);
        finding.setTitle(title);
        finding.setDescription(desc);
        finding.setFilePath(path);
        finding.setLineNumber(line);
        finding.setEvidence(evidence);
        finding.setConfidence(confidence);
        finding.setImpact("Exposed hardcoded credentials enable unauthorized access and data breaches.");
        finding.setStatus("OPEN");
        return finding;
    }
}
