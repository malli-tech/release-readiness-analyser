package com.aireadiness.knowledge;

import com.aireadiness.knowledge.dto.KnowledgeRetrievalRequest;
import com.aireadiness.knowledge.dto.KnowledgeRetrievalResponse;
import com.aireadiness.knowledge.dto.KnowledgeRetrievalResult;
import com.aireadiness.knowledge.model.KnowledgeCategory;
import com.aireadiness.knowledge.model.KnowledgeChunk;
import com.aireadiness.knowledge.model.SourceType;
import com.aireadiness.knowledge.repository.KnowledgeChunkRepository;
import com.aireadiness.knowledge.service.EmbeddingService;
import com.aireadiness.knowledge.service.KnowledgeRetrievalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KnowledgeRetrievalServiceTest {

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private KnowledgeChunkRepository chunkRepository;

    @Mock
    private MongoTemplate mongoTemplate;

    private KnowledgeRetrievalService retrievalService;

    @BeforeEach
    public void setUp() {
        retrievalService = new KnowledgeRetrievalService(
                embeddingService,
                chunkRepository,
                mongoTemplate,
                5,
                20
        );
    }

    private KnowledgeChunk createChunk(String id, String docId, int index, String text, KnowledgeCategory cat, String tech, List<Double> vector) {
        KnowledgeChunk chunk = new KnowledgeChunk();
        chunk.setId(id);
        chunk.setDocumentId(docId);
        chunk.setChunkIndex(index);
        chunk.setText(text);
        chunk.setTitle("Security Guideline");
        chunk.setSource("https://docs.oracle.com/java");
        chunk.setSourceType(SourceType.OFFICIAL_DOCUMENTATION);
        chunk.setCategory(cat);
        chunk.setTechnology(tech);
        chunk.setVersion("17");
        chunk.setEmbedding(vector);
        return chunk;
    }

    @Test
    @DisplayName("1. Throws IllegalArgumentException for null or blank query")
    public void testRetrieveValidationFailures() {
        assertThrows(IllegalArgumentException.class, () -> retrievalService.retrieve(null));

        KnowledgeRetrievalRequest req = new KnowledgeRetrievalRequest();
        req.setQuery("   ");
        assertThrows(IllegalArgumentException.class, () -> retrievalService.retrieve(req));
    }

    @Test
    @DisplayName("2. Cosine similarity calculation precision")
    public void testCalculateCosineSimilarity() {
        List<Double> v1 = List.of(1.0, 0.0, 0.0);
        List<Double> v2 = List.of(1.0, 0.0, 0.0);
        List<Double> v3 = List.of(0.0, 1.0, 0.0);
        List<Double> v4 = List.of(-1.0, 0.0, 0.0);

        assertEquals(1.0, retrievalService.calculateCosineSimilarity(v1, v2), 0.0001);
        assertEquals(0.0, retrievalService.calculateCosineSimilarity(v1, v3), 0.0001);
        assertEquals(-1.0, retrievalService.calculateCosineSimilarity(v1, v4), 0.0001);
        assertEquals(0.0, retrievalService.calculateCosineSimilarity(null, v1), 0.0001);
    }

    @Test
    @DisplayName("3. Successful semantic retrieval with topK limiting and deterministic sorting")
    public void testRetrieveSuccessAndSorting() {
        List<Double> queryVec = List.of(1.0, 0.0, 0.0);
        when(embeddingService.embed("SQL injection Java")).thenReturn(queryVec);

        KnowledgeChunk c1 = createChunk("c1", "doc-A", 0, "Lower similarity chunk", KnowledgeCategory.SECURITY, "Java", List.of(0.5, 0.5, 0.0));
        KnowledgeChunk c2 = createChunk("c2", "doc-B", 0, "Highest similarity chunk", KnowledgeCategory.SECURITY, "Java", List.of(1.0, 0.0, 0.0));
        KnowledgeChunk c3 = createChunk("c3", "doc-A", 1, "Medium similarity chunk", KnowledgeCategory.SECURITY, "Java", List.of(0.8, 0.2, 0.0));

        when(mongoTemplate.find(any(Query.class), eq(KnowledgeChunk.class))).thenReturn(List.of(c1, c2, c3));

        KnowledgeRetrievalRequest req = new KnowledgeRetrievalRequest("SQL injection Java", KnowledgeCategory.SECURITY, "Java", null, 2);

        KnowledgeRetrievalResponse response = retrievalService.retrieve(req);

        assertNotNull(response);
        assertEquals("SQL injection Java", response.getQuery());
        assertEquals(2, response.getTopK());
        assertEquals(2, response.getResults().size());

        KnowledgeRetrievalResult r1 = response.getResults().get(0);
        KnowledgeRetrievalResult r2 = response.getResults().get(1);

        assertEquals("c2", r1.getChunkId());
        assertEquals(1.0, r1.getSimilarityScore(), 0.001);

        assertEquals("c3", r2.getChunkId());
        assertTrue(r1.getSimilarityScore() >= r2.getSimilarityScore());
    }
}
