# AI Release Readiness Analyzer

An intelligent, multi-engine platform for evaluating candidate application releases, inspecting source code readiness, scanning security vulnerabilities, and calculating release readiness scores.

---

## Part 7 Capabilities: Project Detection & Analyzer Foundation

Uploaded project code is **never executed by the analyzer**. All detection and profile generation is **100% static** to ensure security when handling untrusted user uploads.

### Core Principles & Protections
- **No-Execution Principle**: Uploaded project code, shell scripts, batch files, npm scripts, Maven/Gradle commands, Python scripts, binaries, and test suites are **never executed**.
- **Static Manifest & File Inspection**: Reads file metadata, extension distributions, directory layouts, and text manifest files (`pom.xml`, `package.json`, `requirements.txt`, `pyproject.toml`, `build.gradle`, `go.mod`, `composer.json`, `.csproj`).
- **Path Traversal Protection**: Ensures workspace path resolution strictly remains contained within isolated sandbox directories.
- **Malformed Manifest Recovery**: Gracefully handles malformed XML/JSON/TOML manifests without crashing detection.

### Technology Detection Engine
- **Languages**: Java, TypeScript, JavaScript, Python, Go, C#, PHP.
- **Frameworks**: Spring Boot, Next.js, React, Express, Django, Flask, FastAPI, ASP.NET Core, Laravel, Symfony.
- **Build Systems & Package Managers**: Maven, Gradle, npm, yarn, pnpm, pip, poetry, pipenv, Go Modules, Composer, MSBuild.
- **Testing Frameworks**: JUnit, Mockito, Jest, Vitest, PyTest, unittest, Go test (`_test.go`), xUnit, NUnit, MSTest, PHPUnit.
- **Databases**: MongoDB, PostgreSQL, MySQL, SQLite, Redis.

### Analysis Completeness & Mode Handling
- **COMPLETE_PROJECT**: Evaluates project structure and alerts if expected components (e.g. tests) are absent.
- **SELECTED_CONTENT**: Marks completeness as `PARTIAL` and generates contextual warnings without making false non-presence claims about un-uploaded project modules.

### API Endpoints
- `POST /api/releases/{releaseId}/analysis`: Start static project detection & plan generation for a release.
- `GET /api/releases/{releaseId}/analysis`: Retrieve the latest analysis profile for a release.
- `GET /api/analyses/{analysisId}`: Retrieve a specific analysis record by ID.
