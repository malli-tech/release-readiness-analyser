package com.aireadiness.knowledge.util;

import org.springframework.stereotype.Component;

@Component
public class KnowledgeTextNormalizer {

    public String normalize(String text) {
        if (text == null) {
            return "";
        }

        // 1. Normalize line endings to \n
        String normalized = text.replace("\r\n", "\n").replace('\r', '\n');

        // 2. Trim trailing whitespace from each line while preserving leading indentation
        String[] lines = normalized.split("\n", -1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(stripTrailingWhitespace(lines[i]));
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }
        normalized = sb.toString();

        // 3. Collapse 3+ consecutive newlines into 2 newlines (preserve paragraph separation)
        normalized = normalized.replaceAll("\n{3,}", "\n\n");

        // 4. Trim leading and trailing whitespace of the overall text
        return normalized.trim();
    }

    private String stripTrailingWhitespace(String line) {
        if (line == null || line.isEmpty()) {
            return "";
        }
        int end = line.length() - 1;
        while (end >= 0 && Character.isWhitespace(line.charAt(end))) {
            end--;
        }
        return line.substring(0, end + 1);
    }
}
