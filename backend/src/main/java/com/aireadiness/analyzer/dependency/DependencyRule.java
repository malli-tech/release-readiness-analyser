package com.aireadiness.analyzer.dependency;

import com.aireadiness.model.Finding;

import java.util.List;

public interface DependencyRule {

    String getRuleId();

    String getName();

    List<Finding> evaluate(DependencyContext context);
}
