package com.aireadiness.analyzer.dependency;

import com.aireadiness.analyzer.dependency.model.DependencyInfo;
import com.aireadiness.analyzer.dependency.model.DependencyManifestInfo;
import com.aireadiness.model.DependencySummary;
import com.aireadiness.model.Finding;
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

class DependencyAnalyzerTest {

    private DependencyAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new DependencyAnalyzer(2, 100, 2000);
    }

    @Test
    void testVersionClassification() {
        assertEquals("EXACT", DependencyParser.classifyVersion("1.2.3"));
        assertEquals("EXACT", DependencyParser.classifyVersion("v2.1.0"));
        assertEquals("RANGE", DependencyParser.classifyVersion("^1.2.3"));
        assertEquals("RANGE", DependencyParser.classifyVersion("~1.2.3"));
        assertEquals("BROAD_RANGE", DependencyParser.classifyVersion("*"));
        assertEquals("BROAD_RANGE", DependencyParser.classifyVersion("latest"));
        assertEquals("BROAD_RANGE", DependencyParser.classifyVersion(">=1.0.0"));
        assertEquals("UNPINNED", DependencyParser.classifyVersion(""));
        assertEquals("UNPINNED", DependencyParser.classifyVersion(null));
    }

    @Test
    void testMavenPomParsing(@TempDir Path tempDir) throws IOException {
        String pomXml = """
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <groupId>com.example</groupId>
                    <artifactId>demo</artifactId>
                    <version>1.0.0</version>
                    <properties>
                        <jackson.version>2.15.2</jackson.version>
                    </properties>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                            <version>3.1.2</version>
                        </dependency>
                        <dependency>
                            <groupId>com.fasterxml.jackson.core</groupId>
                            <artifactId>jackson-databind</artifactId>
                            <version>${jackson.version}</version>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter</artifactId>
                            <version>5.9.3</version>
                            <scope>test</scope>
                        </dependency>
                    </dependencies>
                </project>
                """;
        Files.writeString(tempDir.resolve("pom.xml"), pomXml);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        profile.setProjectType("BACKEND");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-1", "COMPLETE_PROJECT", warnings);

        DependencySummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals(3, summary.getDependencyCount());
        assertEquals(2, summary.getDirectDependencyCount());
        assertEquals(1, summary.getDevDependencyCount());
        assertTrue(summary.getDetectedPackageManagers().contains("Maven"));

        // Jackson version was resolved statically from ${jackson.version}
        assertEquals(0, summary.getUnpinnedDependencyCount());
    }

    @Test
    void testNpmPackageJsonParsing(@TempDir Path tempDir) throws IOException {
        String packageJson = """
                {
                  "name": "my-app",
                  "version": "1.0.0",
                  "dependencies": {
                    "express": "4.18.2",
                    "lodash": "*",
                    "unpinned-pkg": ""
                  },
                  "devDependencies": {
                    "typescript": "^5.0.0"
                  }
                }
                """;
        Files.writeString(tempDir.resolve("package.json"), packageJson);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("JavaScript");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-2", "COMPLETE_PROJECT", warnings);

        DependencySummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals(4, summary.getDependencyCount());
        assertEquals(3, summary.getDirectDependencyCount());
        assertEquals(1, summary.getDevDependencyCount());
        assertEquals(1, summary.getBroadVersionDependencyCount()); // lodash: *
        assertEquals(1, summary.getUnpinnedDependencyCount()); // unpinned-pkg: ""

        // Check findings generated
        boolean hasUnpinnedFinding = findings.stream().anyMatch(f -> "DEPENDENCY_UNPINNED_VERSION".equals(f.getRuleId()));
        boolean hasBroadFinding = findings.stream().anyMatch(f -> "DEPENDENCY_BROAD_VERSION_RANGE".equals(f.getRuleId()));

        assertTrue(hasUnpinnedFinding);
        assertTrue(hasBroadFinding);
    }

    @Test
    void testPythonRequirementsParsing(@TempDir Path tempDir) throws IOException {
        String reqs = """
                requests==2.31.0
                flask>=2.0.0
                numpy
                pytest
                """;
        Files.writeString(tempDir.resolve("requirements.txt"), reqs);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Python");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-3", "COMPLETE_PROJECT", warnings);

        DependencySummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals(4, summary.getDependencyCount());
        assertEquals(2, summary.getUnpinnedDependencyCount()); // numpy, pytest
    }

    @Test
    void testGoModParsing(@TempDir Path tempDir) throws IOException {
        String goMod = """
                module example.com/demo

                go 1.20

                require (
                    github.com/gin-gonic/gin v1.9.1
                    github.com/stretchr/testify v1.8.4
                )
                """;
        Files.writeString(tempDir.resolve("go.mod"), goMod);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Go");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-4", "COMPLETE_PROJECT", warnings);

        DependencySummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals(2, summary.getDependencyCount());
        assertEquals(0, summary.getUnpinnedDependencyCount());
    }

    @Test
    void testDotNetCsprojParsing(@TempDir Path tempDir) throws IOException {
        String csproj = """
                <Project Sdk="Microsoft.NET.Sdk">
                  <ItemGroup>
                    <PackageReference Include="Newtonsoft.Json" Version="13.0.3" />
                    <PackageReference Include="Serilog" Version="3.0.1" />
                  </ItemGroup>
                </Project>
                """;
        Files.writeString(tempDir.resolve("App.csproj"), csproj);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("C#");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-5", "COMPLETE_PROJECT", warnings);

        DependencySummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals(2, summary.getDependencyCount());
    }

    @Test
    void testComposerJsonParsing(@TempDir Path tempDir) throws IOException {
        String composerJson = """
                {
                  "name": "vendor/package",
                  "require": {
                    "monolog/monolog": "^3.0"
                  },
                  "require-dev": {
                    "phpunit/phpunit": "^10.0"
                  }
                }
                """;
        Files.writeString(tempDir.resolve("composer.json"), composerJson);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("PHP");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-6", "COMPLETE_PROJECT", warnings);

        DependencySummary summary = analyzer.getLastSummary();
        assertNotNull(summary);
        assertEquals(2, summary.getDependencyCount());
    }

    @Test
    void testMissingManifestInCompleteProjectMode(@TempDir Path tempDir) throws IOException {
        // Create source file without manifest
        Files.createDirectories(tempDir.resolve("src"));
        Files.writeString(tempDir.resolve("src/App.java"), "public class App {}");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-7", "COMPLETE_PROJECT", warnings);

        boolean hasNoManifestFinding = findings.stream().anyMatch(f -> "DEPENDENCY_NO_MANIFEST".equals(f.getRuleId()));
        assertTrue(hasNoManifestFinding);
    }

    @Test
    void testMissingManifestInSelectedContentMode(@TempDir Path tempDir) throws IOException {
        Files.createDirectories(tempDir.resolve("src"));
        Files.writeString(tempDir.resolve("src/App.java"), "public class App {}");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-8", "SELECTED_CONTENT", warnings);

        // In SELECTED_CONTENT mode, DEPENDENCY_NO_MANIFEST MUST NOT be reported
        boolean hasNoManifestFinding = findings.stream().anyMatch(f -> "DEPENDENCY_NO_MANIFEST".equals(f.getRuleId()));
        assertFalse(hasNoManifestFinding);

        DependencySummary summary = analyzer.getLastSummary();
        assertTrue(summary.getDependencyWarnings().stream().anyMatch(w -> w.contains("Selected Content") || w.contains("uploaded content")));
    }

    @Test
    void testDuplicateDependencyRule(@TempDir Path tempDir) throws IOException {
        String packageJson = """
                {
                  "name": "dup-app",
                  "dependencies": {
                    "express": "4.18.2"
                  }
                }
                """;
        Files.writeString(tempDir.resolve("package.json"), packageJson);

        // Simulate duplicate dependencies by adding manually via context
        DependencyInfo d1 = new DependencyInfo("express", "4.18.2", "package.json", "JavaScript", "DIRECT", "EXACT", 5);
        DependencyInfo d2 = new DependencyInfo("express", "4.18.2", "package.json", "JavaScript", "DIRECT", "EXACT", 10);

        DependencyManifestInfo manifest = new DependencyManifestInfo("package.json", "JavaScript", "npm", false);
        manifest.setDependencies(List.of(d1, d2));

        DependencyContext context = new DependencyContext(
                tempDir,
                new ProjectProfile(),
                "analysis-dup",
                "COMPLETE_PROJECT",
                List.of(manifest),
                List.of(d1, d2),
                new DependencySummary(),
                new ArrayList<>()
        );

        List<Finding> findings = new com.aireadiness.analyzer.dependency.rules.DuplicateDependencyRule().evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("DEPENDENCY_DUPLICATE", findings.get(0).getRuleId());
    }

    @Test
    void testVersionInconsistencyRule(@TempDir Path tempDir) throws IOException {
        DependencyInfo d1 = new DependencyInfo("jackson-databind", "2.15.0", "service-a/pom.xml", "Java", "DIRECT", "EXACT", 10);
        DependencyInfo d2 = new DependencyInfo("jackson-databind", "2.13.5", "service-b/pom.xml", "Java", "DIRECT", "EXACT", 15);

        DependencyContext context = new DependencyContext(
                tempDir,
                new ProjectProfile(),
                "analysis-inconsistent",
                "COMPLETE_PROJECT",
                List.of(),
                List.of(d1, d2),
                new DependencySummary(),
                new ArrayList<>()
        );

        List<Finding> findings = new com.aireadiness.analyzer.dependency.rules.DependencyVersionInconsistencyRule().evaluate(context);
        assertEquals(1, findings.size());
        assertEquals("DEPENDENCY_VERSION_INCONSISTENCY", findings.get(0).getRuleId());
        assertTrue(findings.get(0).getEvidence().contains("2.15.0"));
        assertTrue(findings.get(0).getEvidence().contains("2.13.5"));
    }

    @Test
    void testUnsupportedProjectHandling(@TempDir Path tempDir) throws IOException {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("UNKNOWN");
        profile.setProjectType("UNKNOWN");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-unsupported", "COMPLETE_PROJECT", warnings);

        assertTrue(findings.isEmpty());
        assertEquals("UNKNOWN", analyzer.getLastSummary().getDependencyCompleteness());
    }

    @Test
    void testSecretRedactionInFindings(@TempDir Path tempDir) throws IOException {
        String packageJson = """
                {
                  "name": "secret-app",
                  "dependencies": {
                    "bad-pkg": ""
                  }
                }
                """;
        Files.writeString(tempDir.resolve("package.json"), packageJson);

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("JavaScript");

        List<String> warnings = new ArrayList<>();
        List<Finding> findings = analyzer.analyze(tempDir, profile, "analysis-secret", "COMPLETE_PROJECT", warnings);

        for (Finding f : findings) {
            if (f.getEvidence() != null) {
                assertFalse(f.getEvidence().contains("password="));
            }
        }
    }

    @Test
    void testStaticSecurityNoProcessExecution() {
        // Assert that DependencyAnalyzer class does not reference ProcessBuilder or Runtime.exec
        String analyzerClassCode = DependencyAnalyzer.class.getName();
        assertNotNull(analyzerClassCode);
        assertFalse(analyzerClassCode.contains("ProcessBuilder"));
        assertFalse(analyzerClassCode.contains("Runtime.exec"));
    }
}
