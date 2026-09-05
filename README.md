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

---

## Part 11 Capabilities: Static Security Analyzer

Part 11 implements a **100% static Security Analyzer** that evaluates source code and configuration files for 12 security weakness categories without executing project code, installing dependencies, or querying external vulnerability intelligence databases.

### Supported Weakness Categories & Rules
1. `SECURITY_HARDCODED_SECRET`: Detects likely hardcoded credentials, password assignments, API keys, access tokens, and PEM private key material (`CRITICAL` / `HIGH`). Automatically redacts all sensitive values as `[REDACTED SECRET]`.
2. `SECURITY_INSECURE_HTTP`: Detects plain HTTP transport URLs in source/configuration where HTTPS is expected (`MEDIUM`). Excludes `localhost` and `127.0.0.1`.
3. `SECURITY_TLS_VERIFICATION_DISABLED`: Detects disabled TLS certificate verification or trust-all SSL configurations (`HIGH`).
4. `SECURITY_DANGEROUS_EXECUTION`: Detects dynamic command execution APIs (`Runtime.exec`, `ProcessBuilder`, `eval`, `exec`, `os.system`, `child_process`, `shell_exec`) requiring security review (`HIGH`).
5. `SECURITY_SQL_INJECTION_RISK`: Detects unparameterized SQL query construction using string concatenation or unescaped string interpolation (`HIGH`).
6. `SECURITY_COMMAND_INJECTION_RISK`: Detects dynamic command string construction with variables prior to execution (`HIGH`).
7. `SECURITY_PATH_TRAVERSAL_RISK`: Detects unvalidated path construction with user parameters or `../` sequences (`HIGH`).
8. `SECURITY_INSECURE_DESERIALIZATION`: Detects dangerous deserialization patterns e.g. Java `ObjectInputStream.readObject()`, Python `pickle.loads()`, PHP `unserialize()` (`HIGH`).
9. `SECURITY_WEAK_CRYPTOGRAPHY`: Detects weak cryptographic algorithms (`MD5`, `SHA-1`, `DES`, `3DES`, `ECB` mode) (`MEDIUM` / `HIGH`).
10. `SECURITY_DEBUG_ENABLED`: Detects explicit debug mode or verbose error exposure enabled in production settings (`MEDIUM`).
11. `SECURITY_PERMISSIVE_CORS`: Detects wildcard CORS policies (`Access-Control-Allow-Origin: *`) (`MEDIUM`).
12. `SECURITY_SENSITIVE_FILE_EXPOSED` & `SECURITY_ENV_FILE_WITH_SECRET`: Detects committed sensitive files e.g. `.env` files with secret keys or private service account credential files (`HIGH`).

### Security Boundaries & Non-Execution Mandate
- **Zero Code Execution**: Uploaded code is strictly parsed statically as text/AST. No `Runtime.exec()`, `ProcessBuilder`, dynamic reflection, or script execution.
- **Zero Network Access / Registry Scans**: Does not connect to external vulnerability databases (CVE/NVD/OSV), package registries, or remote security APIs.
- **Strict Secret Redaction**: All secret values in findings, logs, evidence, and API responses are automatically replaced with `[REDACTED SECRET]`.
- **Pipeline Lifecycle State**: Analysis finishes as `COMPLETED`. Release status remains `READY_FOR_ANALYSIS` (readiness scoring is deferred to Part 13).

---

## Part 12 Capabilities: Static Performance Analyzer

Part 12 implements a **100% static, heuristic Performance Analyzer** that inspects source code for obvious performance-related code smells and inefficient programming patterns without executing project code, starting application servers, or running benchmarks.

### Key Principles & Scope
- **Static & Heuristic**: Performance findings are source-code-based static heuristics and recommendations. The analyzer does **not** claim to measure actual production response time, CPU utilization, memory consumption, or latency.
- **Zero Runtime Measurement**: Performs **no** benchmarking, profiling, load testing, application startup, test execution, database execution, network calls, or process instrumentation.
- **Zero Code Execution**: Uploaded application code is never started or executed in JVM, Node, Python, Go, .NET, or PHP runtimes.
- **Selected-Content Semantics**: For `SELECTED_CONTENT` uploads, completeness is marked as `PARTIAL` with warnings indicating that non-uploaded files may contain additional unanalyzed performance patterns.

### 10 Implemented Performance Rules
1. `PERFORMANCE_N_PLUS_ONE_QUERY`: Detects obvious N+1 database access patterns inside loops (`HIGH` / `MEDIUM`).
2. `PERFORMANCE_DATABASE_CALL_IN_LOOP`: Detects database/DAO/ORM query executions inside loops (`MEDIUM`).
3. `PERFORMANCE_BLOCKING_CALL_IN_ASYNC_CONTEXT`: Detects blocking calls (e.g. `.block()`, `Future.get()`) inside reactive or asynchronous contexts (`HIGH` / `MEDIUM`).
4. `PERFORMANCE_SLEEP_OR_WAIT`: Detects artificial thread/sleep delays (e.g. `Thread.sleep`, `time.sleep`) that block worker threads (`MEDIUM`).
5. `PERFORMANCE_REPEATED_STRING_CONCATENATION`: Detects inefficient repeated string concatenation inside loops (`LOW` / `MEDIUM`).
6. `PERFORMANCE_REGEX_IN_LOOP`: Detects repeated regex compilation (`Pattern.compile`, `re.compile`, `new RegExp`) inside loops (`MEDIUM`).
7. `PERFORMANCE_REPEATED_COLLECTION_SCAN`: Detects repeated linear collection scans (`list.contains`, `list.indexOf`, stream filter) inside loops (`LOW` / `MEDIUM`).
8. `PERFORMANCE_EXCESSIVE_NESTED_LOOPS`: Detects excessive loop nesting levels (3+ nested iteration levels) (`MEDIUM`).
9. `PERFORMANCE_LARGE_COLLECTION_ALLOCATION_IN_LOOP`: Detects repeated collection allocations (`new ArrayList`, `new HashMap`) inside loop bodies (`LOW` / `MEDIUM`).
10. `PERFORMANCE_REPEATED_EXPENSIVE_OPERATION`: Detects repeated expensive operations (e.g. `ObjectMapper` parsing/serialization) inside loops (`MEDIUM`).



