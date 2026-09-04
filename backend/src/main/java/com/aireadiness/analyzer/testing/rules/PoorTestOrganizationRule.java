package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.analyzer.testing.model.ParsedFileInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class PoorTestOrganizationRule implements TestingRule {

    @Override
    public String getRuleId() {
        return "TESTING_POOR_TEST_ORGANIZATION";
    }

    @Override
    public String getName() {
        return "Poor Test Organization";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        List<ParsedFileInfo> testFiles = context.getTestFiles();
        int sourceCount = context.getSourceFiles().size();

        // 1. Single test file handling all tests in a multi-file project
        if (testFiles.size() == 1 && sourceCount >= 5) {
            ParsedFileInfo testFile = testFiles.get(0);
            if (testFile.getTestMethods().size() > 10 || testFile.getLines().size() > 300) {
                Finding finding = new Finding();
                finding.setAnalysisId(context.getAnalysisId());
                finding.setCategory("TESTING");
                finding.setRuleId(getRuleId());
                finding.setSeverity("LOW");
                finding.setTitle("Poor test organization (monolithic test file)");
                finding.setDescription("All test cases for a multi-file project are concentrated in a single large test file.");
                finding.setFilePath(testFile.getRelativePath());
                finding.setEvidence("Single test file '" + testFile.getFileName() + "' contains " + testFile.getTestMethods().size() + " test methods and " + testFile.getLines().size() + " lines for " + sourceCount + " source files.");
                finding.setConfidence("MEDIUM");
                finding.setImpact("Large monolithic test files impair readability, modularity, and selective test execution.");
                finding.setStatus("OPEN");
                findings.add(finding);
            }
        }

        // 2. Unexpected test file placement (e.g. Java test files in root instead of src/test)
        for (ParsedFileInfo testFile : testFiles) {
            String path = testFile.getRelativePath().replace('\\', '/');
            if (testFile.getFileName().endsWith(".java") && !path.contains("src/test/") && !path.contains("src/test-") && !path.contains("test/")) {
                Finding finding = new Finding();
                finding.setAnalysisId(context.getAnalysisId());
                finding.setCategory("TESTING");
                finding.setRuleId(getRuleId());
                finding.setSeverity("LOW");
                finding.setTitle("Unexpected test file location");
                finding.setDescription("Test file is stored outside standard framework test directory conventions.");
                finding.setFilePath(testFile.getRelativePath());
                finding.setEvidence("Java test file '" + testFile.getFileName() + "' is located at '" + path + "' instead of standard 'src/test/java/'.");
                finding.setConfidence("MEDIUM");
                finding.setImpact("Non-standard test file locations may prevent automated build tools from picking up tests.");
                finding.setStatus("OPEN");
                findings.add(finding);
            }
        }

        return findings;
    }
}
