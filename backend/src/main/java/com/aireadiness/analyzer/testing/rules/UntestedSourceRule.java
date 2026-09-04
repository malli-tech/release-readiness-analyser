package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.analyzer.testing.model.ParsedFileInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UntestedSourceRule implements TestingRule {

    private static final Set<String> EXCLUDED_NAME_SUFFIXES = Set.of(
            "dto", "entity", "config", "configuration", "constant", "constants", "model",
            "application", "main", "exception", "response", "request", "vo", "interface"
    );

    private static final Set<String> EXCLUDED_PATH_SEGMENTS = Set.of(
            "/config/", "/dto/", "/model/", "/entity/", "/entities/", "/constants/",
            "/exception/", "/exceptions/", "/migration/", "/db/"
    );

    @Override
    public String getRuleId() {
        return "TESTING_UNTESTED_SOURCE_FILE";
    }

    @Override
    public String getName() {
        return "Untested Source File";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        if (!context.isCompleteProject()) {
            return findings;
        }

        for (ParsedFileInfo sourceFile : context.getSourceFiles()) {
            if (isExcludedSourceFile(sourceFile)) {
                continue;
            }

            String stem = OrphanTestRule.extractStemFromSourceFile(sourceFile.getFileName());
            if (stem == null || stem.isBlank()) continue;

            boolean matchingTestFound = context.getTestFiles().stream().anyMatch(tf -> {
                String testStem = OrphanTestRule.extractStemFromTestFile(tf.getFileName());
                return stem.equalsIgnoreCase(testStem);
            });

            if (!matchingTestFound) {
                Finding finding = new Finding();
                finding.setAnalysisId(context.getAnalysisId());
                finding.setCategory("TESTING");
                finding.setRuleId(getRuleId());
                finding.setSeverity("LOW");
                finding.setTitle("Source file without obvious corresponding test file");
                finding.setDescription("Source file has no obvious corresponding test file in the complete uploaded project.");
                finding.setFilePath(sourceFile.getRelativePath());
                finding.setEvidence("Source file '" + sourceFile.getFileName() + "' has no obvious matching test file.");
                finding.setConfidence("LOW");
                finding.setImpact("Untested business logic classes present higher risk of regressions.");
                finding.setStatus("OPEN");
                findings.add(finding);
            }
        }

        return findings;
    }

    private boolean isExcludedSourceFile(ParsedFileInfo file) {
        String path = file.getRelativePath().toLowerCase().replace('\\', '/');
        for (String segment : EXCLUDED_PATH_SEGMENTS) {
            if (path.contains(segment)) {
                return true;
            }
        }

        String fileName = file.getFileName();
        int dot = fileName.lastIndexOf('.');
        String base = (dot >= 0 ? fileName.substring(0, dot) : fileName).toLowerCase();

        for (String suffix : EXCLUDED_NAME_SUFFIXES) {
            if (base.endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }
}
