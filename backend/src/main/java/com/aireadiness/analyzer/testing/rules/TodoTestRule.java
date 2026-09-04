package com.aireadiness.analyzer.testing.rules;

import com.aireadiness.analyzer.testing.TestingContext;
import com.aireadiness.analyzer.testing.TestingRule;
import com.aireadiness.analyzer.testing.model.ParsedFileInfo;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TodoTestRule implements TestingRule {

    private static final Pattern TODO_PATTERN = Pattern.compile("(?i)\\b(TODO|FIXME|XXX|HACK)\\b");

    @Override
    public String getRuleId() {
        return "TESTING_TODO_TEST";
    }

    @Override
    public String getName() {
        return "TODO Marker in Test Code";
    }

    @Override
    public List<Finding> evaluate(TestingContext context) {
        List<Finding> findings = new ArrayList<>();

        for (ParsedFileInfo file : context.getTestFiles()) {
            List<String> lines = file.getLines();
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (TODO_PATTERN.matcher(line).find()) {
                    Finding finding = new Finding();
                    finding.setAnalysisId(context.getAnalysisId());
                    finding.setCategory("TESTING");
                    finding.setRuleId(getRuleId());
                    finding.setSeverity("LOW");
                    finding.setTitle("TODO/FIXME comment in test code");
                    finding.setDescription("Test code contains a TODO or FIXME marker indicating incomplete test implementation.");
                    finding.setFilePath(file.getRelativePath());
                    finding.setLineNumber(i + 1);
                    finding.setEvidence("Test line contains TODO marker: " + line.trim());
                    finding.setConfidence("HIGH");
                    finding.setImpact("Incomplete tests may leave critical assertions or test cases unimplemented.");
                    finding.setStatus("OPEN");
                    findings.add(finding);
                }
            }
        }

        return findings;
    }
}
