package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class LowTestPresenceRule implements TestingRule {

    @Override
    public String getRuleId() {
        return "TESTING_LOW_TEST_PRESENCE";
    }

    @Override
    public String getName() {
        return "Low Test Presence Ratio";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        int sourceCount = context.getSourceFiles().size();
        int testCount = context.getTestFiles().size();

        if (sourceCount < 5 || testCount == 0) {
            return findings;
        }

        double ratio = (double) testCount / sourceCount;

        if (ratio < 0.20) {
            Finding finding = new Finding();
            finding.setAnalysisId(context.getAnalysisId());
            finding.setCategory("TESTING");
            finding.setRuleId(getRuleId());
            finding.setSeverity("MEDIUM");
            finding.setTitle("Low test presence ratio");
            finding.setDescription("Test file to source file ratio is below 20%. This is a static test-presence indicator and does not represent runtime coverage.");
            finding.setEvidence(String.format("%d test file(s) for %d source file(s) (%.1f%% test presence ratio).", testCount, sourceCount, ratio * 100));
            finding.setConfidence("MEDIUM");
            finding.setImpact("Low test presence ratio suggests limited test file distribution relative to source code modules.");
            finding.setStatus("OPEN");
            findings.add(finding);
        }

        return findings;
    }
}
