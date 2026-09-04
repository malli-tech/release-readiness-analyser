package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class ExcessiveSkippedTestsRule implements TestingRule {

    @Override
    public String getRuleId() {
        return "TESTING_EXCESSIVE_SKIPPED_TESTS";
    }

    @Override
    public String getName() {
        return "Excessive Skipped Tests";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        int totalTests = context.getSummary().getTestsDetected();
        int skippedTests = context.getSummary().getSkippedTestsDetected();

        if (totalTests == 0 || skippedTests == 0) {
            return findings;
        }

        if (totalTests < 5 && skippedTests < 2) {
            return findings;
        }

        double skippedRatio = (double) skippedTests / totalTests;

        if (skippedRatio > 0.20) {
            Finding finding = new Finding();
            finding.setAnalysisId(context.getAnalysisId());
            finding.setCategory("TESTING");
            finding.setRuleId(getRuleId());
            finding.setSeverity("HIGH");
            finding.setTitle("Excessive skipped tests detected");
            finding.setDescription("A significant portion of detected tests appear to be skipped (>20%).");
            finding.setEvidence(String.format("%d of %d detected tests (%.1f%%) are marked as skipped.", skippedTests, totalTests, skippedRatio * 100));
            finding.setConfidence("HIGH");
            finding.setImpact("A high proportion of skipped tests reduces true test coverage and indicates potential test debt.");
            finding.setStatus("OPEN");
            findings.add(finding);
        }

        return findings;
    }
}
