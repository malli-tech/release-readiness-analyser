package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.analyzer.testing.model.ParsedFileInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OrphanTestRule implements TestingRule {

    private static final Set<String> GENERIC_TEST_NAMES = Set.of(
            "test", "tests", "maintest", "apptest", "base", "basetest", "integrationtest", "e2etest", "smoketest"
    );

    @Override
    public String getRuleId() {
        return "TESTING_ORPHAN_TEST";
    }

    @Override
    public String getName() {
        return "Orphan Test File";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        if (!context.isCompleteProject()) {
            return findings;
        }

        for (ParsedFileInfo testFile : context.getTestFiles()) {
            String stem = extractStemFromTestFile(testFile.getFileName());
            if (stem == null || stem.isBlank() || GENERIC_TEST_NAMES.contains(stem.toLowerCase())) {
                continue;
            }

            boolean matchingSourceFound = context.getSourceFiles().stream().anyMatch(sf -> {
                String sourceStem = extractStemFromSourceFile(sf.getFileName());
                return stem.equalsIgnoreCase(sourceStem);
            });

            if (!matchingSourceFound) {
                Finding finding = new Finding();
                finding.setAnalysisId(context.getAnalysisId());
                finding.setCategory("TESTING");
                finding.setRuleId(getRuleId());
                finding.setSeverity("LOW");
                finding.setTitle("Possible orphan test file");
                finding.setDescription("Test file has no corresponding source file in the complete uploaded project.");
                finding.setFilePath(testFile.getRelativePath());
                finding.setEvidence("Test file '" + testFile.getFileName() + "' has no obvious matching source file (expected e.g. '" + stem + ".*').");
                finding.setConfidence("MEDIUM");
                finding.setImpact("Orphan tests may be obsolete, testing deleted code, or improperly named.");
                finding.setStatus("OPEN");
                findings.add(finding);
            }
        }

        return findings;
    }

    public static String extractStemFromTestFile(String fileName) {
        if (fileName == null) return null;
        String fn = fileName.trim();

        // JS/TS: *.test.ts, *.spec.ts
        if (fn.contains(".test.") || fn.contains(".spec.")) {
            int idx = fn.indexOf(".test.");
            if (idx < 0) idx = fn.indexOf(".spec.");
            return fn.substring(0, idx);
        }

        int lastDot = fn.lastIndexOf('.');
        String base = lastDot >= 0 ? fn.substring(0, lastDot) : fn;

        // Python: test_*.py or *_test.py
        if (base.toLowerCase().startsWith("test_")) {
            return base.substring(5);
        }
        if (base.toLowerCase().endsWith("_test")) {
            return base.substring(0, base.length() - 5);
        }

        // Java/C#/PHP/Go: *Tests or *Test
        if (base.endsWith("Tests")) {
            return base.substring(0, base.length() - 5);
        }
        if (base.endsWith("Test")) {
            return base.substring(0, base.length() - 4);
        }

        return base;
    }

    public static String extractStemFromSourceFile(String fileName) {
        if (fileName == null) return null;
        int lastDot = fileName.lastIndexOf('.');
        return lastDot >= 0 ? fileName.substring(0, lastDot) : fileName;
    }
}
