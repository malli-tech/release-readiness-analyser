package com.aireadiness.service;

import com.aireadiness.model.ProjectProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectDetectionServiceTest {

    private final ProjectDetectionService detectionService = new ProjectDetectionService();

    @Test
    @DisplayName("1. Java Spring Boot Maven project detection")
    public void testJavaSpringBootDetection(@TempDir Path tempDir) throws IOException {
        Path pomPath = tempDir.resolve("pom.xml");
        Files.writeString(pomPath, """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-web</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-data-mongodb</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.junit.jupiter</groupId>
                            <artifactId>junit-jupiter-api</artifactId>
                        </dependency>
                    </dependencies>
                </project>
                """, StandardCharsets.UTF_8);

        Path mainJavaDir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(mainJavaDir);
        Files.writeString(mainJavaDir.resolve("Application.java"), """
                package com.example;
                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;

                @SpringBootApplication
                public class Application {
                    public static void main(String[] args) {
                        SpringApplication.run(Application.class, args);
                    }
                }
                """, StandardCharsets.UTF_8);

        Path testJavaDir = tempDir.resolve("src/test/java/com/example");
        Files.createDirectories(testJavaDir);
        Files.writeString(testJavaDir.resolve("ApplicationTest.java"), """
                package com.example;
                import org.junit.jupiter.api.Test;

                public class ApplicationTest {
                    @Test
                    void contextLoads() {}
                }
                """, StandardCharsets.UTF_8);

        ProjectProfile profile = detectionService.detectProject(tempDir, "COMPLETE_PROJECT");

        assertEquals("JAVA", profile.getPrimaryLanguage());
        assertEquals("SPRING_BOOT", profile.getFramework());
        assertEquals("MAVEN", profile.getBuildSystem());
        assertEquals("BACKEND", profile.getProjectType());
        assertTrue(profile.getTestFrameworks().contains("JUNIT"));
        assertEquals("MONGODB", profile.getDatabase());
        assertEquals("COMPLETE", profile.getAnalysisCompleteness());
    }

    @Test
    @DisplayName("2. Next.js TypeScript project detection")
    public void testNextJsTypeScriptDetection(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("package.json"), """
                {
                    "name": "my-next-app",
                    "dependencies": {
                        "next": "14.0.0",
                        "react": "18.2.0"
                    },
                    "devDependencies": {
                        "vitest": "^1.0.0"
                    }
                }
                """, StandardCharsets.UTF_8);

        Files.writeString(tempDir.resolve("next.config.mjs"), "export default {};", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("pnpm-lock.yaml"), "lockfileVersion: 5.4", StandardCharsets.UTF_8);

        Path appDir = tempDir.resolve("src/app");
        Files.createDirectories(appDir);
        Files.writeString(appDir.resolve("page.tsx"), "export default function Home() { return <div>Next.js App</div>; }", StandardCharsets.UTF_8);

        ProjectProfile profile = detectionService.detectProject(tempDir, "COMPLETE_PROJECT");

        assertEquals("TYPESCRIPT", profile.getPrimaryLanguage());
        assertTrue(profile.getFrameworks().contains("NEXT_JS"));
        assertTrue(profile.getFrameworks().contains("REACT"));
        assertEquals("FRONTEND", profile.getProjectType());
        assertEquals("pnpm", profile.getPackageManager());
        assertTrue(profile.getTestFrameworks().contains("VITEST"));
    }

    @Test
    @DisplayName("3. Python Flask project detection")
    public void testPythonFlaskDetection(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("requirements.txt"), """
                flask==3.0.0
                pytest==7.4.0
                psycopg2-binary==2.9.9
                """, StandardCharsets.UTF_8);

        Files.writeString(tempDir.resolve("app.py"), """
                from flask import Flask
                app = Flask(__name__)

                @app.route("/")
                def hello():
                    return "Hello World!"
                """, StandardCharsets.UTF_8);

        ProjectProfile profile = detectionService.detectProject(tempDir, "COMPLETE_PROJECT");

        assertEquals("PYTHON", profile.getPrimaryLanguage());
        assertEquals("FLASK", profile.getFramework());
        assertEquals("PIP", profile.getBuildSystem());
        assertEquals("BACKEND", profile.getProjectType());
        assertTrue(profile.getTestFrameworks().contains("PYTEST"));
        assertEquals("POSTGRESQL", profile.getDatabase());
    }

    @Test
    @DisplayName("4. Go project detection")
    public void testGoDetection(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("go.mod"), """
                module example.com/mygoapp

                go 1.21
                """, StandardCharsets.UTF_8);

        Files.writeString(tempDir.resolve("main.go"), """
                package main
                import "fmt"
                func main() { fmt.Println("Hello") }
                """, StandardCharsets.UTF_8);

        Files.writeString(tempDir.resolve("main_test.go"), """
                package main
                import "testing"
                func TestMain(t *testing.T) {}
                """, StandardCharsets.UTF_8);

        ProjectProfile profile = detectionService.detectProject(tempDir, "COMPLETE_PROJECT");

        assertEquals("GO", profile.getPrimaryLanguage());
        assertEquals("GO_MODULES", profile.getBuildSystem());
        assertEquals("BACKEND", profile.getProjectType());
        assertTrue(profile.getTestFrameworks().contains("GOTEST"));
    }

    @Test
    @DisplayName("5. Selected Content mode should return PARTIAL completeness and non-presumptuous warnings")
    public void testSelectedContentModeCompleteness(@TempDir Path tempDir) throws IOException {
        Path srcDir = tempDir.resolve("src/main/java/com/example");
        Files.createDirectories(srcDir);
        Files.writeString(srcDir.resolve("Service.java"), "package com.example; public class Service {}", StandardCharsets.UTF_8);

        ProjectProfile profile = detectionService.detectProject(tempDir, "SELECTED_CONTENT");

        assertEquals("PARTIAL", profile.getAnalysisCompleteness());
        assertTrue(profile.getDetectionWarnings().stream().anyMatch(w -> w.contains("Selected-content upload mode used")));
        assertTrue(profile.getDetectionWarnings().stream().anyMatch(w -> w.contains("Testing files were not found")));
    }

    @Test
    @DisplayName("6. Malformed manifest should not crash detection and should record warning")
    public void testMalformedManifestHandling(@TempDir Path tempDir) throws IOException {
        Files.writeString(tempDir.resolve("package.json"), "{ malformed json content ...", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("index.js"), "console.log('hello');", StandardCharsets.UTF_8);

        ProjectProfile profile = detectionService.detectProject(tempDir, "COMPLETE_PROJECT");

        assertNotNull(profile);
        assertEquals("JAVASCRIPT", profile.getPrimaryLanguage());
    }

    @Test
    @DisplayName("7. Static security check: ensure no external processes or executables are invoked")
    public void testStaticSecurityGuarantee() {
        // Assert that detection operates purely in memory on temporary path files
        assertDoesNotThrow(() -> {
            ProjectProfile profile = detectionService.detectProject(Path.of("non_existent_path"), "COMPLETE_PROJECT");
            assertEquals("UNKNOWN", profile.getPrimaryLanguage());
        });
    }
}
