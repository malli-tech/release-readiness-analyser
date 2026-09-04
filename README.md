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

---

## Part 8 Capabilities: Static Code Quality Analyzer

Part 8 implements a **100% static, rule-based Code Quality Analyzer** that evaluates source code maintainability, complexity, and code smells without executing uploaded code.

### 10 Implemented Quality Rules
1. `CODE_QUALITY_LONG_METHOD`: Detects methods/functions > 100 non-blank lines (`MEDIUM`).
2. `CODE_QUALITY_LARGE_CLASS`: Detects classes/types > 500 lines (`MEDIUM`).
3. `CODE_QUALITY_TOO_MANY_PARAMETERS`: Detects functions with > 6 parameters (`MEDIUM`).
4. `CODE_QUALITY_DEEP_NESTING`: Detects control flow nesting > 4 levels (`MEDIUM`).
5. `CODE_QUALITY_TODO_FIXME`: Detects explicit TODO/FIXME/XXX markers (`LOW`).
6. `CODE_QUALITY_EMPTY_EXCEPTION_HANDLER`: Detects empty catch/except blocks (`HIGH`).
7. `CODE_QUALITY_MAGIC_NUMBER`: Detects suspicious hardcoded numeric literals (`LOW`).
8. `CODE_QUALITY_COMMENTED_OUT_CODE`: Detects blocks of commented-out source code (`LOW`).
9. `CODE_QUALITY_DUPLICATED_CODE`: Detects repeated sequences of ≥ 6 lines (`MEDIUM`).
10. `CODE_QUALITY_POOR_NAMING`: Detects poor single-letter variable names outside loop/coordinate contexts (`LOW`).

### Safety, Limits & Protections
- **Multi-Language Support**: Java, JavaScript, TypeScript, Python, Go, C#, PHP.
- **Configurable Resource Limits**: Enforces 2 MB file limit, 50 MB total source content limit, 5,000 max source files limit, 2,000 max total findings limit. Over-limit files are skipped with warnings.
- **Secret Redaction**: Redacts accidental passwords/tokens/credentials in evidence snippets (`***REDACTED***`).
- **Finding Deduplication**: Unique identity mapping (`analysisId:ruleId:filePath:lineNumber`).
- **Selected-Content Semantics**: Preserves `SELECTED_CONTENT` notice without false non-presence claims for missing files.

### API Endpoints
- `POST /api/releases/{releaseId}/analysis`: Performs static project detection and runs Code Quality analysis. Returns `201 Created` with populated `findings[]`.
- `GET /api/releases/{releaseId}/analysis`: Retrieves the latest analysis and findings for a release.
- `GET /api/analyses/{analysisId}`: Retrieves a specific analysis record by ID.

---

## Part 10 Capabilities: Static Dependency Analyzer

Part 10 implements a **100% static Dependency Analyzer** evaluating manifest declarations and dependency-management quality across 9 developer ecosystems without executing project code, invoking package managers, or accessing external vulnerability databases.

### Supported Ecosystems & Manifests
- **Java / Maven**: `pom.xml` (DOM XML parsing, XXE protection, static property resolution)
- **Java / Gradle**: `build.gradle`, `build.gradle.kts`, `settings.gradle`
- **JavaScript / TypeScript / npm / Yarn / pnpm**: `package.json`, `package-lock.json`, `yarn.lock`, `pnpm-lock.yaml`
- **Python**: `requirements.txt`, `requirements-dev.txt`, `pyproject.toml`, `Pipfile`, `poetry.lock`
- **Go**: `go.mod`, `go.sum`
- **C# / .NET**: `*.csproj`, `packages.config`, `Directory.Packages.props`
- **PHP / Composer**: `composer.json`, `composer.lock`

### Implemented Dependency Rules
1. `DEPENDENCY_NO_MANIFEST`: Detects projects with source code but missing dependency manifests (`MEDIUM`, `COMPLETE_PROJECT` mode).
2. `DEPENDENCY_UNPINNED_VERSION`: Detects unpinned dependencies without explicit version constraints (`MEDIUM`).
3. `DEPENDENCY_BROAD_VERSION_RANGE`: Detects overly broad version declarations e.g. `*`, `latest`, `>=1.0.0` (`LOW`).
4. `DEPENDENCY_DUPLICATE`: Detects duplicate dependency declarations in the same manifest and scope (`MEDIUM`).
5. `DEPENDENCY_VERSION_INCONSISTENCY`: Detects inconsistent versions declared across multiple module manifests (`MEDIUM`).
6. `DEPENDENCY_MANIFEST_WARNING`: Summary-level warning for malformed or partially parsed manifests (`LOW`).

### Absolute Security & Vulnerability Boundary
- **Zero Process Execution**: No invocation of `mvn`, `gradle`, `npm`, `yarn`, `pnpm`, `pip`, `poetry`, `go`, `dotnet`, or `composer`. No `ProcessBuilder` or `Runtime.exec()`.
- **Zero Registry / Network Access**: Does not contact npm registry, Maven Central, PyPI, Go proxies, NuGet, or Packagist.
- **Zero CVE Vulnerability Queries**: Does not query OSV, NVD, Snyk, Sonatype, or OWASP. Measures static dependency management quality, not CVE security status.

