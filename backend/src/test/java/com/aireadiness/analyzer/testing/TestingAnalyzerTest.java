package com.aireadiness.analyzer.testing;

import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;
import com.aireadiness.model.TestingSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestingAnalyzerTest {

    private TestingAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new TestingAnalyzer(2, 50, 5000, 2000);
    }

    @Test
    void testFileDetection_SupportsMultipleLanguages() {
        assertTrue(TestingAnalyzer.isTestFile("src/test/java/UserServiceTest.java", "UserServiceTest.java"));
        assertTrue(TestingAnalyzer.isTestFile("src/test/java/UserServiceTests.java", "UserServiceTests.java"));
        assertTrue(TestingAnalyzer.isTestFile("src/components/User.test.tsx", "User.test.tsx"));
        assertTrue(TestingAnalyzer.isTestFile("src/components/User.spec.js", "User.spec.js"));
        assertTrue(TestingAnalyzer.isTestFile("tests/test_user.py", "test_user.py"));
        assertTrue(TestingAnalyzer.isTestFile("user_test.py", "user_test.py"));
        assertTrue(TestingAnalyzer.isTestFile("user_test.go", "user_test.go"));
        assertTrue(TestingAnalyzer.isTestFile("UserServiceTest.cs", "UserServiceTest.cs"));
        assertTrue(TestingAnalyzer.isTestFile("UserServiceTest.php", "UserServiceTest.php"));

        assertFalse(TestingAnalyzer.isTestFile("src/main/java/UserService.java", "UserService.java"));
    }

    @Test
    void testNoTestFiles_CompleteProject_TriggersHighFinding(@TempDir Path tempDir) throws IOException {
        Path src = tempDir.resolve("src/main/java");
        Files.createDirectories(src);
        Files.writeString(src.resolve("UserService.java"), "public class UserService {}");
        Files.writeString(src.resolve("OrderService.java"), "public class OrderService {}");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        profile.setProjectType("BACKEND");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-1", "COMPLETE_PROJECT", warnings);

        TestingSummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals(0, summary.getTestFiles());
        assertEquals(2, summary.getSourceFiles());
        assertEquals("WEAK", summary.getTestingCompleteness());

        assertTrue(findings.stream().anyMatch(f -> "TESTING_NO_TEST_FILES".equals(f.getRuleId()) && "HIGH".equals(f.getSeverity())));
    }

    @Test
    void testNoTestFiles_SelectedContent_WarningOnly_NoRuleFinding(@TempDir Path tempDir) throws IOException {
        Path src = tempDir.resolve("UserService.java");
        Files.writeString(src, "public class UserService {}");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        profile.setProjectType("BACKEND");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-2", "SELECTED_CONTENT", warnings);

        TestingSummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals(0, summary.getTestFiles());
        assertEquals(1, summary.getSourceFiles());

        assertFalse(findings.stream().anyMatch(f -> "TESTING_NO_TEST_FILES".equals(f.getRuleId())));
        assertTrue(summary.getTestingWarnings().stream().anyMatch(w -> w.contains("Test files were not found in the selected content")));
    }

    @Test
    void testEmptyTestAndNoAssertionAndSkippedAndTodo(@TempDir Path tempDir) throws IOException {
        Path testDir = tempDir.resolve("src/test/java");
        Files.createDirectories(testDir);

        String testContent = """
                package com.example;
                import org.junit.jupiter.api.Test;
                import org.junit.jupiter.api.Disabled;
                import static org.junit.jupiter.api.Assertions.*;

                public class SampleTest {
                    @Test
                    void testEmpty() {
                    }

                    @Test
                    void testNoAssertion() {
                        int x = 10;
                        int y = 20;
                    }

                    @Test
                    @Disabled
                    void testSkipped() {
                        assertEquals(1, 1);
                    }

                    @Test
                    void testWithTodo() {
                        // TODO: add validation here password="Secret123Password!"
                        assertEquals(2, 2);
                    }
                }
                """;
        Files.writeString(testDir.resolve("SampleTest.java"), testContent);

        Path srcDir = tempDir.resolve("src/main/java");
        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("Sample.java"), "public class Sample {}");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        profile.setProjectType("BACKEND");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-3", "COMPLETE_PROJECT", warnings);

        TestingSummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals(1, summary.getTestFiles());
        assertEquals(4, summary.getTestsDetected(), "Tests detected");
        assertEquals(2, summary.getAssertionsDetected(), "Assertions detected");
        assertEquals(1, summary.getSkippedTestsDetected(), "Skipped tests");
        assertEquals(1, summary.getEmptyTestsDetected(), "Empty tests");
        assertEquals(1, summary.getTodoTestsDetected(), "Todo tests detected count=" + summary.getTodoTestsDetected());
        assertTrue(summary.getDetectedFrameworks().contains("JUnit"));

        assertTrue(findings.stream().anyMatch(f -> "TESTING_EMPTY_TEST".equals(f.getRuleId())));
        assertTrue(findings.stream().anyMatch(f -> "TESTING_NO_ASSERTION".equals(f.getRuleId())));
        assertTrue(findings.stream().anyMatch(f -> "TESTING_SKIPPED_TEST".equals(f.getRuleId())));
        assertTrue(findings.stream().anyMatch(f -> "TESTING_TODO_TEST".equals(f.getRuleId())));

        // Secret Redaction check
        Finding todoFinding = findings.stream().filter(f -> "TESTING_TODO_TEST".equals(f.getRuleId())).findFirst().orElseThrow();
        assertFalse(todoFinding.getEvidence().contains("Secret123Password!"));
        assertTrue(todoFinding.getEvidence().contains("***REDACTED***"));
    }

    @Test
    void testExcessiveSkippedTests(@TempDir Path tempDir) throws IOException {
        Path testDir = tempDir.resolve("src/test/java");
        Files.createDirectories(testDir);

        String testContent = """
                import org.junit.jupiter.api.Test;
                import org.junit.jupiter.api.Disabled;
                import static org.junit.jupiter.api.Assertions.*;

                public class MassiveSkipTest {
                    @Test @Disabled void test1() { assertEquals(1,1); }
                    @Test @Disabled void test2() { assertEquals(1,1); }
                    @Test void test3() { assertEquals(1,1); }
                    @Test void test4() { assertEquals(1,1); }
                    @Test void test5() { assertEquals(1,1); }
                }
                """;
        Files.writeString(testDir.resolve("MassiveSkipTest.java"), testContent);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-4", "COMPLETE_PROJECT", warnings);

        TestingSummary summary = analyzer.getLastSummary();
        assertEquals(5, summary.getTestsDetected());
        assertEquals(2, summary.getSkippedTestsDetected()); // 40% > 20%

        assertTrue(findings.stream().anyMatch(f -> "TESTING_EXCESSIVE_SKIPPED_TESTS".equals(f.getRuleId()) && "HIGH".equals(f.getSeverity())));
    }

    @Test
    void testOrphanTestAndUntestedSource_CompleteProject(@TempDir Path tempDir) throws IOException {
        Path testDir = tempDir.resolve("src/test/java");
        Path srcDir = tempDir.resolve("src/main/java");
        Files.createDirectories(testDir);
        Files.createDirectories(srcDir);

        Files.writeString(testDir.resolve("OrphanServiceTest.java"), "@Test void testFoo() { assertEquals(1,1); }");
        Files.writeString(srcDir.resolve("PaymentService.java"), "public class PaymentService {}");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-5", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> "TESTING_ORPHAN_TEST".equals(f.getRuleId())));
        assertTrue(findings.stream().anyMatch(f -> "TESTING_UNTESTED_SOURCE_FILE".equals(f.getRuleId())));
    }

    @Test
    void testUnsupportedProject_ReturnsSafeWarning_NoCrash(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("unknown.xyz"), "xyz content");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("UNKNOWN");
        profile.setProjectType("UNKNOWN");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-6", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.isEmpty());
        TestingSummary summary = analyzer.getLastSummary();
        assertEquals("UNKNOWN", summary.getTestingCompleteness());
        assertTrue(summary.getTestingWarnings().stream().anyMatch(w -> w.contains("could not reliably identify")));
    }
}
