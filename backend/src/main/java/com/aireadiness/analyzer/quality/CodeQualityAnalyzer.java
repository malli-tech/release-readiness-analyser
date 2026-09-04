package com.aireadiness.analyzer.quality;

import com.aireadiness.analyzer.Analyzer;
import com.aireadiness.analyzer.quality.rules.*;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class CodeQualityAnalyzer implements Analyzer {

    private final long maxFileSizeBytes;
    private final long maxTotalSourceBytes;
    private final int maxSourceFiles;
    private final int maxTotalFindings;

    private final List<QualityRule> rules;

    private static final Set<String> SOURCE_EXTENSIONS = Set.of(
            ".java", ".js", ".jsx", ".ts", ".tsx", ".py", ".cs", ".go", ".php"
    );

    private static final Pattern SECRET_PATTERN = Pattern.compile(
            "(?i)(password|secret|apikey|api_key|token|auth_token|private_key|aws_key)\\s*[:=]\\s*[\"']([^\"']+)[\"']"
    );

    public CodeQualityAnalyzer(
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
                new LongMethodRule(),
                new LargeClassRule(),
                new TooManyParametersRule(),
                new DeepNestingRule(),
                new TodoFixmeRule(),
                new EmptyCatchRule(),
                new MagicNumberRule(),
                new CommentedOutCodeRule(),
                new DuplicateCodeRule(),
                new NamingRule()
        );
    }

    @Override
    public String getType() {
        return "CODE_QUALITY";
    }

    @Override
    public List<Finding> analyze(Path workspaceDir, ProjectProfile profile, String analysisId, String uploadMode, List<String> warnings) {
        List<Finding> findings = new ArrayList<>();
        Set<String> findingDeduplicationKeys = new HashSet<>();

        if (workspaceDir == null || !Files.exists(workspaceDir) || !Files.isDirectory(workspaceDir)) {
            warnings.add("Workspace directory is unavailable for code quality analysis.");
            return findings;
        }

        if ("SELECTED_CONTENT".equalsIgnoreCase(uploadMode)) {
            warnings.add("Analysis covers only the uploaded content. Other project components may not have been included.");
        }

        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            warnings.add("Code quality analysis could not be reliably performed for unknown/unsupported project type.");
        }

        long accumulatedBytes = 0;
        int fileCount = 0;

        try {
            List<Path> allPaths = new ArrayList<>();
            try (var stream = Files.walk(workspaceDir)) {
                stream.forEach(allPaths::add);
            }

            for (Path path : allPaths) {
                if (Files.isDirectory(path)) continue;

                // Path containment safety check
                Path relativePath = workspaceDir.relativize(path).normalize();
                if (relativePath.toString().startsWith("..")) continue;

                String relPathStr = relativePath.toString().replace('\\', '/');
                String ext = getExtension(path.getFileName().toString()).toLowerCase();

                if (!SOURCE_EXTENSIONS.contains(ext)) continue;

                fileCount++;
                if (fileCount > maxSourceFiles) {
                    warnings.add("Exceeded maximum allowed source file count limit (" + maxSourceFiles + "). Remaining files were skipped.");
                    break;
                }

                long fileSize = Files.size(path);
                if (fileSize > maxFileSizeBytes) {
                    warnings.add("File skipped because it exceeds the configured static analysis size limit: " + relPathStr);
                    continue;
                }

                accumulatedBytes += fileSize;
                if (accumulatedBytes > maxTotalSourceBytes) {
                    warnings.add("Exceeded maximum total source content size limit (" + maxTotalSourceBytes / (1024 * 1024) + " MB). Remaining content skipped.");
                    break;
                }

                List<String> lines = readLinesSafely(path);
                if (lines == null || lines.isEmpty()) continue;

                // Evaluate rules on source lines
                for (QualityRule rule : rules) {
                    if (findings.size() >= maxTotalFindings) {
                        warnings.add("Maximum analysis findings count reached (" + maxTotalFindings + "). Further findings truncated.");
                        break;
                    }

                    try {
                        List<Finding> ruleFindings = rule.evaluate(relPathStr, lines, profile, analysisId);
                        for (Finding f : ruleFindings) {
                            String dedupKey = analysisId + ":" + f.getRuleId() + ":" + f.getFilePath() + ":" + (f.getLineNumber() != null ? f.getLineNumber() : 0);
                            if (findingDeduplicationKeys.add(dedupKey)) {
                                redactSecretsInFinding(f);
                                findings.add(f);
                            }
                        }
                    } catch (Exception e) {
                        warnings.add("Rule " + rule.getRuleId() + " failed on file " + relPathStr + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            warnings.add("Code quality analysis encountered an error: " + e.getMessage());
        }

        if (fileCount == 0) {
            warnings.add("No analyzable source files were found in the uploaded content.");
        }

        return findings;
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
            finding.setEvidence(SECRET_PATTERN.matcher(finding.getEvidence()).replaceAll("$1=***REDACTED***"));
        }
        if (finding.getDescription() != null) {
            finding.setDescription(SECRET_PATTERN.matcher(finding.getDescription()).replaceAll("$1=***REDACTED***"));
        }
    }
}
