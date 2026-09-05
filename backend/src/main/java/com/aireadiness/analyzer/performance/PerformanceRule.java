package com.aireadiness.analyzer.performance;

import com.aireadiness.model.Finding;

import java.util.List;

public interface PerformanceRule {

    String getRuleId();

    String getName();

    List<Finding> evaluate(PerformanceContext context);
}
