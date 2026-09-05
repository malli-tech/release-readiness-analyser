package com.aireadiness.analyzer.performance;

import com.aireadiness.analyzer.Analyzer;
import com.aireadiness.analyzer.performance.model.ParsedSourceFile;
import com.aireadiness.analyzer.performance.rules.*;
import com.aireadiness.model.Finding;
import com.aireadiness.model.PerformanceSummary;
import com.aireadiness.model.ProjectProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class PerformanceAnalyzer implements Analyzer {

    private final long maxFileSizeBytes;
    private final long maxTotalSourceBytes;
    private final int maxSourceFiles;
    private final int maxTotalFindings;

    private final List<PerformanceRule> rules;
    private PerformanceSummary lastSummary;

    private static final Set<String> SOURCE_EXTENSIONS = Set.of(
            ".java", ".js", ".jsx", ".ts", ".tsx", ".py", ".cs", ".go", ".php"
    );

    private static final Pattern SECRET_PATTERN = Pattern.compile(
            "(?i)(password|passwd|secret|apikey|api_key|token|auth_token|private_key|aws_key)\\s*[:=]\\s*[\"']([^\"']+)[\"']"
    );

    public PerformanceAnalyzer(
            @Value("${app.analyzer.max-file-size-mb:2}") int maxFileSizeMb,
            @Value("${app.analyzer.max-total-source-mb:50}") int maxTotalSourceMb,
            @Value("${app.analyzer.max-source-files:5000}") int maxSourceFiles,
            @Value("${app.analyzer.max-total-findings:2000}") int maxTotalFindings
    ) {
        this.maxFileSizeBytes = (long) maxFileSizeMb * 1024 * 1024;
        this.maxTotalSourceBytes = (long) maxTotalSourceMb * 1024 * 1024;
        this.maxSourceFiles = maxSourceFiles;
        this.maxTotalFindings = maxTotalFindings;

        this.rules = List.of(
                new NPlusOneQueryRule(),
                new DatabaseCallInLoopRule(),
                new BlockingCallInAsyncContextRule(),
                new SleepOrWaitRule(),
                new RepeatedStringConcatenationRule(),
                new RegexInLoopRule(),
                new RepeatedCollectionScanRule(),
                new ExcessiveNestedLoopsRule(),
                new CollectionAllocationInLoopRule(),
                new RepeatedExpensiveOperationRule()
        );
    }

    @Override
    public String getType() {
        return "PERFORMANCE";
    }

    public PerformanceSummary getLastSummary() {
        return lastSummary;
    }

    @Override
    public List<Finding> analyze(Path workspaceDir, ProjectProfile profile, String analysisId, String uploadMode, List<String> warnings) {
        List<Finding> findings = new ArrayList<>();
        Set<String> findingDeduplicationKeys = new HashSet<>();

        PerformanceSummary summary = new PerformanceSummary();
        this.lastSummary = summary;

        if (workspaceDir == null || !Files.exists(workspaceDir) || !Files.isDirectory(workspaceDir)) {
            warnings.add("Workspace directory is unavailable for static performance analysis.");
            summary.setPerformanceCompleteness("UNKNOWN");
            summary.getPerformanceWarnings().add("Workspace directory is unavailable.");
            return findings;
        }

        if ("SELECTED_CONTENT".equalsIgnoreCase(uploadMode)) {
            summary.getPerformanceWarnings().add("Performance analysis is based only on the selected uploaded content. Additional performance issues may exist in files that were not uploaded.");
        }

        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            warnings.add("No supported source files were available for static performance analysis.");
            summary.getPerformanceWarnings().add("Performance analysis is not currently supported or project ecosystem is unknown.");
            summary.setPerformanceCompleteness("UNKNOWN");
            return findings;
        }

        List<ParsedSourceFile> parsedFiles = new ArrayList<>();
        long accumulatedBytes = 0;
        int fileCount = 0;

        try {
            List<Path> allPaths = new ArrayList<>();
            try (var stream = Files.walk(workspaceDir)) {
                stream.forEach(allPaths::add);
            }

            for (Path path : allPaths) {
                if (Files.isDirectory(path)) continue;

                Path relativePath = workspaceDir.relativize(path).normalize();
                if (relativePath.toString().startsWith("..")) continue;

                String relPathStr = relativePath.toString().replace('\\', '/');
                String fnLower = path.getFileName().toString().toLowerCase();
                String ext = getExtension(fnLower);

                if (!SOURCE_EXTENSIONS.contains(ext)) continue;

                fileCount++;
                if (fileCount > maxSourceFiles) {
                    warnings.add("Exceeded maximum file count limit (" + maxSourceFiles + ") for performance analysis. Remaining files skipped.");
                    break;
                }

                long fileSize = Files.size(path);
                if (fileSize > maxFileSizeBytes) {
                    warnings.add("File skipped because it exceeds static performance analysis size limit: " + relPathStr);
                    continue;
                }

                accumulatedBytes += fileSize;
                if (accumulatedBytes > maxTotalSourceBytes) {
                    warnings.add("Exceeded maximum total source content size limit (" + maxTotalSourceBytes / (1024 * 1024) + " MB). Remaining content skipped.");
                    break;
                }

                List<String> lines = readLinesSafely(path);
                ParsedSourceFile parsedFile = new ParsedSourceFile(relPathStr, path.getFileName().toString(), ext, lines);
                parsedFiles.add(parsedFile);
            }
        } catch (Exception e) {
            warnings.add("Static performance analysis encountered an error: " + e.getMessage());
        }

        summary.setAnalyzedSourceFiles(parsedFiles.size());

        PerformanceContext context = new PerformanceContext(
                workspaceDir,
                profile,
                analysisId,
                uploadMode,
                parsedFiles,
                summary,
                warnings
        );

        // Evaluate performance rules
        for (PerformanceRule rule : rules) {
            if (findings.size() >= maxTotalFindings) {
                warnings.add("Maximum performance analysis findings count reached (" + maxTotalFindings + "). Further findings truncated.");
                break;
            }

            try {
                List<Finding> ruleFindings = rule.evaluate(context);
                for (Finding f : ruleFindings) {
                    String dedupKey = analysisId + ":" + f.getRuleId() + ":" + (f.getFilePath() != null ? f.getFilePath() : "") + ":" + (f.getLineNumber() != null ? f.getLineNumber() : 0) + ":" + (f.getEvidence() != null ? f.getEvidence().hashCode() : 0);
                    if (findingDeduplicationKeys.add(dedupKey)) {
                        redactSecretsInFinding(f);
                        findings.add(f);
                    }
                }
            } catch (Exception e) {
                warnings.add("Performance rule " + rule.getRuleId() + " failed: " + e.getMessage());
            }
        }

        // Aggregate statistics for PerformanceSummary
        int high = 0, med = 0, low = 0;
        Set<String> affectedFilesSet = new HashSet<>();
        for (Finding f : findings) {
            if ("HIGH".equalsIgnoreCase(f.getSeverity()) || "CRITICAL".equalsIgnoreCase(f.getSeverity())) high++;
            else if ("MEDIUM".equalsIgnoreCase(f.getSeverity())) med++;
            else low++;

            if (f.getFilePath() != null) {
                affectedFilesSet.add(f.getFilePath());
            }
        }

        summary.setTotalPerformanceFindings(findings.size());
        summary.setHighSeverityFindings(high);
        summary.setMediumSeverityFindings(med);
        summary.setLowSeverityFindings(low);
        summary.setDetectedPerformanceIssues(findings.size());
        summary.setAffectedFiles(affectedFilesSet.size());

        summary.setPerformanceCompleteness(calculateCompleteness(parsedFiles, uploadMode, profile));

        return findings;
    }

    private String calculateCompleteness(List<ParsedSourceFile> files, String uploadMode, ProjectProfile profile) {
        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            return "UNKNOWN";
        }

        if ("SELECTED_CONTENT".equalsIgnoreCase(uploadMode)) {
            return files.isEmpty() ? "UNKNOWN" : "PARTIAL";
        }

        return files.isEmpty() ? "UNKNOWN" : "COMPLETE";
    }

    private List<String> readLinesSafely(Path path) {
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            try {
                return Files.readAllLines(path, StandardCharsets.ISO_8859_1);
            } catch (Exception ex) {
                return Collections.emptyList();
            }
        }
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx) : "";
    }

    private void redactSecretsInFinding(Finding finding) {
        if (finding.getEvidence() != null) {
            finding.setEvidence(SECRET_PATTERN.matcher(finding.getEvidence()).replaceAll("$1=[REDACTED SECRET]"));
        }
        if (finding.getDescription() != null) {
            finding.setDescription(SECRET_PATTERN.matcher(finding.getDescription()).replaceAll("$1=[REDACTED SECRET]"));
        }
    }
}
