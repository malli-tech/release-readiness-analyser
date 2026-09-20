package com.aireadiness.knowledge;

import com.aireadiness.knowledge.model.KnowledgeCategory;
import com.aireadiness.knowledge.util.RetrievalQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RetrievalQueryBuilderTest {

    private RetrievalQueryBuilder queryBuilder;

    @BeforeEach
    public void setUp() {
        queryBuilder = new RetrievalQueryBuilder();
    }

    @Test
    @DisplayName("1. Formats full finding metadata into clean query string")
    public void testBuildQueryFullMetadata() {
        String query = queryBuilder.buildQuery(
                KnowledgeCategory.SECURITY,
                "SECURITY_SQL_INJECTION",
                "HIGH",
                "Java / Spring Boot",
                "src/main/java/com/example/UserRepository.java"
        );

        assertEquals("SECURITY SECURITY SQL INJECTION HIGH Java / Spring Boot UserRepository.java", query);
    }

    @Test
    @DisplayName("2. Handles missing optional fields gracefully")
    public void testBuildQueryPartialMetadata() {
        String query = queryBuilder.buildQuery(
                KnowledgeCategory.PERFORMANCE,
                "PERFORMANCE_N_PLUS_ONE_QUERY",
                null,
                null,
                null
        );

        assertEquals("PERFORMANCE PERFORMANCE N PLUS ONE QUERY", query);
    }

    @Test
    @DisplayName("3. Returns default fallback when all parameters are null")
    public void testBuildQueryAllNull() {
        String query = queryBuilder.buildQuery(null, null, null, null, null);
        assertNotNull(query);
        assertFalse(query.trim().isEmpty());
    }

    @Test
    @DisplayName("4. AI-review query includes finding title & description and excludes file path")
    public void testAIReviewQueryConstruction() {
        String query = queryBuilder.buildQuery(
                KnowledgeCategory.SECURITY,
                "SECURITY_SQL_INJECTION",
                "HIGH",
                "SQL Injection Vulnerability",
                "String concatenation in query creation",
                "Java",
                "Spring Boot"
        );

        assertTrue(query.contains("SQL Injection Vulnerability"));
        assertTrue(query.contains("String concatenation in query creation"));
        assertTrue(query.contains("Java"));
        assertTrue(query.contains("Spring Boot"));
        assertFalse(query.contains("UserRepository.java"));
        assertFalse(query.contains("sk-"));
        assertFalse(query.contains("OPENAI_API_KEY"));
    }
}
