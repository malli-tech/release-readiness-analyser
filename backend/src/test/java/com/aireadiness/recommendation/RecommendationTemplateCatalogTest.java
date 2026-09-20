package com.aireadiness.recommendation;

import com.aireadiness.recommendation.catalog.RecommendationTemplate;
import com.aireadiness.recommendation.catalog.RecommendationTemplateCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RecommendationTemplateCatalogTest {

    private RecommendationTemplateCatalog catalog;

    @BeforeEach
    public void setUp() {
        catalog = new RecommendationTemplateCatalog();
    }

    @Test
    @DisplayName("1. Maps all 46 static analyzer rules across 5 categories to deterministic templates")
    public void testAllAnalyzerRulesCovered() {
        List<String> allRuleIds = List.of(
                // Code Quality (10)
                "CODE_QUALITY_LONG_METHOD",
                "CODE_QUALITY_LARGE_CLASS",
                "CODE_QUALITY_TOO_MANY_PARAMETERS",
                "CODE_QUALITY_DEEP_NESTING",
                "CODE_QUALITY_TODO_FIXME",
                "CODE_QUALITY_EMPTY_EXCEPTION_HANDLER",
                "CODE_QUALITY_MAGIC_NUMBER",
                "CODE_QUALITY_COMMENTED_OUT_CODE",
                "CODE_QUALITY_DUPLICATED_CODE",
                "CODE_QUALITY_POOR_NAMING",
                // Testing (10)
                "TESTING_NO_TEST_FILES",
                "TESTING_LOW_TEST_PRESENCE",
                "TESTING_UNTESTED_SOURCE_FILE",
                "TESTING_NO_ASSERTION",
                "TESTING_EMPTY_TEST",
                "TESTING_SKIPPED_TEST",
                "TESTING_EXCESSIVE_SKIPPED_TESTS",
                "TESTING_ORPHAN_TEST",
                "TESTING_POOR_TEST_ORGANIZATION",
                "TESTING_TODO_TEST",
                // Dependency (6)
                "DEPENDENCY_NO_MANIFEST",
                "DEPENDENCY_UNPINNED_VERSION",
                "DEPENDENCY_BROAD_VERSION_RANGE",
                "DEPENDENCY_DUPLICATE",
                "DEPENDENCY_VERSION_INCONSISTENCY",
                "DEPENDENCY_MANIFEST_WARNING",
                // Security (12)
                "SECURITY_HARDCODED_SECRET",
                "SECURITY_SQL_INJECTION_RISK",
                "SECURITY_COMMAND_INJECTION_RISK",
                "SECURITY_PATH_TRAVERSAL_RISK",
                "SECURITY_INSECURE_DESERIALIZATION",
                "SECURITY_WEAK_CRYPTOGRAPHY",
                "SECURITY_TLS_VERIFICATION_DISABLED",
                "SECURITY_INSECURE_HTTP",
                "SECURITY_DEBUG_ENABLED",
                "SECURITY_PERMISSIVE_CORS",
                "SECURITY_SENSITIVE_FILE_EXPOSED",
                "SECURITY_DANGEROUS_EXECUTION",
                // Performance (10)
                "PERFORMANCE_N_PLUS_ONE_QUERY",
                "PERFORMANCE_DATABASE_CALL_IN_LOOP",
                "PERFORMANCE_REPEATED_EXPENSIVE_OPERATION",
                "PERFORMANCE_BLOCKING_CALL_IN_ASYNC_CONTEXT",
                "PERFORMANCE_EXCESSIVE_NESTED_LOOPS",
                "PERFORMANCE_REPEATED_COLLECTION_SCAN",
                "PERFORMANCE_LARGE_COLLECTION_ALLOCATION_IN_LOOP",
                "PERFORMANCE_REPEATED_STRING_CONCATENATION",
                "PERFORMANCE_REGEX_IN_LOOP",
                "PERFORMANCE_SLEEP_OR_WAIT"
        );

        assertEquals(48, allRuleIds.size(), "Should verify exactly 48 static analyzer rules");

        for (String ruleId : allRuleIds) {
            Optional<RecommendationTemplate> templateOpt = catalog.findTemplate(ruleId);
            assertTrue(templateOpt.isPresent(), "Catalog should contain template for ruleId: " + ruleId);

            RecommendationTemplate template = templateOpt.get();
            assertNotNull(template.getTitle(), "Title should not be null for rule: " + ruleId);
            assertFalse(template.getTitle().trim().isEmpty(), "Title should not be empty for rule: " + ruleId);
            assertNotNull(template.getSummary(), "Summary should not be null for rule: " + ruleId);
            assertFalse(template.getSummary().trim().isEmpty(), "Summary should not be empty for rule: " + ruleId);
            assertNotNull(template.getRecommendedAction(), "Recommended action should not be null for rule: " + ruleId);
            assertFalse(template.getRecommendedAction().trim().isEmpty(), "Recommended action should not be empty for rule: " + ruleId);
            assertNotNull(template.getDefaultPriority(), "Default priority should not be null for rule: " + ruleId);
            assertNotNull(template.getDefaultEffort(), "Default effort should not be null for rule: " + ruleId);
        }
    }

    @Test
    @DisplayName("2. Resolves human-readable rule title aliases deterministically")
    public void testRuleTitleAliases() {
        Optional<RecommendationTemplate> sqlOpt = catalog.findTemplate("Unsafe SQL Query Construction");
        assertTrue(sqlOpt.isPresent());
        assertEquals("SECURITY_SQL_INJECTION_RISK", sqlOpt.get().getRuleId());

        Optional<RecommendationTemplate> secretsOpt = catalog.findTemplate("Hardcoded Credentials / Private Keys");
        assertTrue(secretsOpt.isPresent());
        assertEquals("SECURITY_HARDCODED_SECRET", secretsOpt.get().getRuleId());
    }

    @Test
    @DisplayName("3. Returns empty Optional for unknown or unsupported rule IDs")
    public void testUnknownRuleReturnsEmpty() {
        assertFalse(catalog.findTemplate("UNKNOWN_RULE_ID_XYZ").isPresent());
        assertFalse(catalog.findTemplate(null).isPresent());
        assertFalse(catalog.findTemplate("").isPresent());
    }
}
