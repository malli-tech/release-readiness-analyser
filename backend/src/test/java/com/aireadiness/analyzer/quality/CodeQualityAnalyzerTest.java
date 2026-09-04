package com.aireadiness.analyzer.quality;

import com.aireadiness.model.Finding;
import com.aireadiness.model.ProjectProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodeQualityAnalyzerTest {

    private final CodeQualityAnalyzer analyzer = new CodeQualityAnalyzer(2, 50, 5000, 2000);

    private ProjectProfile createSampleProfile(String lang, String framework, String projectType) {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage(lang);
        profile.setFramework(framework);
        profile.setProjectType(projectType);
        return profile;
    }

    @Test
    @DisplayName("1. Long Method rule detection (>100 non-blank lines)")
    public void testLongMethodRule(@TempDir Path tempDir) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("public class TestClass {\n");
        sb.append("    public void hugeMethod() {\n");
        for (int i = 1; i <= 120; i++) {
            sb.append("        int var").append(i).append(" = ").append(i).append(";\n");
        }
        sb.append("    }\n");
        sb.append("}\n");

        Files.writeString(tempDir.resolve("TestClass.java"), sb.toString(), StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> f.getRuleId().equals("CODE_QUALITY_LONG_METHOD")));
    }

    @Test
    @DisplayName("2. Large Class rule detection (>500 lines)")
    public void testLargeClassRule(@TempDir Path tempDir) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("public class GiantClass {\n");
        for (int i = 1; i <= 550; i++) {
            sb.append("    private String field").append(i).append(" = \"val\";\n");
        }
        sb.append("}\n");

        Files.writeString(tempDir.resolve("GiantClass.java"), sb.toString(), StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> f.getRuleId().equals("CODE_QUALITY_LARGE_CLASS")));
    }

    @Test
    @DisplayName("3. Too Many Parameters rule detection (>6 parameters)")
    public void testTooManyParametersRule(@TempDir Path tempDir) throws IOException {
        String code = """
                public class Controller {
                    public void processOrder(String a, String b, String c, String d, String e, String f, String g) {
                        System.out.println("Processing");
                    }
                }
                """;

        Files.writeString(tempDir.resolve("Controller.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> f.getRuleId().equals("CODE_QUALITY_TOO_MANY_PARAMETERS")));
    }

    @Test
    @DisplayName("4. Deep Nesting rule detection (>4 levels)")
    public void testDeepNestingRule(@TempDir Path tempDir) throws IOException {
        String code = """
                public class NestingClass {
                    public void check() {
                        if (true) {
                            for (int i = 0; i < 10; i++) {
                                if (i > 2) {
                                    while (true) {
                                        if (i == 5) {
                                            System.out.println("Deep");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                """;

        Files.writeString(tempDir.resolve("NestingClass.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> f.getRuleId().equals("CODE_QUALITY_DEEP_NESTING")));
    }

    @Test
    @DisplayName("5. TODO / FIXME comment marker detection")
    public void testTodoFixmeRule(@TempDir Path tempDir) throws IOException {
        String code = """
                public class TodoClass {
                    // TODO: implement real authentication
                    // FIXME: resolve memory leak
                }
                """;

        Files.writeString(tempDir.resolve("TodoClass.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        long todoCount = findings.stream().filter(f -> f.getRuleId().equals("CODE_QUALITY_TODO_FIXME")).count();
        assertEquals(2, todoCount);
    }

    @Test
    @DisplayName("6. Empty Exception Handler rule detection")
    public void testEmptyCatchRule(@TempDir Path tempDir) throws IOException {
        String code = """
                public class CatchClass {
                    public void handle() {
                        try {
                            int x = 10 / 0;
                        } catch (Exception e) {
                        }
                    }
                }
                """;

        Files.writeString(tempDir.resolve("CatchClass.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> f.getRuleId().equals("CODE_QUALITY_EMPTY_EXCEPTION_HANDLER")));
    }

    @Test
    @DisplayName("7. Magic Number Rule detection")
    public void testMagicNumberRule(@TempDir Path tempDir) throws IOException {
        String code = """
                public class MagicClass {
                    public void calc() {
                        int timeout = 86437;
                        if (timeout > 99999) {}
                    }
                }
                """;

        Files.writeString(tempDir.resolve("MagicClass.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> f.getRuleId().equals("CODE_QUALITY_MAGIC_NUMBER")));
    }

    @Test
    @DisplayName("8. Commented-Out Code Rule detection")
    public void testCommentedOutCodeRule(@TempDir Path tempDir) throws IOException {
        String code = """
                public class DeadCodeClass {
                    public void execute() {
                        // if (user != null) {
                        //     process(user);
                        //     return true;
                        // }
                    }
                }
                """;

        Files.writeString(tempDir.resolve("DeadCodeClass.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> f.getRuleId().equals("CODE_QUALITY_COMMENTED_OUT_CODE")));
    }

    @Test
    @DisplayName("9. Duplicated Code Block detection")
    public void testDuplicateCodeRule(@TempDir Path tempDir) throws IOException {
        String code = """
                public class DupClass {
                    public void first() {
                        System.out.println("Line A");
                        System.out.println("Line B");
                        System.out.println("Line C");
                        System.out.println("Line D");
                        System.out.println("Line E");
                        System.out.println("Line F");
                    }
                    public void second() {
                        System.out.println("Line A");
                        System.out.println("Line B");
                        System.out.println("Line C");
                        System.out.println("Line D");
                        System.out.println("Line E");
                        System.out.println("Line F");
                    }
                }
                """;

        Files.writeString(tempDir.resolve("DupClass.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> f.getRuleId().equals("CODE_QUALITY_DUPLICATED_CODE")));
    }

    @Test
    @DisplayName("10. Poor Naming Rule detection")
    public void testNamingRule(@TempDir Path tempDir) throws IOException {
        String code = """
                public class NamingClass {
                    public void calc() {
                        int q = 42;
                    }
                }
                """;

        Files.writeString(tempDir.resolve("NamingClass.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.stream().anyMatch(f -> f.getRuleId().equals("CODE_QUALITY_POOR_NAMING")));
    }

    @Test
    @DisplayName("11. Multi-language support (Python, JS/TS, Go)")
    public void testMultiLanguageSupport(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("app.py"), "def func(a, b, c, d, e, f, g, h):\n    pass\n# TODO: fix python\n", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("main.go"), "package main\nfunc main() {\n    // TODO: fix go\n}\n", StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("PYTHON", "FLASK", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertFalse(findings.isEmpty());
    }

    @Test
    @DisplayName("12. Selected Content mode should add notice warning")
    public void testSelectedContentWarning(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("App.java"), "public class App {}", StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "SELECTED_CONTENT", warnings);

        assertTrue(warnings.stream().anyMatch(w -> w.contains("Analysis covers only the uploaded content")));
    }

    @Test
    @DisplayName("13. Unsupported project type should handle gracefully with warning")
    public void testUnsupportedProjectType(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("unknown.xyz"), "unknown data", StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("UNKNOWN", "UNKNOWN", "UNKNOWN"), "ans-1", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.isEmpty());
        assertTrue(warnings.stream().anyMatch(w -> w.contains("Code quality analysis could not be reliably performed") || w.contains("No analyzable source files")));
    }

    @Test
    @DisplayName("14. Secret Redaction in Evidence Snippets")
    public void testSecretRedaction(@TempDir Path tempDir) throws IOException {
        String code = """
                public class SecretClass {
                    // TODO: password = "SecretPass123!"
                }
                """;

        Files.writeString(tempDir.resolve("SecretClass.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        Finding todo = findings.stream().filter(f -> f.getRuleId().equals("CODE_QUALITY_TODO_FIXME")).findFirst().orElseThrow();
        assertFalse(todo.getEvidence().contains("SecretPass123!"));
        assertTrue(todo.getEvidence().contains("***REDACTED***"));
    }

    @Test
    @DisplayName("15. Finding Deduplication")
    public void testFindingDeduplication(@TempDir Path tempDir) throws IOException {
        String code = "// TODO: duplicate todo\n";
        Files.writeString(tempDir.resolve("DupTodo.java"), code, StandardCharsets.UTF_8);

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, createSampleProfile("JAVA", "SPRING_BOOT", "BACKEND"), "ans-1", "COMPLETE_PROJECT", warnings);

        long count = findings.stream().filter(f -> f.getRuleId().equals("CODE_QUALITY_TODO_FIXME")).count();
        assertEquals(1, count);
    }
}
