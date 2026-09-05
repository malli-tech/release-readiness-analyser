package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeakCryptographyRule implements SecurityRule {

    private static final Pattern WEAK_CRYPTO_PATTERN = Pattern.compile(
            "(?i)(getInstance\\s*\\(\\s*[\"'](MD5|SHA-1|SHA1|DES|DESede|TripleDES|AES/ECB/[^\"']+)[\"']\\)|" +
                    "hashlib\\.(?:md5|sha1)\\s*\\(|md5\\s*\\(|Cipher\\.DES\\b)"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_WEAK_CRYPTOGRAPHY";
    }

    @Override
    public String getName() {
        return "Weak Cryptographic Algorithm";
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

                Matcher matcher = WEAK_CRYPTO_PATTERN.matcher(line);
                if (matcher.find()) {
                    String matchStr = matcher.group();
                    boolean isDesOrEcb = matchStr.toUpperCase().contains("DES") || matchStr.toUpperCase().contains("ECB");
                    String severity = isDesOrEcb ? "HIGH" : "MEDIUM";

                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity(severity);
                    finding.setTitle("Weak cryptographic algorithm detected");
                    finding.setDescription("The application uses a legacy or weak cryptographic algorithm (" + matchStr + "). Replace with modern primitives (e.g. SHA-256/SHA-512, AES-GCM, Argon2).");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Crypto Usage: " + line.trim());
                    finding.setConfidence("HIGH");
                    finding.setImpact("Weak cryptographic algorithms are vulnerable to collision attacks, key recovery, or cipher text manipulation.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setWeakCryptographyFindings(context.getSummary().getWeakCryptographyFindings() + 1);
                }
            }
        }

        return findings;
    }
}
