package com.aireadiness.analyzer.testing;

import com.aireadiness.analyzer.Analyzer;
import com.aireadiness.analyzer.testing.model.ParsedFileInfo;
import com.aireadiness.analyzer.testing.model.TestMethodInfo;
import com.aireadiness.analyzer.testing.rules.*;
import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;
import com.aireadiness.model.TestingSummary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class TestingAnalyzer implements Analyzer {

    private final long maxFileSizeBytes;
    private final long maxTotalSourceBytes;
    private final int maxSourceFiles;
    private final int maxTotalFindings;

    private final List<TestingRule> rules;

    private TestingSummary lastSummary;

    private static final Set<String> SOURCE_EXTENSIONS = Set.of(
            ".java", ".js", ".jsx", ".ts", ".tsx", ".py", ".cs", ".go", ".php"
    );

    private static final Pattern SECRET_PATTERN = Pattern.compile(
            "(?i)(password|secret|apikey|api_key|token|auth_token|private_key|aws_key)\\s*[:=]\\s*[\"']([^\"']+)[\"']"
    );

    public TestingAnalyzer(
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
                new NoTestFilesRule(),
                new LowTestPresenceRule(),
                new EmptyTestRule(),
                new NoAssertionRule(),
                new SkippedTestRule(),
                new TodoTestRule(),
                new OrphanTestRule(),
                new UntestedSourceRule(),
                new ExcessiveSkippedTestsRule(),
                new PoorTestOrganizationRule()
        );
    }

    @Override
    public String getType() {
        return "TESTING";
    }

    public TestingSummary getLastSummary() {
        return lastSummary;
    }

    @Override
    public List<Finding> analyze(Path workspaceDir, ProjectProfile profile, String analysisId, String uploadMode, List<String> warnings) {
        List<Finding> findings = new ArrayList<>();
        Set<String> findingDeduplicationKeys = new HashSet<>();

        TestingSummary summary = new TestingSummary();
        this.lastSummary = summary;

        if (workspaceDir == null || !Files.exists(workspaceDir) || !Files.isDirectory(workspaceDir)) {
            warnings.add("Workspace directory is unavailable for static testing analysis.");
            summary.setTestingCompleteness("UNKNOWN");
            summary.getTestingWarnings().add("Workspace directory is unavailable.");
            return findings;
        }

        if ("SELECTED_CONTENT".equalsIgnoreCase(uploadMode)) {
            summary.getTestingWarnings().add("Analysis covers only the uploaded content. Tests may exist outside the uploaded files.");
        }

        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            warnings.add("Testing analysis could not reliably identify the project's test structure.");
            summary.getTestingWarnings().add("Testing analysis could not reliably identify the project's test structure.");
            summary.setTestingCompleteness("UNKNOWN");
            return findings;
        }

        List<ParsedFileInfo> allParsedFiles = new ArrayList<>();
        List<ParsedFileInfo> testFiles = new ArrayList<>();
        List<ParsedFileInfo> sourceFiles = new ArrayList<>();

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
                String ext = getExtension(path.getFileName().toString()).toLowerCase();

                if (!SOURCE_EXTENSIONS.contains(ext)) continue;

                fileCount++;
                if (fileCount > maxSourceFiles) {
                    warnings.add("Exceeded maximum allowed source file count limit (" + maxSourceFiles + "). Remaining files were skipped.");
                    break;
                }

                long fileSize = Files.size(path);
                if (fileSize > maxFileSizeBytes) {
                    warnings.add("File skipped because it exceeds static analysis size limit: " + relPathStr);
                    continue;
                }

                accumulatedBytes += fileSize;
                if (accumulatedBytes > maxTotalSourceBytes) {
                    warnings.add("Exceeded maximum total source content size limit (" + maxTotalSourceBytes / (1024 * 1024) + " MB). Remaining content skipped.");
                    break;
                }

                List<String> lines = readLinesSafely(path);
                if (lines == null || lines.isEmpty()) continue;

                boolean isTest = isTestFile(relPathStr, path.getFileName().toString());
                ParsedFileInfo parsedFile = new ParsedFileInfo(relPathStr, path.getFileName().toString(), isTest, lines);

                if (isTest) {
                    List<TestMethodInfo> methods = TestParser.parseTestMethods(path.getFileName().toString(), lines);
                    parsedFile.setTestMethods(methods);
                    testFiles.add(parsedFile);
                } else {
                    sourceFiles.add(parsedFile);
                }
                allParsedFiles.add(parsedFile);
            }
        } catch (Exception e) {
            warnings.add("Static testing analysis encountered an error: " + e.getMessage());
        }

        // Framework Detection
        Set<String> detectedFrameworks = detectFrameworks(allParsedFiles, profile);
        summary.setDetectedFrameworks(new ArrayList<>(detectedFrameworks));

        if (!testFiles.isEmpty() && detectedFrameworks.isEmpty()) {
            summary.getTestingWarnings().add("Test-like files were detected, but the testing framework could not be confidently identified.");
        }

        // Aggregate statistics for TestingSummary
        int totalTests = 0;
        int totalAssertions = 0;
        int totalSkipped = 0;
        int totalEmpty = 0;
        int totalTodo = 0;
        int testFilesNoAssertions = 0;

        for (ParsedFileInfo tf : testFiles) {
            boolean fileHasAssertion = false;
            for (TestMethodInfo tm : tf.getTestMethods()) {
                totalTests++;
                if (tm.isHasAssertion()) {
                    totalAssertions++;
                    fileHasAssertion = true;
                }
                if (tm.isSkipped()) totalSkipped++;
                if (tm.isEmpty()) totalEmpty++;
                if (tm.isHasTodo()) totalTodo++;
            }
            if (!fileHasAssertion && !tf.getTestMethods().isEmpty()) {
                testFilesNoAssertions++;
            }
        }

        summary.setTestFiles(testFiles.size());
        summary.setSourceFiles(sourceFiles.size());
        double ratio = sourceFiles.isEmpty() ? 0.0 : (double) testFiles.size() / sourceFiles.size();
        summary.setTestPresenceRatio(ratio);
        summary.setTestsDetected(totalTests);
        summary.setAssertionsDetected(totalAssertions);
        summary.setSkippedTestsDetected(totalSkipped);
        summary.setEmptyTestsDetected(totalEmpty);
        summary.setTodoTestsDetected(totalTodo);
        summary.setTestFilesWithoutObviousAssertions(testFilesNoAssertions);

        // Determine Testing Completeness
        String completeness = calculateCompleteness(summary, uploadMode, profile);
        summary.setTestingCompleteness(completeness);

        // Selected Content warning
        if ("SELECTED_CONTENT".equalsIgnoreCase(uploadMode) && testFiles.isEmpty()) {
            summary.getTestingWarnings().add("Test files were not found in the selected content. Tests may exist outside the uploaded files.");
        }

        TestingContext context = new TestingContext(
                workspaceDir,
                profile,
                analysisId,
                uploadMode,
                allParsedFiles,
                testFiles,
                sourceFiles,
                summary,
                warnings
        );

        // Evaluate testing rules
        for (TestingRule rule : rules) {
            if (findings.size() >= maxTotalFindings) {
                warnings.add("Maximum testing analysis findings count reached (" + maxTotalFindings + "). Further findings truncated.");
                break;
            }

            try {
                List<Finding> ruleFindings = rule.evaluate(context);
                for (Finding f : ruleFindings) {
                    String dedupKey = analysisId + ":" + f.getRuleId() + ":" + (f.getFilePath() != null ? f.getFilePath() : "") + ":" + (f.getLineNumber() != null ? f.getLineNumber() : 0);
                    if (findingDeduplicationKeys.add(dedupKey)) {
                        redactSecretsInFinding(f);
                        findings.add(f);
                    }
                }
            } catch (Exception e) {
                warnings.add("Testing rule " + rule.getRuleId() + " failed: " + e.getMessage());
            }
        }

        return findings;
    }

    public static boolean isTestFile(String relativePath, String fileName) {
        String pathLower = relativePath.toLowerCase();
        String nameLower = fileName.toLowerCase();

        // Path indicator check
        if (pathLower.contains("src/test/") || pathLower.contains("tests/") || pathLower.contains("test/") || pathLower.contains("__tests__/")) {
            return true;
        }

        // File name indicator check
        // JS/TS
        if (nameLower.endsWith(".test.js") || nameLower.endsWith(".test.jsx") || nameLower.endsWith(".test.ts") || nameLower.endsWith(".test.tsx") ||
                nameLower.endsWith(".spec.js") || nameLower.endsWith(".spec.jsx") || nameLower.endsWith(".spec.ts") || nameLower.endsWith(".spec.tsx")) {
            return true;
        }

        // Python
        if (nameLower.startsWith("test_") && nameLower.endsWith(".py")) return true;
        if (nameLower.endsWith("_test.py")) return true;

        // Go
        if (nameLower.endsWith("_test.go")) return true;

        // Java / C# / PHP
        if (nameLower.endsWith("test.java") || nameLower.endsWith("tests.java")) return true;
        if (nameLower.endsWith("test.cs") || nameLower.endsWith("tests.cs")) return true;
        if (nameLower.endsWith("test.php")) return true;

        return false;
    }

    private Set<String> detectFrameworks(List<ParsedFileInfo> files, ProjectProfile profile) {
        Set<String> frameworks = new LinkedHashSet<>();

        if (profile != null && profile.getTestFrameworks() != null) {
            for (String df : profile.getTestFrameworks()) {
                if (df != null && !df.isBlank() && !df.equalsIgnoreCase("UNKNOWN")) {
                    frameworks.add(df);
                }
            }
        }

        for (ParsedFileInfo file : files) {
            String content = String.join("\n", file.getLines());

            // Java
            if (content.contains("org.junit") || content.contains("@Test")) frameworks.add("JUnit");
            if (content.contains("org.mockito") || content.contains("Mockito")) frameworks.add("Mockito");

            // JS/TS
            if (content.contains("describe(") || content.contains("it(") || content.contains("expect(")) {
                if (content.contains("vitest")) frameworks.add("Vitest");
                else frameworks.add("Jest");
            }

            // Python
            if (content.contains("import pytest") || content.contains("@pytest")) frameworks.add("pytest");
            if (content.contains("import unittest") || content.contains("unittest.TestCase")) frameworks.add("unittest");

            // Go
            if (content.contains("import \"testing\"") || content.contains("*testing.T")) frameworks.add("Go testing");

            // C#
            if (content.contains("[Fact]") || content.contains("[Theory]") || content.contains("Xunit")) frameworks.add("xUnit");
            if (content.contains("[TestFixture]") || content.contains("NUnit")) frameworks.add("NUnit");
            if (content.contains("[TestClass]") || content.contains("[TestMethod]")) frameworks.add("MSTest");

            // PHP
            if (content.contains("PHPUnit\\Framework") || content.contains("extends TestCase")) frameworks.add("PHPUnit");
        }

        return frameworks;
    }

    private String calculateCompleteness(TestingSummary summary, String uploadMode, ProjectProfile profile) {
        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            return "UNKNOWN";
        }

        if ("SELECTED_CONTENT".equalsIgnoreCase(uploadMode)) {
            if (summary.getTestFiles() > 0 && summary.getTestPresenceRatio() >= 0.30) {
                return "PARTIAL";
            }
            if (summary.getTestFiles() > 0) {
                return "PARTIAL";
            }
            return "UNKNOWN";
        }

        // COMPLETE_PROJECT
        if (summary.getSourceFiles() == 0) return "UNKNOWN";
        if (summary.getTestFiles() == 0) return "WEAK";

        double ratio = summary.getTestPresenceRatio();
        int totalTests = summary.getTestsDetected();
        int skippedOrEmpty = summary.getSkippedTestsDetected() + summary.getEmptyTestsDetected();

        double errorRate = totalTests > 0 ? (double) skippedOrEmpty / totalTests : 0.0;

        if (ratio >= 0.40 && errorRate < 0.15) {
            return "STRONG";
        } else if (ratio >= 0.20) {
            return "MODERATE";
        } else {
            return "WEAK";
        }
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
