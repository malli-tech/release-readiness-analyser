package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TlsVerificationRule implements SecurityRule {

    private static final Pattern TLS_DISABLED_PATTERN = Pattern.compile(
            "(?i)(rejectUnauthorized\\s*:\\s*false|strictSSL\\s*:\\s*false|NODE_TLS_REJECT_UNAUTHORIZED\\s*=\\s*['\"]0['\"]|" +
                    "verify\\s*=\\s*False|InsecureSkipVerify\\s*:\\s*true|_create_unverified_context|" +
                    "ALLOW_ALL_HOSTNAME_VERIFIER|TrustAllStrategy|InsecureTrustManager|ServerCertificateValidationCallback\\s*=\\s*.*true)"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_TLS_VERIFICATION_DISABLED";
    }

    @Override
    public String getName() {
        return "Disabled TLS/SSL Certificate Verification";
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

                if (TLS_DISABLED_PATTERN.matcher(line).find()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("HIGH");
                    finding.setTitle("Disabled TLS/SSL certificate validation");
                    finding.setDescription("Disabling TLS/SSL certificate or hostname verification bypasses transport layer encryption integrity checks and exposes connections to man-in-the-middle attacks.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Disabled Verification: " + line.trim());
                    finding.setConfidence("HIGH");
                    finding.setImpact("Connections with disabled TLS validation can be intercepted or manipulated by active attackers.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setInsecureTransportFindings(context.getSummary().getInsecureTransportFindings() + 1);
                }
            }
        }

        return findings;
    }
}
