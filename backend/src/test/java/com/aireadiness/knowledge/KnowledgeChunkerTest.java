package com.aireadiness.knowledge;

import com.aireadiness.knowledge.model.KnowledgeCategory;
import com.aireadiness.knowledge.model.KnowledgeChunk;
import com.aireadiness.knowledge.model.KnowledgeDocument;
import com.aireadiness.knowledge.model.SourceType;
import com.aireadiness.knowledge.util.KnowledgeChunker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class KnowledgeChunkerTest {

    private KnowledgeChunker chunker;

    @BeforeEach
    public void setUp() {
        chunker = new KnowledgeChunker(500, 50, 700);
    }

    private KnowledgeDocument createSampleDocument(String content) {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId("doc-123");
        doc.setTitle("Spring Boot Security Standard");
        doc.setSource("https://docs.spring.io/spring-security");
        doc.setSourceType(SourceType.OFFICIAL_DOCUMENTATION);
        doc.setCategory(KnowledgeCategory.SECURITY);
        doc.setTechnology("Java / Spring Boot");
        doc.setVersion("3.2.0");
        doc.setContent(content);
        return doc;
    }

    @Test
    @DisplayName("1. Returns empty list for null or blank content")
    public void testChunkNullOrBlank() {
        assertTrue(chunker.chunk(null).isEmpty());

        KnowledgeDocument doc = createSampleDocument("   ");
        assertTrue(chunker.chunk(doc).isEmpty());
    }

    @Test
    @DisplayName("2. Single short document creates 1 chunk with full metadata inheritance")
    public void testChunkSingleShortDocument() {
        String content = "This is a short official guidance document for Spring Boot Security configuration.";
        KnowledgeDocument doc = createSampleDocument(content);

        List<KnowledgeChunk> chunks = chunker.chunk(doc);

        assertEquals(1, chunks.size());
        KnowledgeChunk chunk = chunks.get(0);

        assertEquals("doc-123", chunk.getDocumentId());
        assertEquals(0, chunk.getChunkIndex());
        assertEquals(content, chunk.getText());
        assertEquals("Spring Boot Security Standard", chunk.getTitle());
        assertEquals("https://docs.spring.io/spring-security", chunk.getSource());
        assertEquals(SourceType.OFFICIAL_DOCUMENTATION, chunk.getSourceType());
        assertEquals(KnowledgeCategory.SECURITY, chunk.getCategory());
        assertEquals("Java / Spring Boot", chunk.getTechnology());
        assertEquals("3.2.0", chunk.getVersion());
        assertNotNull(chunk.getCreatedAt());
    }

    @Test
    @DisplayName("3. Multi-paragraph document splits cleanly at paragraph boundaries")
    public void testChunkMultiParagraphSplitting() {
        StringBuilder sb = new StringBuilder();
        sb.append("Paragraph 1: ").append("A".repeat(300)).append("\n\n");
        sb.append("Paragraph 2: ").append("B".repeat(300)).append("\n\n");
        sb.append("Paragraph 3: ").append("C".repeat(300));

        KnowledgeDocument doc = createSampleDocument(sb.toString());

        List<KnowledgeChunk> chunks = chunker.chunk(doc);

        assertTrue(chunks.size() >= 2);
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).getChunkIndex());
            assertEquals("doc-123", chunks.get(i).getDocumentId());
            assertNotNull(chunks.get(i).getText());
        }
    }

    @Test
    @DisplayName("4. Long single paragraph splits by word boundaries")
    public void testChunkLongSingleParagraph() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 150; i++) {
            sb.append("word").append(i).append(" ");
        }
        String longPara = sb.toString().trim();

        KnowledgeDocument doc = createSampleDocument(longPara);

        List<KnowledgeChunk> chunks = chunker.chunk(doc);

        assertTrue(chunks.size() > 1);
        for (int i = 0; i < chunks.size(); i++) {
            assertEquals(i, chunks.get(i).getChunkIndex());
            assertTrue(chunks.get(i).getText().length() <= chunker.getMaxChunkSize());
        }
    }

    @Test
    @DisplayName("5. Default constructor initializes standard chunking parameters")
    public void testDefaultConstructorParams() {
        KnowledgeChunker defaultChunker = new KnowledgeChunker();
        assertEquals(KnowledgeChunker.DEFAULT_TARGET_CHUNK_SIZE, defaultChunker.getTargetChunkSize());
        assertEquals(KnowledgeChunker.DEFAULT_OVERLAP_SIZE, defaultChunker.getOverlapSize());
        assertEquals(KnowledgeChunker.DEFAULT_MAX_CHUNK_SIZE, defaultChunker.getMaxChunkSize());
    }
}
