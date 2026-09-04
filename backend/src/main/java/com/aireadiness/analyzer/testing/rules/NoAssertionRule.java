package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.analyzer.testing.model.ParsedFileInfo;
import com.aireadiness.analyzer.testing.model.TestMethodInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;

public class NoAssertionRule implements TestingRule {

    @Override
    public String getRuleId() {
        return "TESTING_NO_ASSERTION";
    }

    @Override
    public String getName() {
        return "Test Method Has No Assertion";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        for (ParsedFileInfo file : context.getTestFiles()) {
            for (TestMethodInfo method : file.getTestMethods()) {
                if (!method.isEmpty() && !method.isSkipped() && !method.isHasAssertion()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("TESTING");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("MEDIUM");
                    finding.setTitle("Test method contains no assertion");
                    finding.setDescription("Test method contains no recognizable assertion or verification statement.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(method.getStartLine());
                    finding.setEvidence("Test method '" + method.getName() + "' contains code but no recognized assertion.");
                    finding.setConfidence("MEDIUM");
                    finding.setImpact("Tests without assertions may execute code without verifying expected behavior or state.");
                    finding.setStatus("OPEN");
                    findings.add(finding);
                }
            }
        }

        return findings;
    }
}
