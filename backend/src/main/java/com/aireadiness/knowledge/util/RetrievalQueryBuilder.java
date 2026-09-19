package com.aireadiness.knowledge.util;

import com.aireadiness.knowledge.model.KnowledgeCategory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RetrievalQueryBuilder {

    /**
     * Deterministically builds a semantic retrieval query string from finding metadata.
     *
     * @param category Finding category enum or name
     * @param ruleId Rule identifier (e.g., SECURITY_SQL_INJECTION)
     * @param severity Finding severity (HIGH, MEDIUM, LOW)
     * @param technology Target framework/technology (e.g., Spring Boot, Java 17)
     * @param filePath Source file path where finding occurred
     * @return Deterministic query string for semantic vector retrieval.
     */
    public String buildQuery(KnowledgeCategory category, String ruleId, String severity, String technology, String filePath) {
        List<String> parts = new ArrayList<>();

        if (category != null) {
            parts.add(category.name().replace("_", " "));
        }

        if (ruleId != null && !ruleId.trim().isEmpty()) {
            parts.add(ruleId.trim().replace("_", " "));
        }

        if (severity != null && !severity.trim().isEmpty()) {
            parts.add(severity.trim().toUpperCase());
        }

        if (technology != null && !technology.trim().isEmpty()) {
            parts.add(technology.trim());
        }

        if (filePath != null && !filePath.trim().isEmpty()) {
            String fileName = extractFileName(filePath.trim());
            if (!fileName.isEmpty()) {
                parts.add(fileName);
            }
        }

        if (parts.isEmpty()) {
            return "general software engineering standards best practices";
        }

        return String.join(" ", parts);
    }

    private String extractFileName(String path) {
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        if (lastSlash >= 0 && lastSlash < path.length() - 1) {
            return path.substring(lastSlash + 1);
        }
        return path;
    }
}
