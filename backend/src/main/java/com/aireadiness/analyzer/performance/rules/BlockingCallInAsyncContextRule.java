package com.aireadiness.analyzer.performance.rules;

import com.aireadiness.analyzer.performance.PerformanceContext;
import com.aireadiness.analyzer.performance.PerformanceRule;
import com.aireadiness.analyzer.performance.model.ParsedSourceFile;
import com.aireadiness.model.Finding;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class BlockingCallInAsyncContextRule implements PerformanceRule {

    private static final Pattern BLOCKING_CALL_PATTERN = Pattern.compile(
            "(?i)\\.(block|blockOptional|blockFirst|blockLast)\\s*\\(|\\b(future|completablefuture|task|promise)\\.(get|join)\\s*\\("
    );

    private static final Pattern ASYNC_CONTEXT_PATTERN = Pattern.compile(
            "(?i)@Async|Mono<|Flux<|CompletableFuture<|async\\s+function|async\\s+\\w+|\\.subscribe\\(|Observable<|Single<"
    );

    @Override
    public String getRuleId() {
        return "PERFORMANCE_BLOCKING_CALL_IN_ASYNC_CONTEXT";
    }

    @Override
    public String getName() {
        return "Blocking Operation in Asynchronous Context";
    }

    @Override
    public List<Finding> evaluate(PerformanceContext context) {
        List<Finding> findings = new ArrayList<>();

        for (ParsedSourceFile file : context.getParsedFiles()) {
            List<String> lines = file.getLines();
            boolean isAsyncContextInFile = false;
            boolean inBlockComment = false;

            // Check if file contains async/reactive annotations or signatures
            for (String line : lines) {
                if (ASYNC_CONTEXT_PATTERN.matcher(line).find()) {
                    isAsyncContextInFile = true;
                    break;
                }
            }

            for (int i = 0; i < lines.size(); i++) {
                String rawLine = lines.get(i);
                String trimmed = rawLine.trim();

                if (trimmed.startsWith("/*")) inBlockComment = true;
                if (inBlockComment) {
                    if (trimmed.endsWith("*/") || trimmed.contains("*/")) inBlockComment = false;
                    continue;
                }
                if (trimmed.startsWith("//") || trimmed.startsWith("#") || trimmed.startsWith("*")) {
                    continue;
                }

                if (BLOCKING_CALL_PATTERN.matcher(trimmed).find()) {
                    // Check if call is explicitly blocking or inside an async context
                    boolean isExplicitReactiveBlock = trimmed.contains(".block(") || trimmed.contains(".blockOptional(") || trimmed.contains(".blockFirst(") || trimmed.contains(".blockLast(");

                    if (isExplicitReactiveBlock || isAsyncContextInFile) {
                        Finding f = new Finding();
                        f.setAnalysisId(context.getAnalysisId());
                        f.setCategory("PERFORMANCE");
                        f.setRuleId(getRuleId());
                        f.setSeverity(isExplicitReactiveBlock ? "HIGH" : "MEDIUM");
                        f.setTitle("Blocking Call in Asynchronous or Reactive Context");
                        f.setDescription("Blocking operation detected in asynchronous or reactive code. Invoking blocking calls like .block() or Future.get() blocks execution threads and can lead to thread starvation.");
                        f.setFilePath(file.getRelativePath());
                        f.setLineNumber(i + 1);
                        f.setEvidence(trimmed);
                        f.setConfidence("HIGH");
                        f.setImpact("Blocking execution threads in reactive or async contexts eliminates the performance benefits of non-blocking I/O and risks thread starvation under load.");
                        f.setStatus("OPEN");
                        findings.add(f);
                    }
                }
            }
        }

        return findings;
    }
}
