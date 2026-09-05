package com.aireadiness.analyzer.security.rules;

import com.aireadiness.analyzer.security.SecurityContext;
import com.aireadiness.analyzer.security.SecurityRule;
import com.aireadiness.analyzer.security.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PathTraversalRule implements SecurityRule {

    // Matches file API calls receiving concatenated user inputs or explicit "../" path concatenation
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
            "(?i)(new\\s+File\\s*\\([^\\)]*\\+|open\\s*\\([^\\)]*\\+|fs\\.readFile(?:Sync)?\\s*\\([^\\)]*\\+|" +
                    "Paths\\.get\\s*\\([^\\)]*req\\.|Path\\.Combine\\s*\\([^\\)]*request)"
    );

    @Override
    public String getRuleId() {
        return "SECURITY_PATH_TRAVERSAL_RISK";
    }

    @Override
    public String getName() {
        return "Unsafe Path Traversal Construction";
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

                if (PATH_TRAVERSAL_PATTERN.matcher(line).find()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("SECURITY");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("HIGH");
                    finding.setTitle("Potential Path Traversal in file access");
                    finding.setDescription("Constructing file paths directly from request input or unvalidated dynamic string concatenation allows Directory/Path Traversal ('../') attacks.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("File Access API: " + line.trim());
                    finding.setConfidence("MEDIUM");
                    finding.setImpact("Path traversal allows attackers to read or write arbitrary files on the system outside intended directories.");
                    finding.setStatus("OPEN");

                    findings.add(finding);
                    context.getSummary().setInjectionRiskFindings(context.getSummary().getInjectionRiskFindings() + 1);
                }
            }
        }

        return findings;
    }
}
