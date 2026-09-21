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

---

## Part 13 Capabilities: Unified Analysis Layer

Part 13 implements a **Unified Analysis Layer** that consolidates, deduplicates, normalizes, and summarizes static findings across all 5 specialized analyzers (`CODE_QUALITY`, `TESTING`, `DEPENDENCY`, `SECURITY`, `PERFORMANCE`) into a single structured `UnifiedAnalysisSummary`.

### Key Responsibilities & Architectural Scope
- **Finding Consolidation & Deduplication**: Aggregates findings from all executed analyzers while eliminating exact duplicates via deterministic composite keys (`analysisId:ruleId:filePath:lineNumber:evidenceHash`).
- **Category & Severity Aggregation**: Computes real-time category counts (`CODE_QUALITY`, `TESTING`, `DEPENDENCY`, `SECURITY`, `PERFORMANCE`) and severity counts (`HIGH`, `MEDIUM`, `LOW`, `INFO`) verifying `sum(severity counts) == totalFindings`.
- **Deterministic Ordering**: Sorts unified findings consistently by Severity weight -> Category -> File Path -> Line Number -> Rule ID.
- **Analyzer Execution Tracking**: Tracks status of each analyzer (`COMPLETED`, `FAILED`, `SKIPPED`) in `completedAnalyzers`, `failedAnalyzers`, and `skippedAnalyzers`.
- **Fault Tolerance**: If an individual analyzer encounters an error during execution, successful results from other analyzers survive, the failure is recorded in `failedAnalyzers`, and a warning is appended.
- **Preserved Summaries**: Retains specialized summaries (`testingSummary`, `dependencySummary`, `securitySummary`, `performanceSummary`) alongside the unified summary.
- **No Readiness Scoring / Risk Engine**: Does not compute release readiness scores, weighted category risk metrics, RAG retrieval, or LLM recommendations (deferred to Parts 14–19).
- **Zero Code Execution**: Operates purely on in-memory static finding domain models without executing code, invoking build tools, or performing network operations.

---

## Part 14 Capabilities: Risk Engine

Part 14 implements a **100% static, deterministic Risk Engine** (`risk-v1`) that converts the unified findings and summary from Part 13 into a transparent, weighted `RiskSummary` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, `UNKNOWN`).

### Key Principles & Risk Calculation Formula
- **Severity Base Weights**: `HIGH` = 10.00, `MEDIUM` = 5.00, `LOW` = 1.00, `INFO` = 0.00.
- **Category Multipliers**: `SECURITY` = 1.50x, `DEPENDENCY` = 1.25x, `PERFORMANCE` = 1.10x, `TESTING` = 1.00x, `CODE_QUALITY` = 1.00x.
- **Weighted Points Formula**: `weightedRiskPoints = sum( severityWeight(finding) * categoryMultiplier(category) )` computed with `BigDecimal` precision (`HALF_UP`, scale 2).
- **Thresholds**:
  - `LOW`: 0.00 – 9.99 weighted points
  - `MEDIUM`: 10.00 – 24.99 weighted points
  - `HIGH`: 25.00 – 49.99 weighted points
  - `CRITICAL`: 50.00+ weighted points
- **Critical Security Override**: If at least 1 `HIGH` severity `SECURITY` finding is present, the overall risk level is forced to at least `HIGH` (regardless of score).
- **Category Risk Breakdown**: Computes `CategoryRisk` breakdowns (`findingCount`, `highFindings`, `mediumFindings`, `weightedRiskPoints`, `riskLevel`) for all 5 categories.
- **Deterministic Risk Factors**: Generates explainable risk factor cards derived from actual data.
- **Completeness Semantics**:
  - `COMPLETE` with 0 findings -> `LOW` risk (0.00 pts).
  - `PARTIAL` -> Calculates risk from observed findings, with risk warning attached regarding unanalyzed files.
  - `UNKNOWN` (unsupported project or missing source) -> `UNKNOWN` overall risk level.
  - Failed analyzers -> Propagates warnings about incomplete analyzer coverage.
- **No Readiness Score / Release Gates**: Does not produce 0–100 readiness scores, release approval decisions, deployment gates, or AI/LLM recommendations (deferred to Parts 15–19).
- **Zero Code Execution & Network Operations**: Processes in-memory domain models without process invocations, file re-scanning, or network calls.

---

## Part 15 Capabilities: Readiness Score Layer

Part 15 implements a **100% static, deterministic Readiness Score Engine** (`readiness-v1`) that evaluates overall release readiness on a scale from `0.00` to `100.00` based on static analysis data and Part 14 weighted risk points.

### Key Principles & Formula
- **Core Formula**: `readinessScore = max(0.00, 100.00 - weightedRiskPoints)` computed with `BigDecimal` precision (`scale 2`, `RoundingMode.HALF_UP`). Clamped to minimum `0.00` if `weightedRiskPoints > 100.00`.
- **Primary Risk Signal**: Part 14 `weightedRiskPoints` serves as the primary negative signal. No arbitrary second penalties are applied for severity/category findings to prevent double-counting.
- **Readiness Level Classification**:
  - `EXCELLENT`: `90.00` – `100.00`
  - `GOOD`: `75.00` – `89.99`
  - `FAIR`: `50.00` – `74.99`
  - `POOR`: `25.00` – `49.99`
  - `NOT_READY`: `0.00` – `24.99`
  - `UNKNOWN`: Insufficient or unknown analysis coverage (`readinessScore = null`).
- **Confidence Model**:
  - `HIGH`: `COMPLETE` analysis coverage.
  - `MEDIUM`: `PARTIAL` upload coverage or partial analyzer failure.
  - `UNKNOWN`: Insufficient analysis coverage or unsupported ecosystem.
- **Completeness & Coverage Semantics**:
  - `COMPLETE`: Score calculated normally with `HIGH` confidence.
  - `PARTIAL`: Score calculated from observed findings with `MEDIUM` confidence and warning attached.
  - `UNKNOWN`: Score is `null`, level is `UNKNOWN`, confidence is `UNKNOWN`, warning attached.
- **Data-Derived Factors**: Generates deterministic `ReadinessFactor` cards based purely on empirical findings and summary data (e.g. `Strong Release Readiness`, `High Severity Findings Present`, `Elevated Release Risk`, `High-Security Findings Present`, `Testing Gaps Detected`, `Dependency Risk Detected`, `Performance Risk Detected`, `Partial Analysis Coverage`, `Unknown Analysis Coverage`).
- **Version Tracking**: Tagged with calculation version `readiness-v1`.
- **Zero AI / Zero Code Execution**: Operates 100% statically without LLM calls, RAG pipelines, external network services, or code execution.

---

## Part 16 Capabilities: RAG Knowledge Base Foundation

Part 16 implements a **trusted technical knowledge base foundation** for storing, structuring, normalizing, and chunking official documentation, release standards, security baselines, and architectural guidance in MongoDB.

### Key Capabilities & Architectural Scope
- **Trusted Domain Models**:
  - `KnowledgeDocument`: Stores document metadata (title, source, sourceType, category, technology, version, status, content, chunkCount).
  - `KnowledgeChunk`: Stores individual chunks derived from parent documents with full metadata inheritance.
- **Supported Sources & Categories**:
  - `SourceType`: `OFFICIAL_DOCUMENTATION`, `INTERNAL_STANDARD`, `SECURITY_POLICY`, `BEST_PRACTICES_GUIDE`, `RELEASE_CHECKLIST`.
  - `KnowledgeCategory`: `SECURITY_BEST_PRACTICES`, `PERFORMANCE_OPTIMIZATION`, `CODE_QUALITY_STANDARDS`, `TESTING_STRATEGIES`, `DEPENDENCY_MANAGEMENT`, `RELEASE_COMPLIANCE`.
- **Text Normalization (`KnowledgeTextNormalizer`)**:
  - Strips zero-width characters, standardizes UTF-8 spaces, normalizes Windows (`\r\n`) and Mac (`\r`) line breaks to Unix (`\n`), trims trailing whitespace per line, and collapses excessive blank lines (max 2 consecutive newlines).
- **Paragraph-Aware Chunking (`KnowledgeChunker`)**:
  - Deterministically splits documents into searchable text chunks targeting ~1,800 characters (max 2,500 chars) with configurable ~200 character overlap.
  - Respects paragraph boundaries (`\n\n`) and word boundaries without cutting words in half.
  - Assigns 0-indexed chunk indices (`chunkIndex: 0, 1, 2...`) and preserves parent document metadata in every chunk.
- **Atomic Ingestion & Persistence (`KnowledgeIngestionService`)**:
  - Validates document inputs, normalizes content, saves document to generate ID, chunks text, and persists chunks.
  - Includes atomic rollback: if chunking or chunk persistence fails, created chunks and saved document records are deleted to guarantee data consistency.
- **REST Endpoints (`KnowledgeController`)**:
  - `POST /api/knowledge/documents`: Ingests a new document, normalizes content, chunks, and persists. Returns `201 Created`.
  - `GET /api/knowledge/documents`: Lists active knowledge documents. Returns `200 OK`.
  - `GET /api/knowledge/documents/{id}`: Retrieves a specific document by ID. Returns `200 OK` or `404 Not Found`.
  - `GET /api/knowledge/documents/{id}/chunks`: Retrieves chunks for a document ordered by `chunkIndex`. Returns `200 OK`.
- **Zero Process Execution**: Operates without dynamic reflection, script execution, or process invocation.

---

## Part 17 Capabilities: RAG Retrieval

Part 17 implements **semantic retrieval** over the trusted knowledge base foundation built in Part 16, utilizing vector embeddings and local MongoDB vector search without generating AI/LLM text responses.

### Key Capabilities & Architectural Scope
- **Embedding Abstraction (`EmbeddingService` & `OpenAIEmbeddingService`)**:
  - Encapsulates vector embedding generation behind the `EmbeddingService` interface.
  - Implements `OpenAIEmbeddingService` using Spring's `RestClient` targeting OpenAI's embeddings API (`text-embedding-3-small`, default 1536 dimensions).
  - Securely configured via `OPENAI_API_KEY` environment variable. Never logs, prints, or exposes the API key in responses, logs, or repository files.
  - Throws explicit `IllegalStateException` if `OPENAI_API_KEY` is absent when embedding generation is invoked.
- **Vector Storage on Knowledge Chunks**:
  - Extends `KnowledgeChunk` domain entity to store vector embeddings (`List<Double> embedding`).
  - Preserves all document metadata inheritance (`documentId`, `chunkIndex`, `title`, `source`, `category`, `technology`, `version`).
- **Embedding During Ingestion & Re-Embedding**:
  - Document ingestion pipeline: `Document -> Normalize -> Chunk -> Embed Batch -> Persist Chunks`.
  - Atomic rollback: if vector embedding generation or persistence fails, created chunks and saved document records are cleaned up.
  - Re-embedding support: `reEmbedDocument(documentId)` re-generates and updates embeddings for existing active documents.
- **MongoDB Vector Search & Cosine Similarity (`KnowledgeRetrievalService`)**:
  - Connects to local MongoDB 8.2 (`knowledge_chunks` collection, `embedding` field, 1536 dimensions).
  - Computes exact Cosine Similarity ($\frac{\mathbf{u} \cdot \mathbf{v}}{\|\mathbf{u}\| \|\mathbf{v}\|}$) between query vector and candidate chunk vectors.
  - Higher similarity score indicates greater semantic alignment according to the vector model.
  - Supports pre-filtering by `category`, `technology` (case-insensitive regex match), and `version`.
- **Top-K Retrieval & Deterministic Sorting**:
  - Configurable `topK` parameter (min 1, max 20, default 5).
  - Deterministically sorts results: `similarityScore` DESC -> `documentId` ASC -> `chunkIndex` ASC.
- **Deterministic Query Builder (`RetrievalQueryBuilder`)**:
  - Formats retrieval query strings deterministically from finding attributes (`category`, `ruleId`, `severity`, `technology`, `filePath`).
  - Contains zero LLM/AI prompt generation logic.
- **REST API (`KnowledgeController`)**:
  - `POST /api/knowledge/retrieve`: Accepts `query`, optional `category`, `technology`, `version`, `topK`. Returns `200 OK` with top-K `KnowledgeRetrievalResult` items and similarity scores.
- **Security & Non-Execution Boundaries**:
  - **Zero LLM / Zero Chat Completions in Part 17**: Part 17 strictly retrieves knowledge chunks and does **not** call chat/completion models or generate AI explanations (handed over to Part 18).
  - **Zero Source Code Transmission**: Uploaded user project code is **never sent to OpenAI** or external APIs.
  - **Zero Process Execution**: No `Runtime.exec()`, `ProcessBuilder`, shell scripts, or dynamic code execution.

---

## Part 18 Capabilities: LLM / AI Review Layer

Part 18 implements an **explanatory LLM / AI Review layer** that generates structured educational risk explanations, code review guidance, and remediation pointers for static analyzer findings using OpenAI's Chat Completions API (`gpt-4o-mini`) and Part 17 RAG technical knowledge retrieval.

### Key Capabilities & Architectural Scope
- **LLM Abstraction (`LLMService` & `OpenAILLMService`)**:
  - Service interface `LLMService` generating structured `AIReviewResult` objects.
  - Implementation `OpenAILLMService` targeting OpenAI's Chat Completions API (`https://api.openai.com/v1/chat/completions`) using JSON mode (`response_format: {"type": "json_object"}`).
  - Configurable via `OPENAI_API_KEY` (`openai.api-key`), `openai.llm.model` (`gpt-4o-mini`), `openai.llm.endpoint`, `openai.llm.timeout-ms` (`15000`), and `openai.llm.max-retries` (`2`).
- **Prompt Builder & Injection Protections (`AIReviewPromptBuilder`)**:
  - Assembles structured prompts with XML-like section delimiters (`<PROJECT_CONTEXT>`, `<FINDING_DATA>`, `<EVIDENCE>`, `<RETRIEVED_KNOWLEDGE>`, `<TASK>`).
  - Implements **Prompt Injection Defense**: system instructions mandate treating uploaded source code, comments, and evidence strings strictly as passive untrusted data, ignoring any embedded instructions or prompt overrides.
- **RAG Technical Knowledge Integration**:
  - Reuses Part 17 `KnowledgeRetrievalService` and `RetrievalQueryBuilder` to fetch top-3 relevant knowledge base chunks for each finding.
- **Status Isolation & Fault Tolerance**:
  - If OpenAI API key is unconfigured, or calls fail, timeout, or return malformed JSON, the `AIReview` status is saved as `FAILED` with a safe error message.
  - **The underlying `Analysis`, static findings, `RiskSummary`, and `ReadinessScore` remain 100% valid and completed** without being affected by AI review failures.
- **Strict Non-Authoritative Boundary**:
  - The LLM is strictly an explanatory layer. It cannot alter finding severity, rule IDs, risk scores, or readiness scores, nor can it claim source code was executed.
- **REST Endpoints (`AIReviewController`)**:
  - `POST /api/analyses/{analysisId}/ai-reviews`: Triggers AI reviews for findings in an analysis.
  - `POST /api/analyses/{analysisId}/findings/{findingId}/ai-review`: Triggers AI review for a single finding.
  - `GET /api/analyses/{analysisId}/ai-reviews`: Lists AI reviews for an analysis.
  - `GET /api/analyses/{analysisId}/findings/{findingId}/ai-review`: Gets AI review for a finding.

---

## Part 19 Capabilities: Recommendations Foundation & Mapping Layer

Part 19 implements the **foundational Recommendation domain model and 100% deterministic recommendation mapping engine** for the AI Release Readiness Analyzer.

### Architecture & Conceptual Separation of Roles

The platform enforces a strict conceptual distinction between core analysis components:

- **Analyzer Finding**: *"What was observed"* — Fixed, authoritative static facts detected by static analyzers (`CODE_QUALITY`, `TESTING`, `DEPENDENCY`, `SECURITY`, `PERFORMANCE`).
- **Recommendation**: *"What the developer can do about it"* — Actionable remediation guidance, priority, and implementation effort mapping derived deterministically from findings.
- **Risk**: *"How much release risk the findings represent"* — Weighted Category Risk breakdowns and overall `RiskSummary` (`LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, `UNKNOWN`).
- **Readiness**: *"Overall release-readiness assessment"* — Deterministic 0.00–100.00 readiness score and release gating evaluation (`ReadinessScore`).

> [!IMPORTANT]
> Recommendations DO NOT alter, re-calculate, or override static analyzer findings, finding severities, risk points, or readiness scores.

### Key Capabilities & Architectural Scope
- **Recommendation Domain Model (`Recommendation`)**:
  - Persisted in MongoDB `recommendations` collection with compound index `{ analysisId: 1, findingId: 1 }`.
  - Holds `analysisId`, `findingId`, `ruleId`, `category`, `severity`, `title`, `summary`, `recommendedAction`, `priority`, `effort`, `status`, `filePath`, `lineNumber`, `createdAt`, `updatedAt`.
- **Deterministic Enums**:
  - `RecommendationPriority`: `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`. Deterministically derived from finding severity and category (e.g., `CRITICAL` severity or `HIGH` security findings resolve to `CRITICAL` priority).
  - `RecommendationEffort`: `LOW`, `MEDIUM`, `HIGH`. Estimated remediation effort classification based on rule complexity.
  - `RecommendationStatus`: `OPEN`, `IN_PROGRESS`, `RESOLVED`, `IGNORED`.
- **Comprehensive Template Catalog (`RecommendationTemplateCatalog`)**:
  - Provides deterministic remediation guidance, summary, action, priority, and effort for **all 46 static analyzer rules** across 5 categories:
    - Code Quality (10 rules)
    - Testing (10 rules)
    - Dependency (6 rules)
    - Security (12 rules)
    - Performance (10 rules)
  - Supports rule lookups by canonical rule ID as well as human-readable rule title aliases.
- **Deterministic Service Mapping (`RecommendationService`)**:
  - Generates actionable `Recommendation` lists from `Analysis` findings without external network calls, LLM calls, RAG pipelines, or code execution.
  - Guarantees 1-to-1 traceability from each recommendation back to its source `findingId` and `analysisId`.
  - Handles zero findings (returns empty list), duplicate findings (deduplicates by `findingId`), and unsupported rules (appends diagnostic warning without failing analysis).
  - Sorts recommendations deterministically by `Priority` -> `Category` -> `Rule ID` -> `Finding ID`.

---

## Deployment Architecture

The AI Release Readiness Analyzer is structured for cloud deployment:

```
GitHub Repository
   │
   ├─► Render Frontend (Next.js 15 App Router)
   │      │
   │      ▼ (HTTPS / REST API)
   │
   └─► Render Backend (Spring Boot 3.4 Java Web Service)
          │
          ├─► MongoDB Atlas (Production Document Database)
          │
          └─► OpenAI API (LLM Reviews & Embeddings)
```

For step-by-step setup guides and environment configuration:
- [Deployment Overview](deployment/README.md)
- [Render Backend Setup](deployment/render-backend.md)
- [Render Frontend Setup](deployment/render-frontend.md)
- [MongoDB Atlas Setup](deployment/mongodb-atlas.md)