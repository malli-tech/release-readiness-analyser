package com.aireadiness.service;

import com.aireadiness.model.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ProjectDetectionService {

    private static final long MAX_READ_FILE_SIZE = 100 * 1024; // 100KB max per file read

    public ProjectProfile detectProject(Path workspaceDir, String uploadMode) {
        ProjectProfile profile = new ProjectProfile();
        List<String> warnings = new ArrayList<>();
        List<DetectionEvidence> evidences = new ArrayList<>();

        if (workspaceDir == null || !Files.exists(workspaceDir) || !Files.isDirectory(workspaceDir)) {
            profile.setPrimaryLanguage("UNKNOWN");
            profile.setProjectType("UNKNOWN");
            profile.setAnalysisCompleteness("UNKNOWN");
            profile.getDetectionWarnings().add("Workspace directory is invalid or empty.");
            return profile;
        }

        // 1. Traverse directory tree & collect file metadata
        List<FileDescriptor> allDescriptors = new ArrayList<>();
        int totalDirectories = 0;
        int totalFiles = 0;
        int sourceFileCount = 0;
        int testFileCount = 0;
        int manifestFileCount = 0;
        int configFileCount = 0;
        int docFileCount = 0;

        Set<String> extensions = new HashSet<>();
        List<String> relativePaths = new ArrayList<>();
        Map<String, String> manifestContents = new HashMap<>(); // path -> content snippet
        Map<String, String> configContents = new HashMap<>();   // path -> content snippet
        List<String> sourceFilePaths = new ArrayList<>();

        try {
            List<Path> paths = new ArrayList<>();
            try (var stream = Files.walk(workspaceDir)) {
                stream.forEach(paths::add);
            }

            for (Path path : paths) {
                if (path.equals(workspaceDir)) continue;

                // Path traversal check
                Path relative = workspaceDir.relativize(path).normalize();
                if (relative.toString().startsWith("..")) {
                    continue; // skip escaped paths
                }

                String relPathStr = relative.toString().replace('\\', '/');
                boolean isDir = Files.isDirectory(path);
                String filename = path.getFileName().toString();
                String ext = getExtension(filename);

                if (isDir) {
                    totalDirectories++;
                } else {
                    totalFiles++;
                    extensions.add(ext.toLowerCase());
                    relativePaths.add(relPathStr);

                    long size = Files.size(path);
                    String fileType = determineFileType(relPathStr, filename, ext);

                    if ("SOURCE_CODE".equals(fileType)) {
                        sourceFileCount++;
                        sourceFilePaths.add(relPathStr);
                    } else if ("TEST_CODE".equals(fileType)) {
                        testFileCount++;
                    } else if ("MANIFEST".equals(fileType)) {
                        manifestFileCount++;
                        profile.getDetectedManifests().add(filename);
                        if (size < MAX_READ_FILE_SIZE) {
                            try {
                                String content = Files.readString(path, StandardCharsets.UTF_8);
                                manifestContents.put(relPathStr, content);
                            } catch (Exception e) {
                                warnings.add("Failed to read manifest file: " + relPathStr);
                            }
                        }
                    } else if ("CONFIGURATION".equals(fileType)) {
                        configFileCount++;
                        if (size < MAX_READ_FILE_SIZE) {
                            try {
                                String content = Files.readString(path, StandardCharsets.UTF_8);
                                configContents.put(relPathStr, content);
                            } catch (Exception e) {
                                // ignore configuration read errors
                            }
                        }
                    } else if ("DOCUMENTATION".equals(fileType)) {
                        docFileCount++;
                    }

                    allDescriptors.add(new FileDescriptor(relPathStr, filename, ext, size, false, fileType));
                }
            }
        } catch (Exception e) {
            warnings.add("Error scanning workspace directory structure: " + e.getMessage());
        }

        // Limit sample files stored in MongoDB
        List<FileDescriptor> sampleFiles = allDescriptors.stream().limit(50).toList();
        ProjectStructure structure = new ProjectStructure(
                totalFiles, totalDirectories, sourceFileCount, testFileCount,
                manifestFileCount, configFileCount, docFileCount, sampleFiles
        );
        profile.setProjectStructure(structure);

        if (totalFiles == 0) {
            profile.setPrimaryLanguage("UNKNOWN");
            profile.setProjectType("UNKNOWN");
            profile.setAnalysisCompleteness("UNKNOWN");
            warnings.add("Uploaded project contains no files.");
            profile.setDetectionWarnings(warnings);
            return profile;
        }

        // 2. Language Detection
        Set<String> languages = new LinkedHashSet<>();
        if (hasAnyExtension(extensions, ".java")) languages.add("JAVA");
        if (hasAnyExtension(extensions, ".ts", ".tsx")) languages.add("TYPESCRIPT");
        if (hasAnyExtension(extensions, ".js", ".jsx")) languages.add("JAVASCRIPT");
        if (hasAnyExtension(extensions, ".py")) languages.add("PYTHON");
        if (hasAnyExtension(extensions, ".go")) languages.add("GO");
        if (hasAnyExtension(extensions, ".cs")) languages.add("CSHARP");
        if (hasAnyExtension(extensions, ".php")) languages.add("PHP");

        // Primary Language Selection
        String primaryLanguage = "UNKNOWN";
        if (!languages.isEmpty()) {
            if (languages.contains("TYPESCRIPT")) primaryLanguage = "TYPESCRIPT";
            else if (languages.contains("JAVA")) primaryLanguage = "JAVA";
            else if (languages.contains("PYTHON")) primaryLanguage = "PYTHON";
            else if (languages.contains("GO")) primaryLanguage = "GO";
            else if (languages.contains("JAVASCRIPT")) primaryLanguage = "JAVASCRIPT";
            else if (languages.contains("CSHARP")) primaryLanguage = "CSHARP";
            else if (languages.contains("PHP")) primaryLanguage = "PHP";
            else primaryLanguage = languages.iterator().next();
        }
        profile.setPrimaryLanguage(primaryLanguage);
        profile.setLanguages(new ArrayList<>(languages));

        // 3. Build System Detection
        Set<String> buildSystems = new LinkedHashSet<>();
        Set<String> frameworks = new LinkedHashSet<>();
        Set<String> testFrameworks = new LinkedHashSet<>();
        Set<String> databases = new LinkedHashSet<>();
        String packageManager = null;

        // Static Check Java (Maven / Gradle)
        if (languages.contains("JAVA")) {
            if (manifestContents.containsKey("pom.xml") || hasFile(relativePaths, "pom.xml")) {
                buildSystems.add("MAVEN");
                evidences.add(new DetectionEvidence("MAVEN", "HIGH", List.of("pom.xml")));
            }
            if (hasFile(relativePaths, "build.gradle", "build.gradle.kts")) {
                buildSystems.add("GRADLE");
                evidences.add(new DetectionEvidence("GRADLE", "HIGH", List.of("build.gradle")));
            }

            // Spring Boot check
            String pomContent = manifestContents.getOrDefault("pom.xml", "");
            boolean isSpringBootPom = pomContent.contains("spring-boot");
            boolean hasSpringApp = containsKeywordInWorkspace(workspaceDir, sourceFilePaths, "SpringApplication", "@SpringBootApplication");
            boolean hasSpringConfig = hasFileEndingWith(relativePaths, "application.properties", "application.yml", "application.yaml");

            if (isSpringBootPom || hasSpringApp || hasSpringConfig) {
                frameworks.add("SPRING_BOOT");
                List<String> springEv = new ArrayList<>();
                if (isSpringBootPom) springEv.add("pom.xml (spring-boot dependency)");
                if (hasSpringApp) springEv.add("SpringApplication / @SpringBootApplication reference");
                if (hasSpringConfig) springEv.add("application properties/yaml configuration");
                evidences.add(new DetectionEvidence("SPRING_BOOT", "HIGH", springEv));
            }

            // Testing check (JUnit / Mockito)
            boolean hasJUnitPom = pomContent.contains("junit") || pomContent.contains("org.junit");
            boolean hasTestCode = containsKeywordInWorkspace(workspaceDir, sourceFilePaths, "@Test", "org.junit");
            if (hasJUnitPom || hasTestCode || testFileCount > 0) {
                testFrameworks.add("JUNIT");
                evidences.add(new DetectionEvidence("JUNIT", "HIGH", List.of("JUnit dependencies / @Test annotations")));
            }
            if (pomContent.contains("mockito") || containsKeywordInWorkspace(workspaceDir, sourceFilePaths, "org.mockito", "@Mock")) {
                testFrameworks.add("MOCKITO");
            }
        }

        // Static Check JS/TS (package.json / lock files)
        if (languages.contains("JAVASCRIPT") || languages.contains("TYPESCRIPT")) {
            String pkgContent = getManifestContent(manifestContents, "package.json");
            
            // Package manager check via lock file
            if (hasFile(relativePaths, "package-lock.json")) {
                packageManager = "npm";
            } else if (hasFile(relativePaths, "yarn.lock")) {
                packageManager = "yarn";
            } else if (hasFile(relativePaths, "pnpm-lock.yaml")) {
                packageManager = "pnpm";
            } else if (pkgContent != null) {
                packageManager = "npm";
            }
            if (packageManager != null) {
                buildSystems.add(packageManager.toUpperCase());
            }

            if (pkgContent != null) {
                // Safe inspection of package.json dependencies
                if (pkgContent.contains("\"next\"") || hasFileEndingWith(relativePaths, "next.config.js", "next.config.mjs", "next.config.ts")) {
                    frameworks.add("NEXT_JS");
                    evidences.add(new DetectionEvidence("NEXT_JS", "HIGH", List.of("package.json (next dependency)", "next.config")));
                }
                if (pkgContent.contains("\"react\"") || hasAnyExtension(extensions, ".jsx", ".tsx")) {
                    frameworks.add("REACT");
                    evidences.add(new DetectionEvidence("REACT", "HIGH", List.of("package.json (react dependency) / .jsx/.tsx files")));
                }
                if (pkgContent.contains("\"express\"")) {
                    frameworks.add("EXPRESS");
                    evidences.add(new DetectionEvidence("EXPRESS", "HIGH", List.of("package.json (express dependency)")));
                }
                if (pkgContent.contains("\"jest\"") || hasFileEndingWith(relativePaths, "jest.config.js", "jest.config.ts", "jest.config.cjs")) {
                    testFrameworks.add("JEST");
                }
                if (pkgContent.contains("\"vitest\"") || hasFileEndingWith(relativePaths, "vitest.config.ts", "vitest.config.js")) {
                    testFrameworks.add("VITEST");
                }
            } else {
                if (hasAnyExtension(extensions, ".jsx", ".tsx")) {
                    frameworks.add("REACT");
                }
            }
        }

        // Static Check Python
        if (languages.contains("PYTHON")) {
            String reqContent = getManifestContent(manifestContents, "requirements.txt");
            String pyprojContent = getManifestContent(manifestContents, "pyproject.toml");
            String combinedReq = (reqContent != null ? reqContent : "") + " " + (pyprojContent != null ? pyprojContent : "");

            if (hasFile(relativePaths, "requirements.txt")) buildSystems.add("PIP");
            if (hasFile(relativePaths, "pyproject.toml")) buildSystems.add("POETRY");
            if (hasFile(relativePaths, "Pipfile")) buildSystems.add("PIPENV");

            if (combinedReq.contains("django") || hasFile(relativePaths, "manage.py")) {
                frameworks.add("DJANGO");
                evidences.add(new DetectionEvidence("DJANGO", "HIGH", List.of("django dependency / manage.py")));
            }
            if (combinedReq.contains("flask")) {
                frameworks.add("FLASK");
                evidences.add(new DetectionEvidence("FLASK", "HIGH", List.of("flask dependency")));
            }
            if (combinedReq.contains("fastapi")) {
                frameworks.add("FASTAPI");
                evidences.add(new DetectionEvidence("FASTAPI", "HIGH", List.of("fastapi dependency")));
            }
            if (combinedReq.contains("pytest") || hasFileEndingWith(relativePaths, "pytest.ini")) {
                testFrameworks.add("PYTEST");
            }
            if (containsKeywordInWorkspace(workspaceDir, sourceFilePaths, "import unittest")) {
                testFrameworks.add("UNITTEST");
            }
        }

        // Static Check Go
        if (languages.contains("GO")) {
            if (hasFile(relativePaths, "go.mod")) {
                buildSystems.add("GO_MODULES");
                evidences.add(new DetectionEvidence("GO_MODULES", "HIGH", List.of("go.mod")));
            }
            if (hasFileEndingWith(relativePaths, "_test.go")) {
                testFrameworks.add("GOTEST");
                evidences.add(new DetectionEvidence("GOTEST", "HIGH", List.of("_test.go files")));
            }
        }

        // Static Check C#
        if (languages.contains("CSHARP")) {
            if (hasFileEndingWith(relativePaths, ".csproj", ".sln")) {
                buildSystems.add("MSBUILD");
            }
            if (hasFileEndingWith(relativePaths, "Program.cs", "Startup.cs")) {
                frameworks.add("ASPNET_CORE");
            }
            String csproj = getAnyManifestEndingWith(manifestContents, ".csproj");
            if (csproj != null) {
                if (csproj.contains("xunit")) testFrameworks.add("XUNIT");
                if (csproj.contains("nunit")) testFrameworks.add("NUNIT");
                if (csproj.contains("MSTest")) testFrameworks.add("MSTEST");
            }
        }

        // Static Check PHP
        if (languages.contains("PHP")) {
            String composer = getManifestContent(manifestContents, "composer.json");
            if (composer != null || hasFile(relativePaths, "composer.json")) {
                buildSystems.add("COMPOSER");
            }
            if ((composer != null && composer.contains("laravel/framework")) || hasFile(relativePaths, "artisan")) {
                frameworks.add("LARAVEL");
            }
            if (composer != null && composer.contains("symfony/")) {
                frameworks.add("SYMFONY");
            }
            if (hasFile(relativePaths, "phpunit.xml") || (composer != null && composer.contains("phpunit"))) {
                testFrameworks.add("PHPUNIT");
            }
        }

        // 4. Database Detection
        String allManifestText = String.join(" ", manifestContents.values()).toLowerCase();
        String allConfigText = String.join(" ", configContents.values()).toLowerCase();

        if (allManifestText.contains("mongodb") || allManifestText.contains("spring-data-mongodb") || allConfigText.contains("mongodb://")) {
            databases.add("MONGODB");
            evidences.add(new DetectionEvidence("MONGODB", "HIGH", List.of("mongodb dependency / connection string")));
        }
        if (allManifestText.contains("postgresql") || allManifestText.contains("psycopg") || allConfigText.contains("postgres://")) {
            databases.add("POSTGRESQL");
            evidences.add(new DetectionEvidence("POSTGRESQL", "HIGH", List.of("postgresql dependency / driver")));
        }
        if (allManifestText.contains("mysql") || allConfigText.contains("mysql://")) {
            databases.add("MYSQL");
            evidences.add(new DetectionEvidence("MYSQL", "HIGH", List.of("mysql dependency / driver")));
        }
        if (allManifestText.contains("sqlite") || allConfigText.contains("sqlite")) {
            databases.add("SQLITE");
            evidences.add(new DetectionEvidence("SQLITE", "MEDIUM", List.of("sqlite dependency / configuration")));
        }
        if (allManifestText.contains("redis") || allManifestText.contains("spring-data-redis") || allConfigText.contains("redis://")) {
            databases.add("REDIS");
            evidences.add(new DetectionEvidence("REDIS", "HIGH", List.of("redis dependency / driver")));
        }

        // Set profiles lists & primary values
        profile.setFrameworks(new ArrayList<>(frameworks));
        profile.setFramework(frameworks.isEmpty() ? "UNKNOWN" : frameworks.iterator().next());
        profile.setBuildSystem(buildSystems.isEmpty() ? "UNKNOWN" : buildSystems.iterator().next());
        profile.setTestFrameworks(new ArrayList<>(testFrameworks));
        profile.setDatabases(new ArrayList<>(databases));
        profile.setDatabase(databases.isEmpty() ? "NONE" : databases.iterator().next());
        profile.setPackageManager(packageManager);
        profile.setDetectionEvidences(evidences);

        // 5. Project Type Determination
        boolean isFrontend = frameworks.contains("NEXT_JS") || frameworks.contains("REACT");
        boolean isBackend = frameworks.contains("SPRING_BOOT") || frameworks.contains("EXPRESS")
                || frameworks.contains("DJANGO") || frameworks.contains("FLASK")
                || frameworks.contains("FASTAPI") || frameworks.contains("ASPNET_CORE")
                || frameworks.contains("LARAVEL") || languages.contains("GO");

        if (isFrontend && isBackend) {
            profile.setProjectType("FULL_STACK");
        } else if (isFrontend) {
            profile.setProjectType("FRONTEND");
        } else if (isBackend) {
            profile.setProjectType("BACKEND");
        } else {
            profile.setProjectType("UNKNOWN");
        }

        // 6. Analysis Completeness & Warning Handling
        boolean isSelectedContent = "SELECTED_CONTENT".equalsIgnoreCase(uploadMode);

        if (isSelectedContent) {
            profile.setAnalysisCompleteness("PARTIAL");
            warnings.add("Selected-content upload mode used. Uploaded archive may not contain all project files.");
            if (testFileCount == 0 && testFrameworks.isEmpty()) {
                warnings.add("Testing files were not found in uploaded content; because this is selected-content mode, their absence cannot be confirmed.");
            }
            if (isFrontend && !isBackend) {
                warnings.add("Backend components were not present in the uploaded content.");
            } else if (isBackend && !isFrontend) {
                warnings.add("Frontend components were not present in the uploaded content.");
            }
        } else {
            // COMPLETE_PROJECT mode
            if (testFileCount == 0 && testFrameworks.isEmpty() && sourceFileCount > 0) {
                profile.setAnalysisCompleteness("PARTIAL");
                warnings.add("No automated test files or testing framework configurations were detected in the uploaded project.");
            } else {
                profile.setAnalysisCompleteness("COMPLETE");
            }
        }

        profile.setDetectionWarnings(warnings);
        return profile;
    }

    private String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        return (idx >= 0 && idx < filename.length() - 1) ? filename.substring(idx) : "";
    }

    private boolean hasAnyExtension(Set<String> extensions, String... exts) {
        for (String ext : exts) {
            if (extensions.contains(ext.toLowerCase())) return true;
        }
        return false;
    }

    private boolean hasFile(List<String> paths, String... targetNames) {
        for (String p : paths) {
            String fname = new File(p).getName();
            for (String t : targetNames) {
                if (fname.equalsIgnoreCase(t)) return true;
            }
        }
        return false;
    }

    private boolean hasFileEndingWith(List<String> paths, String... suffixes) {
        for (String p : paths) {
            for (String s : suffixes) {
                if (p.toLowerCase().endsWith(s.toLowerCase())) return true;
            }
        }
        return false;
    }

    private String getManifestContent(Map<String, String> manifestContents, String filename) {
        for (Map.Entry<String, String> entry : manifestContents.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(filename) || entry.getKey().endsWith("/" + filename)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String getAnyManifestEndingWith(Map<String, String> manifestContents, String suffix) {
        for (Map.Entry<String, String> entry : manifestContents.entrySet()) {
            if (entry.getKey().toLowerCase().endsWith(suffix.toLowerCase())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String determineFileType(String relPath, String filename, String extension) {
        String lowerPath = relPath.toLowerCase();
        String lowerName = filename.toLowerCase();
        String lowerExt = extension.toLowerCase();

        if (lowerPath.contains("test") || lowerPath.contains("spec") || lowerName.endsWith("test.go") || lowerName.endsWith("spec.js") || lowerName.endsWith("test.js") || lowerName.endsWith("test.py")) {
            return "TEST_CODE";
        }
        if (Set.of("pom.xml", "package.json", "requirements.txt", "pyproject.toml", "build.gradle", "build.gradle.kts", "go.mod", "composer.json", "pipfile").contains(lowerName) || lowerExt.endsWith("csproj")) {
            return "MANIFEST";
        }
        if (lowerExt.equals(".java") || lowerExt.equals(".ts") || lowerExt.equals(".tsx") || lowerExt.equals(".js") || lowerExt.equals(".jsx") || lowerExt.equals(".py") || lowerExt.equals(".go") || lowerExt.equals(".cs") || lowerExt.equals(".php")) {
            return "SOURCE_CODE";
        }
        if (Set.of(".properties", ".yml", ".yaml", ".json", ".xml", ".ini", ".env", ".toml").contains(lowerExt)) {
            return "CONFIGURATION";
        }
        if (Set.of(".md", ".txt", ".rst", ".adoc", ".pdf").contains(lowerExt) || lowerName.startsWith("readme") || lowerName.startsWith("license")) {
            return "DOCUMENTATION";
        }
        if (Set.of(".exe", ".dll", ".so", ".dylib", ".class", ".jar", ".zip", ".tar", ".gz", ".png", ".jpg", ".ico").contains(lowerExt)) {
            return "BINARY";
        }
        return "OTHER";
    }

    private boolean containsKeywordInWorkspace(Path workspaceDir, List<String> relativePaths, String... keywords) {
        int checked = 0;
        for (String rel : relativePaths) {
            if (checked > 20) break; // Limit scanning to 20 files
            Path p = workspaceDir.resolve(rel);
            if (Files.exists(p) && !Files.isDirectory(p)) {
                try {
                    String text = Files.readString(p, StandardCharsets.UTF_8);
                    for (String kw : keywords) {
                        if (text.contains(kw)) return true;
                    }
                    checked++;
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }
}
