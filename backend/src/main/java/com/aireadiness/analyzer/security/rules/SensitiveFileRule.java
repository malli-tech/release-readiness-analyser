package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SensitiveFileRule implements SecurityRule {

    private static final Pattern SECRET_ENV_LINE = Pattern.compile(
            "(?i)^(?:DB_PASS|PASSWORD|SECRET|API_KEY|TOKEN|PRIVATE_KEY|AWS_SECRET)\\s*=\\s*[\"']?([^\"'\\s]+)[\"']?"
    );

    private static final List<String> PLACEHOLDERS = List.of(
            "placeholder", "example", "your-key", "your_key", "your-secret", "xxxx", "****",
            "dummy", "test", "change_me", "changeme", "todo", "null", "undefined", "${"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_SENSITIVE_FILE_EXPOSED";
    }

    @Override
    public String getName() {
        return "Committed Sensitive Credentials File";
    }

    @Override
    public List<Finding> evaluate(SecurityContext context) {
        List<Finding> findings = new ArrayList<>();

        if (context.getParsedFiles() == null) return findings;

        for (ParsedSourceFile file : context.getParsedFiles()) {
            String fnLower = file.getFilename().toLowerCase();
            String relPathLower = file.getRelativePath().toLowerCase();

            // Ignore .env.example / .env.sample
            if (fnLower.contains("example") || fnLower.contains("sample") || fnLower.contains("template")) continue;

            // 1. Private Key Files / Credentials JSON -> SECURITY_SENSITIVE_FILE_EXPOSED
            if (fnLower.endsWith(".pem") || fnLower.endsWith(".pkcs12") || fnLower.equals("id_rsa") ||
                    fnLower.equals("id_ed25519") || fnLower.equals("service-account.json") || fnLower.equals("credentials.json")) {
                Finding finding = createFinding(
                        context.getAnalysisId(),
                        getRuleId(),
                        "HIGH",
                        "Exposed private key or credentials file committed",
                        "A sensitive key or credentials file ('" + file.getRelativePath() + "') was found in the project. Credentials files must not be committed to source control repositories.",
                        file.getRelativePath(),
                        1,
                        "Sensitive File: " + file.getRelativePath()
                );
                findings.add(finding);
                context.getSummary().setSensitiveFilesDetected(context.getSummary().getSensitiveFilesDetected() + 1);
                continue;
            }

            // 2. .env files containing non-placeholder secret assignments -> SECURITY_ENV_FILE_WITH_SECRET
            if (fnLower.startsWith(".env")) {
                boolean hasSecret = false;
                int secretLineNum = 1;

                for (int i = 0; i < file.getLines().size(); i++) {
                    String line = file.getLines().get(i).trim();
                    Matcher m = SECRET_ENV_LINE.matcher(line);
                    if (m.find()) {
                        String secretVal = m.group(1);
                        if (!isPlaceholder(secretVal)) {
                            hasSecret = true;
                            secretLineNum = i + 1;
                            break;
                        }
                    }
                }

                if (hasSecret) {
                    Finding finding = createFinding(
                            context.getAnalysisId(),
                            "SECURITY_ENV_FILE_WITH_SECRET",
                            "HIGH",
                            "Environment secret file (.env) committed with live secrets",
                            "An environment file ('" + file.getRelativePath() + "') containing non-placeholder secrets was found. Storing live secrets in environment files committed to source repositories risks severe credential exposure.",
                            file.getRelativePath(),
                            secretLineNum,
                            "Committed Environment File: " + file.getRelativePath() + " containing [REDACTED SECRET]"
                    );
                    findings.add(finding);
                    context.getSummary().setSensitiveFilesDetected(context.getSummary().getSensitiveFilesDetected() + 1);
                }
            }
        }

        return findings;
    }

    private boolean isPlaceholder(String val) {
        if (val == null || val.isBlank() || val.length() < 3) return true;
        String valLower = val.toLowerCase();
        for (String p : PLACEHOLDERS) {
            if (valLower.contains(p)) return true;
        }
        return false;
    }

    private Finding createFinding(String analysisId, String ruleId, String severity, String title, String desc, String path, int line, String evidence) {
        Finding finding = new Finding();
        finding.setAnalysisId(analysisId);
        finding.setCategory("SECURITY");
        finding.setRuleId(ruleId);
        finding.setSeverity(severity);
        finding.setTitle(title);
        finding.setDescription(desc);
        finding.setFilePath(path);
        finding.setLineNumber(line);
        finding.setEvidence(evidence);
        finding.setConfidence("HIGH");
        finding.setImpact("Committed sensitive files allow unauthorized parties with repository access to compromise live services.");
        finding.setStatus("OPEN");
        return finding;
    }
}
