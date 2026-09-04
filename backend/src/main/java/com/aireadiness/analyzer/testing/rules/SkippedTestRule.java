package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.analyzer.testing.model.ParsedFileInfo;
import com.aireadiness.analyzer.testing.model.TestMethodInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class SkippedTestRule implements TestingRule {

    @Override
    public String getRuleId() {
        return "TESTING_SKIPPED_TEST";
    }

    @Override
    public String getName() {
        return "Skipped Test Method";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        for (ParsedFileInfo file : context.getTestFiles()) {
            for (TestMethodInfo method : file.getTestMethods()) {
                if (method.isSkipped()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("TESTING");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("MEDIUM");
                    finding.setTitle("Skipped or disabled test method");
                    finding.setDescription("Test method is explicitly marked as skipped, disabled, or ignored.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(method.getStartLine());
                    finding.setEvidence("Test method '" + method.getName() + "' is annotated/configured as skipped.");
                    finding.setConfidence("HIGH");
                    finding.setImpact("Disabled tests bypass validation and may conceal broken or unmaintained functionality.");
                    finding.setStatus("OPEN");
                    findings.add(finding);
                }
            }
        }

        return findings;
    }
}
