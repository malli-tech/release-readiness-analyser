package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InsecureHttpRule implements SecurityRule {

    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("http://([a-zA-Z0-9\\.\\-_]+)");

    private static final List<String> EXCLUDED_HOSTS = List.of(
            "localhost", "127.0.0.1", "0.0.0.0", "w3.org", "schema.org", "example.com", "maven.apache.org"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_INSECURE_HTTP";
    }

    @Override
    public String getName() {
        return "Insecure HTTP Transport Protocol";
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

                Matcher matcher = HTTP_URL_PATTERN.matcher(line);
                if (matcher.find()) {
                    String host = matcher.group(1).toLowerCase();
                    if (isExcludedHost(host)) continue;

                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("MEDIUM");
                    finding.setTitle("Insecure HTTP transport URL detected");
                    finding.setDescription("An unencrypted HTTP URL ('http://" + host + "') was detected. Transporting sensitive data over HTTP exposes network traffic to eavesdropping and man-in-the-middle attacks.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("URL: http://" + host);
                    finding.setConfidence("HIGH");
                    finding.setImpact("Insecure HTTP URLs allow eavesdropping and tampering of network traffic.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setInsecureTransportFindings(context.getSummary().getInsecureTransportFindings() + 1);
                }
            }
        }

        return findings;
    }

    private boolean isExcludedHost(String host) {
        for (String ex : EXCLUDED_HOSTS) {
            if (host.equals(ex) || host.endsWith("." + ex)) return true;
        }
        return false;
    }
}
