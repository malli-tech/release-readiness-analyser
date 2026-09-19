package com.aireadiness.knowledge;

import com.aireadiness.knowledge.util.KnowledgeTextNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeTextNormalizerTest {

    private KnowledgeTextNormalizer normalizer;

    @BeforeEach
    void setUp() {
        normalizer = new KnowledgeTextNormalizer();
    }

    @Test
    @DisplayName("Should normalize CRLF line endings to LF and trim whitespace")
    void testLineEndingNormalization() {
        String input = "Line 1\r\nLine 2\rLine 3\n";
        String expected = "Line 1\nLine 2\nLine 3";

        assertEquals(expected, normalizer.normalize(input));
    }

    @Test
    @DisplayName("Should collapse 3+ consecutive newlines into 2 newlines (paragraph boundary)")
    void testCollapseExcessiveNewlines() {
        String input = "Paragraph 1\n\n\n\n\nParagraph 2";
        String expected = "Paragraph 1\n\nParagraph 2";

        assertEquals(expected, normalizer.normalize(input));
    }

    @Test
    @DisplayName("Should preserve code block indentation, headings, and punctuation")
    void testPreserveCodeAndHeadings() {
        String input = "# Heading Title\n\n```java\n    public void test() {\n        return;\n    }\n```";
        String result = normalizer.normalize(input);

        assertTrue(result.contains("# Heading Title"));
        assertTrue(result.contains("    public void test() {"));
    }

    @Test
    @DisplayName("Should be 100% deterministic: same input produces identical output")
    void testDeterministicNormalization() {
        String input = "\r\n\n  Sample Technical Text   \n\n\n  More Details \r\n";
        String res1 = normalizer.normalize(input);
        String res2 = normalizer.normalize(input);

        assertEquals(res1, res2);
    }
}
