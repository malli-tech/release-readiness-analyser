package com.aireadiness.analyzer.security;

import com.aireadiness.model.Finding;

import java.util.List;

public interface SecurityRule {

    String getRuleId();

    String getName();

    List<Finding> evaluate(SecurityContext context);
}
