package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.analyzer.testing.model.ParsedFileInfo;
import com.aireadiness.analyzer.testing.model.TestMethodInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class EmptyTestRule implements TestingRule {

    @Override
    public String getRuleId() {
        return "TESTING_EMPTY_TEST";
    }

    @Override
    public String getName() {
        return "Empty Test Method";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        for (ParsedFileInfo file : context.getTestFiles()) {
            for (TestMethodInfo method : file.getTestMethods()) {
                if (method.isEmpty()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("TESTING");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("MEDIUM");
                    finding.setTitle("Empty test method detected");
                    finding.setDescription("Test method contains no meaningful implementation or test body.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(method.getStartLine());
                    finding.setEvidence("Test method '" + method.getName() + "' contains an empty body.");
                    finding.setConfidence("HIGH");
                    finding.setImpact("Empty tests pass without verifying any functionality, giving a false sense of test presence.");
                    finding.setStatus("OPEN");
                    findings.add(finding);
                }
            }
        }

        return findings;
    }
}
