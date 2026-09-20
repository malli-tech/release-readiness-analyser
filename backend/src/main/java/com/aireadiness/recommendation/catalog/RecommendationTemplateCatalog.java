package com.aireadiness.recommendation.catalog;

import com.aireadiness.recommendation.model.RecommendationEffort;
import com.aireadiness.recommendation.model.RecommendationPriority;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RecommendationTemplateCatalog {

    private final Map<String, RecommendationTemplate> templateMap = new HashMap<>();

    public RecommendationTemplateCatalog() {
        initCatalog();
    }

    private void initCatalog() {
        // ==========================================
        // 1. CODE QUALITY RULES (10 Rules)
        // ==========================================
        register(
                "CODE_QUALITY_LONG_METHOD",
                "Refactor Long Method",
                "Method exceeds the maximum recommended statement length limit.",
                "Extract smaller helper methods to keep method length under target limit (<= 50 lines).",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.MEDIUM,
                "Long Method or Function"
        );

        register(
                "CODE_QUALITY_LARGE_CLASS",
                "Split Large Class",
                "Class size exceeds target maximum lines or field count limit.",
                "Decompose class into cohesive modules following Single Responsibility Principle.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.HIGH,
                "Large Class or File"
        );

        register(
                "CODE_QUALITY_TOO_MANY_PARAMETERS",
                "Reduce Parameter Count",
                "Method signature accepts too many positional parameters.",
                "Encapsulate parameters into a parameter object or DTO.",
                RecommendationPriority.LOW,
                RecommendationEffort.MEDIUM,
                "Too Many Parameters"
        );

        register(
                "CODE_QUALITY_DEEP_NESTING",
                "Reduce Control Flow Nesting Depth",
                "Control flow nesting depth exceeds recommended maximum depth.",
                "Use guard clauses, early returns, or helper functions to flatten nested control structures.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.MEDIUM,
                "Deep Control Flow Nesting"
        );

        register(
                "CODE_QUALITY_TODO_FIXME",
                "Address Technical Debt Markers",
                "Source code contains pending TODO/FIXME/XXX debt markers.",
                "Resolve or track open TODO/FIXME markers before release.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "TODO / FIXME / XXX Comment Marker"
        );

        register(
                "CODE_QUALITY_EMPTY_EXCEPTION_HANDLER",
                "Handle Caught Exceptions",
                "Exception catch block is empty and silently swallows errors.",
                "Log, rethrow, or handle caught exceptions instead of silently swallowing them.",
                RecommendationPriority.HIGH,
                RecommendationEffort.LOW,
                "Empty Exception Handler"
        );

        register(
                "CODE_QUALITY_MAGIC_NUMBER",
                "Extract Named Constants",
                "Numeric literal is hardcoded without descriptive constant definition.",
                "Replace numeric literals with descriptive constant variables.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Magic Number Literal"
        );

        register(
                "CODE_QUALITY_COMMENTED_OUT_CODE",
                "Remove Dead Code Comments",
                "Source file contains blocks of commented-out code.",
                "Remove commented-out code blocks; rely on Git version control history.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Commented-Out Code"
        );

        register(
                "CODE_QUALITY_DUPLICATED_CODE",
                "Deduplicate Repeated Code Blocks",
                "Identical code sequence appears across multiple code sections.",
                "Extract shared logic into utility methods or common parent components.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.MEDIUM,
                "Duplicated Code Sequence"
        );

        register(
                "CODE_QUALITY_POOR_NAMING",
                "Improve Variable Naming Clarity",
                "Variable name is too short or cryptically named.",
                "Use meaningful, self-explanatory variable and function identifiers.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Poor Variable Naming"
        );

        // ==========================================
        // 2. TESTING RULES (10 Rules)
        // ==========================================
        register(
                "TESTING_NO_TEST_FILES",
                "Add Automated Test Suite",
                "No test files were detected in the codebase.",
                "Create unit and integration test suites covering critical business paths.",
                RecommendationPriority.HIGH,
                RecommendationEffort.HIGH,
                "No Test Files Detected"
        );

        register(
                "TESTING_LOW_TEST_PRESENCE",
                "Increase Test Coverage",
                "Ratio of test files to source files is below minimum target.",
                "Add automated tests to reach acceptable test presence ratio.",
                RecommendationPriority.HIGH,
                RecommendationEffort.HIGH,
                "Low Test Presence Ratio"
        );

        register(
                "TESTING_UNTESTED_SOURCE_FILE",
                "Create Unit Tests for Source File",
                "Source file has no corresponding automated unit test class.",
                "Add corresponding unit test class for untested source file.",
                RecommendationPriority.LOW,
                RecommendationEffort.MEDIUM,
                "Untested Source File"
        );

        register(
                "TESTING_NO_ASSERTION",
                "Add Assertions to Test Method",
                "Test method executes without performing assertions.",
                "Ensure test method asserts expected outcomes or verifies mock interactions.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Test Method Has No Assertion"
        );

        register(
                "TESTING_EMPTY_TEST",
                "Implement or Remove Empty Test",
                "Test method body is empty or stubbed out.",
                "Add test logic and assertions to empty test methods or remove stub tests.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Empty Test Method"
        );

        register(
                "TESTING_SKIPPED_TEST",
                "Enable and Fix Skipped Test",
                "Test method is disabled or annotated with skip directive.",
                "Investigate and resolve reasons for disabled/skipped test annotations.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Skipped Test Method"
        );

        register(
                "TESTING_EXCESSIVE_SKIPPED_TESTS",
                "Address High Skipped Test Rate",
                "Too many test methods are currently skipped or disabled.",
                "Audit and reactivate excessive skipped test methods before deployment.",
                RecommendationPriority.HIGH,
                RecommendationEffort.MEDIUM,
                "Excessive Skipped Tests"
        );

        register(
                "TESTING_ORPHAN_TEST",
                "Link or Clean Up Orphan Test",
                "Test file does not match any existing source implementation file.",
                "Ensure test file targets active application source components.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Orphan Test File"
        );

        register(
                "TESTING_POOR_TEST_ORGANIZATION",
                "Reorganize Test Package Layout",
                "Test directory structure does not align with source package structure.",
                "Align test package layout with source codebase directory structure.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Poor Test Organization"
        );

        register(
                "TESTING_TODO_TEST",
                "Complete Pending Test Assertions",
                "Test code contains TODO or incomplete implementation markers.",
                "Finish stubbed or incomplete test assertions marked with TODO.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "TODO Marker in Test Code"
        );

        // ==========================================
        // 3. DEPENDENCY RULES (6 Rules)
        // ==========================================
        register(
                "DEPENDENCY_NO_MANIFEST",
                "Add Package Manifest File",
                "No standard dependency manifest file was found.",
                "Include standard dependency manifest (pom.xml, package.json, requirements.txt).",
                RecommendationPriority.HIGH,
                RecommendationEffort.LOW,
                "No Dependency Manifest Found"
        );

        register(
                "DEPENDENCY_UNPINNED_VERSION",
                "Pin Dependency Versions",
                "Dependency version is unpinned or uses floating version tags.",
                "Lock exact dependency versions to prevent non-deterministic builds.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Unpinned Dependency Version"
        );

        register(
                "DEPENDENCY_BROAD_VERSION_RANGE",
                "Restrict Broad Dependency Version Ranges",
                "Dependency uses broad version range specifiers.",
                "Replace wildcards or broad version ranges with specific semantic version constraints.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Broad Dependency Version Range"
        );

        register(
                "DEPENDENCY_DUPLICATE",
                "Remove Duplicate Dependency Entries",
                "Same dependency is declared multiple times in build manifest.",
                "Clean up duplicate dependency entries in manifest file.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Duplicate Dependency Declaration"
        );

        register(
                "DEPENDENCY_VERSION_INCONSISTENCY",
                "Align Inconsistent Dependency Versions",
                "Inconsistent versions of the same library are declared.",
                "Unify mismatched dependency versions across project submodules.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.MEDIUM,
                "Dependency Version Inconsistency"
        );

        register(
                "DEPENDENCY_MANIFEST_WARNING",
                "Resolve Dependency Manifest Warnings",
                "Dependency manifest file contains configuration or parsing warnings.",
                "Fix malformed syntax or configuration warnings in dependency manifests.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Dependency Manifest Warning"
        );

        // ==========================================
        // 4. SECURITY RULES (12 Rules)
        // ==========================================
        register(
                "SECURITY_HARDCODED_SECRET",
                "Remove Hardcoded Secrets and Private Keys",
                "Hardcoded password, secret key, API token, or private key detected.",
                "Extract secret keys, passwords, or tokens into environment variables or secret vaults.",
                RecommendationPriority.CRITICAL,
                RecommendationEffort.LOW,
                "Hardcoded Credentials / Private Keys"
        );

        register(
                "SECURITY_SQL_INJECTION_RISK",
                "Use Parameterized SQL Queries",
                "Raw string concatenation detected in SQL query construction.",
                "Replace string concatenation in database queries with PreparedStatement or ORM query parameters.",
                RecommendationPriority.HIGH,
                RecommendationEffort.MEDIUM,
                "Unsafe SQL Query Construction"
        );

        register(
                "SECURITY_COMMAND_INJECTION_RISK",
                "Prevent System Command Injection",
                "Untrusted input passed to OS command execution API.",
                "Avoid shell string execution; pass sanitized argument arrays directly to process builders.",
                RecommendationPriority.CRITICAL,
                RecommendationEffort.HIGH,
                "Unsafe Command String Construction"
        );

        register(
                "SECURITY_PATH_TRAVERSAL_RISK",
                "Sanitize File Path Access Inputs",
                "User input used in file path construction without canonicalization.",
                "Validate and normalize file paths; ensure paths remain within designated directories.",
                RecommendationPriority.HIGH,
                RecommendationEffort.MEDIUM,
                "Unsafe Path Traversal Construction"
        );

        register(
                "SECURITY_INSECURE_DESERIALIZATION",
                "Secure Object Deserialization",
                "Potentially unsafe native object deserialization detected.",
                "Use safe serialization formats (JSON) or enforce strict type whitelists for Java object deserialization.",
                RecommendationPriority.HIGH,
                RecommendationEffort.HIGH,
                "Insecure Object Deserialization"
        );

        register(
                "SECURITY_WEAK_CRYPTOGRAPHY",
                "Upgrade Weak Cryptographic Algorithms",
                "Weak or deprecated hash/cipher algorithm (MD5, SHA1, DES) used.",
                "Replace weak ciphers (MD5, SHA1, DES) with modern secure standards (AES-GCM, SHA-256).",
                RecommendationPriority.HIGH,
                RecommendationEffort.MEDIUM,
                "Weak Cryptographic Algorithm"
        );

        register(
                "SECURITY_TLS_VERIFICATION_DISABLED",
                "Enable SSL/TLS Certificate Verification",
                "SSL certificate or hostname verification is disabled.",
                "Re-enable SSL/TLS hostname and certificate verification for HTTPS connections.",
                RecommendationPriority.HIGH,
                RecommendationEffort.LOW,
                "Disabled TLS/SSL Certificate Verification"
        );

        register(
                "SECURITY_INSECURE_HTTP",
                "Enforce HTTPS Transport Protocol",
                "Unencrypted HTTP URL detected for external communication.",
                "Upgrade unencrypted http:// URLs to encrypted https:// connections.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Insecure HTTP Transport Protocol"
        );

        register(
                "SECURITY_DEBUG_ENABLED",
                "Disable Debug Mode for Production",
                "Debug mode or verbose debug endpoint is enabled.",
                "Ensure debug mode options are set to false for production deployments.",
                RecommendationPriority.HIGH,
                RecommendationEffort.LOW,
                "Insecure Debug Mode Configuration"
        );

        register(
                "SECURITY_PERMISSIVE_CORS",
                "Restrict CORS Origin Whitelist",
                "Cross-Origin Resource Sharing policy permits wildcard origin '*'.",
                "Replace wildcard '*' CORS origin configurations with specific trusted origin domains.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Permissive CORS Wildcard Policy"
        );

        register(
                "SECURITY_SENSITIVE_FILE_EXPOSED",
                "Remove Committed Sensitive Credentials File",
                "Private key, environment credentials, or secret file committed to workspace.",
                "Remove committed key files (.env, .pem, credentials) from source code and git history.",
                RecommendationPriority.CRITICAL,
                RecommendationEffort.LOW,
                "Committed Sensitive Credentials File"
        );

        register(
                "SECURITY_DANGEROUS_EXECUTION",
                "Audit Dynamic Code Execution APIs",
                "Dynamic code execution (eval, ScriptEngine) detected.",
                "Refactor dynamic code execution (eval, script engine) to safer typed alternatives.",
                RecommendationPriority.HIGH,
                RecommendationEffort.HIGH,
                "Dangerous Code or Command Execution API"
        );

        // ==========================================
        // 5. PERFORMANCE RULES (10 Rules)
        // ==========================================
        register(
                "PERFORMANCE_N_PLUS_ONE_QUERY",
                "Fix N+1 Query Pattern",
                "Potential N+1 relational query pattern detected.",
                "Use JOIN FETCH, batch fetching, or entity graphs to retrieve relational data efficiently.",
                RecommendationPriority.HIGH,
                RecommendationEffort.MEDIUM,
                "Potential N+1 Query Pattern"
        );

        register(
                "PERFORMANCE_DATABASE_CALL_IN_LOOP",
                "Batch Database Operations Outside Loop",
                "Database operation executed inside loop iteration.",
                "Move database queries outside loops; execute bulk operations or single set-based queries.",
                RecommendationPriority.HIGH,
                RecommendationEffort.MEDIUM,
                "Database Operation Inside Loop"
        );

        register(
                "PERFORMANCE_REPEATED_EXPENSIVE_OPERATION",
                "Hoist Expensive Invariant Loop Operations",
                "Expensive function call executed repeatedly inside loop.",
                "Move invariant expensive function calls outside loop iterations.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Repeated Expensive Operation in Loop"
        );

        register(
                "PERFORMANCE_BLOCKING_CALL_IN_ASYNC_CONTEXT",
                "Avoid Blocking I/O in Async Context",
                "Blocking thread call executed inside asynchronous or reactive looper context.",
                "Use non-blocking reactive operators or delegate blocking I/O to a dedicated thread pool.",
                RecommendationPriority.HIGH,
                RecommendationEffort.MEDIUM,
                "Blocking Operation in Asynchronous Context"
        );

        register(
                "PERFORMANCE_EXCESSIVE_NESTED_LOOPS",
                "Optimize Nested Loop Algorithmic Complexity",
                "Nested loop depth creates polynomial runtime complexity.",
                "Reduce algorithmic complexity (O(N^3) or higher) using lookup maps or indexes.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.HIGH,
                "Excessive Nested Loops"
        );

        register(
                "PERFORMANCE_REPEATED_COLLECTION_SCAN",
                "Use Hash Set/Map for Repeated Scans",
                "Linear list lookup executed repeatedly inside loop iteration.",
                "Convert List linear search inside loops to constant-time Set or Map lookups.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Repeated Linear Collection Scan in Loop"
        );

        register(
                "PERFORMANCE_LARGE_COLLECTION_ALLOCATION_IN_LOOP",
                "Pre-allocate or Reuse Collections Outside Loop",
                "New collection object instantiated inside loop iteration.",
                "Allocate collection instances outside loops or clear existing collections to reduce GC pressure.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Collection Allocation Inside Loop"
        );

        register(
                "PERFORMANCE_REPEATED_STRING_CONCATENATION",
                "Use StringBuilder for Loop String Concatenation",
                "String '+' concatenation performed inside loop body.",
                "Replace string '+' concatenation in loops with StringBuilder or StringJoiner.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Repeated String Concatenation in Loop"
        );

        register(
                "PERFORMANCE_REGEX_IN_LOOP",
                "Precompile Regex Pattern Constants",
                "Regex pattern compiled repeatedly inside loop iteration.",
                "Compile Pattern objects as static final constants outside loop execution paths.",
                RecommendationPriority.LOW,
                RecommendationEffort.LOW,
                "Regex Compilation Inside Loop"
        );

        register(
                "PERFORMANCE_SLEEP_OR_WAIT",
                "Remove Artificial Thread Sleep Delays",
                "Artificial Thread.sleep() or delay wait detected.",
                "Replace Thread.sleep() or delay loops with proper event-driven or lock condition synchronization.",
                RecommendationPriority.MEDIUM,
                RecommendationEffort.LOW,
                "Artificial Sleep or Wait Delay"
        );
    }

    private void register(
            String ruleId,
            String title,
            String summary,
            String recommendedAction,
            RecommendationPriority priority,
            RecommendationEffort effort,
            String... aliases
    ) {
        RecommendationTemplate template = new RecommendationTemplate(
                ruleId, title, summary, recommendedAction, priority, effort
        );
        templateMap.put(normalizeKey(ruleId), template);
        for (String alias : aliases) {
            if (alias != null && !alias.trim().isEmpty()) {
                templateMap.put(normalizeKey(alias), template);
            }
        }
    }

    private String normalizeKey(String key) {
        if (key == null) return "";
        return key.trim().toUpperCase().replace("-", "_").replace(" ", "_");
    }

    public Optional<RecommendationTemplate> findTemplate(String ruleId) {
        if (ruleId == null || ruleId.trim().isEmpty()) {
            return Optional.empty();
        }
        String key = normalizeKey(ruleId);
        RecommendationTemplate directMatch = templateMap.get(key);
        if (directMatch != null) {
            return Optional.of(directMatch);
        }

        // Secondary fallback search if ruleId contains partial match
        for (Map.Entry<String, RecommendationTemplate> entry : templateMap.entrySet()) {
            if (key.contains(entry.getKey()) || entry.getKey().contains(key)) {
                return Optional.of(entry.getValue());
            }
        }

        return Optional.empty();
    }

    public boolean supportsRule(String ruleId) {
        return findTemplate(ruleId).isPresent();
    }
}
