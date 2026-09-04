package com.aireadiness.analyzer.testing;

import com.aireadiness.model.Finding;

import java.util.List;

public interface TestingRule {

    String getRuleId();

    String getName();

    List<Finding> evaluate(TestingContext context);
}
