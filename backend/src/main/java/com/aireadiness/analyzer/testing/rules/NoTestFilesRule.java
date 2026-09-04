package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class NoTestFilesRule implements TestingRule {

    @Override
    public String getRuleId() {
        return "TESTING_NO_TEST_FILES";
    }

    @Override
    public String getName() {
        return "No Test Files Detected";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        // Only evaluate for COMPLETE_PROJECT
        if (!context.isCompleteProject()) {
            return findings;
        }

        // If source files exist but test files == 0
        if (!context.getSourceFiles().isEmpty() && context.getTestFiles().isEmpty()) {
            Finding finding = new Finding();
            finding.setAnalysisId(context.getAnalysisId());
            finding.setCategory("TESTING");
            finding.setRuleId(getRuleId());
            finding.setSeverity("HIGH");
            finding.setTitle("No test files detected in complete project");
            finding.setDescription("No test files were detected in the complete uploaded project.");
            finding.setEvidence("0 test files detected out of " + context.getSourceFiles().size() + " source files.");
            finding.setConfidence("HIGH");
            finding.setImpact("Missing unit and integration test suite increases risk of undetected defects.");
            finding.setStatus("OPEN");
            findings.add(finding);
        }

        return findings;
    }
}
