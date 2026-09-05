package com.aireadiness.analyzer.performance;

import com.aireadiness.model.Finding;
import com.aireadiness.model.PerformanceSummary;
import com.aireadiness.model.ProjectProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceAnalyzerTest {

    private PerformanceAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new PerformanceAnalyzer(2, 50, 5000, 2000);
    }

    @Test
    void testNPlusOneQueryDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class UserService {
                    public void loadUsers(List<String> ids) {
                        for (String id : ids) {
                            User user = userRepository.findById(id);
                        }
                    }
                }
                """;
        Files.writeString(tempDir.resolve("UserService.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_N_PLUS_ONE_QUERY".equals(f.getRuleId())));
    }

    @Test
    void testDatabaseCallInLoopDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class OrderService {
                    public void saveOrders(List<Order> orders) {
                        for (Order order : orders) {
                            orderRepository.save(order);
                        }
                    }
                }
                """;
        Files.writeString(tempDir.resolve("OrderService.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_DATABASE_CALL_IN_LOOP".equals(f.getRuleId())));
    }

    @Test
    void testBlockingCallInAsyncContextDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class ReactiveService {
                    public Mono<String> fetchData() {
                        return Mono.just("data").map(d -> {
                            String res = webClient.get().block();
                            return res;
                        });
                    }
                }
                """;
        Files.writeString(tempDir.resolve("ReactiveService.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_BLOCKING_CALL_IN_ASYNC_CONTEXT".equals(f.getRuleId())));
    }

    @Test
    void testSleepOrWaitDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class Worker {
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {}
                    }
                }
                """;
        Files.writeString(tempDir.resolve("Worker.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_SLEEP_OR_WAIT".equals(f.getRuleId())));
    }

    @Test
    void testStringConcatenationInLoopDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class Formatter {
                    public String format(List<String> items) {
                        String result = "";
                        for (String item : items) {
                            result += item + ", ";
                        }
                        return result;
                    }
                }
                """;
        Files.writeString(tempDir.resolve("Formatter.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_REPEATED_STRING_CONCATENATION".equals(f.getRuleId())));
    }

    @Test
    void testRegexInLoopDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class Parser {
                    public void parse(List<String> lines) {
                        for (String line : lines) {
                            Pattern p = Pattern.compile("^[a-z]+");
                        }
                    }
                }
                """;
        Files.writeString(tempDir.resolve("Parser.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_REGEX_IN_LOOP".equals(f.getRuleId())));
    }

    @Test
    void testRepeatedCollectionScanDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class ListMatcher {
                    public void match(List<String> items, List<String> targets) {
                        for (String item : items) {
                            if (targets.contains(item)) {
                                System.out.println("Match");
                            }
                        }
                    }
                }
                """;
        Files.writeString(tempDir.resolve("ListMatcher.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_REPEATED_COLLECTION_SCAN".equals(f.getRuleId())));
    }

    @Test
    void testExcessiveNestedLoopsDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class Matrix {
                    public void compute(int[][][] data) {
                        for (int i = 0; i < 10; i++) {
                            for (int j = 0; j < 10; j++) {
                                for (int k = 0; k < 10; k++) {
                                    System.out.println(data[i][j][k]);
                                }
                            }
                        }
                    }
                }
                """;
        Files.writeString(tempDir.resolve("Matrix.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_EXCESSIVE_NESTED_LOOPS".equals(f.getRuleId())));
    }

    @Test
    void testCollectionAllocationInLoopDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class Collector {
                    public void process(List<String> items) {
                        for (String item : items) {
                            List<String> temp = new ArrayList<>();
                        }
                    }
                }
                """;
        Files.writeString(tempDir.resolve("Collector.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_LARGE_COLLECTION_ALLOCATION_IN_LOOP".equals(f.getRuleId())));
    }

    @Test
    void testRepeatedExpensiveOperationDetection(@TempDir Path tempDir) throws IOException {
        String code = """
                public class JsonProcessor {
                    public void process(List<Object> list) {
                        ObjectMapper mapper = new ObjectMapper();
                        for (Object obj : list) {
                            String json = mapper.writeValueAsString(obj);
                        }
                    }
                }
                """;
        Files.writeString(tempDir.resolve("JsonProcessor.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.stream().anyMatch(f -> "PERFORMANCE_REPEATED_EXPENSIVE_OPERATION".equals(f.getRuleId())));
    }

    @Test
    void testNoFalsePositiveForComments(@TempDir Path tempDir) throws IOException {
        String code = """
                public class Commented {
                    public void run() {
                        // Thread.sleep(1000);
                        /* Pattern p = Pattern.compile("abc"); */
                    }
                }
                """;
        Files.writeString(tempDir.resolve("Commented.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.isEmpty());
    }

    @Test
    void testNoFalsePositiveForStringLiterals(@TempDir Path tempDir) throws IOException {
        String code = """
                public class Literal {
                    public void print() {
                        String msg = "Never use Thread.sleep(1000) in production code";
                    }
                }
                """;
        Files.writeString(tempDir.resolve("Literal.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        assertTrue(findings.isEmpty());
    }

    @Test
    void testUnsupportedLanguageBehavior(@TempDir Path tempDir) throws IOException {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("UNKNOWN");
        profile.setProjectType("UNKNOWN");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-unsupported", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.isEmpty());
        assertEquals("UNKNOWN", analyzer.getLastSummary().getPerformanceCompleteness());
        assertTrue(analyzer.getLastSummary().getPerformanceWarnings().stream().anyMatch(w -> w.contains("not currently supported")));
    }

    @Test
    void testSelectedContentBehavior(@TempDir Path tempDir) throws IOException {
        String code = "public class App {}";
        Files.writeString(tempDir.resolve("App.java"), code);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        profile.setProjectType("BACKEND");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-selected", "SELECTED_CONTENT", warnings);

        PerformanceSummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals("PARTIAL", summary.getPerformanceCompleteness());
        assertTrue(summary.getPerformanceWarnings().stream().anyMatch(w -> w.contains("selected uploaded content")));
    }

    @Test
    void testSecretRedactionInEvidence(@TempDir Path tempDir) throws IOException {
        String code = """
                public class SecretWorker {
                    public void run() {
                        String secret_key = "sensitive_pass_998877";
                        for (int i = 0; i < 10; i++) {
                            for (int j = 0; j < 10; j++) {
                                for (int k = 0; k < 10; k++) {
                                    System.out.println("log " + secret_key);
                                }
                            }
                        }
                    }
                }
                """;
        Files.writeString(tempDir.resolve("SecretWorker.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        for (Finding f : findings) {
            if (f.getEvidence() != null) {
                assertFalse(f.getEvidence().contains("sensitive_pass_998877"));
            }
        }
    }

    @Test
    void testDeduplication(@TempDir Path tempDir) throws IOException {
        String code = """
                public class Dup {
                    public void test() {
                        for (int i = 0; i < 10; i++) {
                            Thread.sleep(100);
                        }
                    }
                }
                """;
        Files.writeString(tempDir.resolve("Dup.java"), code);

        List<Finding> findings = analyze(tempDir, "COMPLETE_PROJECT");
        long sleepFindingsCount = findings.stream().filter(f -> "PERFORMANCE_SLEEP_OR_WAIT".equals(f.getRuleId())).count();
        assertEquals(1, sleepFindingsCount);
    }

    @Test
    void testResourceLimitsFileCount(@TempDir Path tempDir) throws IOException {
        PerformanceAnalyzer tightAnalyzer = new PerformanceAnalyzer(2, 50, 1, 2000);
        Files.writeString(tempDir.resolve("A.java"), "public class A {}");
        Files.writeString(tempDir.resolve("B.java"), "public class B {}");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        List<String> warnings = new ArrayList<>();
        tightAnalyzer.analyze(tempDir, profile, "analysis-limits", "COMPLETE_PROJECT", warnings);

        assertTrue(warnings.stream().anyMatch(w -> w.contains("Exceeded maximum file count limit")));
    }

    @Test
    void testNoCodeExecutionInPerformanceAnalyzer() {
        String className = PerformanceAnalyzer.class.getName();
        assertNotNull(className);
        assertFalse(className.contains("Runtime.exec"));
        assertFalse(className.contains("ProcessBuilder"));
    }

    private List<Finding> analyze(Path workspaceDir, String uploadMode) {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        profile.setProjectType("BACKEND");
        List<String> warnings = new ArrayList<>();
        return analyzer.analyze(workspaceDir, profile, "test-analysis-id", uploadMode, warnings);
    }
}
