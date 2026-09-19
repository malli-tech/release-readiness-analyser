package com.aireadiness.knowledge;

import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.knowledge.dto.CreateDocumentRequest;
import com.aireadiness.knowledge.model.*;
import com.aireadiness.knowledge.repository.KnowledgeChunkRepository;
import com.aireadiness.knowledge.repository.KnowledgeDocumentRepository;
import com.aireadiness.knowledge.service.KnowledgeIngestionService;
import com.aireadiness.knowledge.util.KnowledgeChunker;
import com.aireadiness.knowledge.util.KnowledgeTextNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KnowledgeIngestionServiceTest {

    @Mock
    private KnowledgeDocumentRepository documentRepository;

    @Mock
    private KnowledgeChunkRepository chunkRepository;

    private KnowledgeTextNormalizer textNormalizer;
    private KnowledgeChunker chunker;
    private KnowledgeIngestionService ingestionService;

    @BeforeEach
    public void setUp() {
        textNormalizer = new KnowledgeTextNormalizer();
        chunker = new KnowledgeChunker();
        ingestionService = new KnowledgeIngestionService(
                documentRepository,
                chunkRepository,
                textNormalizer,
                chunker
        );
    }

    private CreateDocumentRequest createValidRequest() {
        CreateDocumentRequest req = new CreateDocumentRequest();
        req.setTitle("Java 17 Security Guidelines");
        req.setSource("https://docs.oracle.com/java17/security");
        req.setSourceType(SourceType.OFFICIAL_DOCUMENTATION);
        req.setCategory(KnowledgeCategory.SECURITY);
        req.setTechnology("Java 17");
        req.setVersion("17.0.8");
        req.setDescription("Official Java 17 security recommendation rules.");
        req.setContent("Always use strong encryption algorithms and keep cryptographic libraries updated.");
        return req;
    }

    @Test
    @DisplayName("1. Successful document ingestion persists document and generated chunks")
    public void testIngestDocumentSuccess() {
        CreateDocumentRequest request = createValidRequest();

        when(documentRepository.save(any(KnowledgeDocument.class))).thenAnswer(invocation -> {
            KnowledgeDocument doc = invocation.getArgument(0);
            if (doc.getId() == null) {
                doc.setId("doc-999");
            }
            return doc;
        });

        KnowledgeDocument result = ingestionService.ingestDocument(request);

        assertNotNull(result);
        assertEquals("doc-999", result.getId());
        assertEquals("Java 17 Security Guidelines", result.getTitle());
        assertEquals(KnowledgeDocumentStatus.ACTIVE, result.getStatus());
        assertEquals(1, result.getChunkCount());

        verify(documentRepository, times(2)).save(any(KnowledgeDocument.class));
        verify(chunkRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("2. Throws IllegalArgumentException for missing required request fields")
    public void testIngestDocumentValidationFailures() {
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestDocument(null));

        CreateDocumentRequest req1 = createValidRequest();
        req1.setTitle("  ");
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestDocument(req1));

        CreateDocumentRequest req2 = createValidRequest();
        req2.setSource(null);
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestDocument(req2));

        CreateDocumentRequest req3 = createValidRequest();
        req3.setSourceType(null);
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestDocument(req3));

        CreateDocumentRequest req4 = createValidRequest();
        req4.setCategory(null);
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestDocument(req4));

        CreateDocumentRequest req5 = createValidRequest();
        req5.setContent("");
        assertThrows(IllegalArgumentException.class, () -> ingestionService.ingestDocument(req5));
    }

    @Test
    @DisplayName("3. Rollback triggers document and chunk cleanup when chunk persistence fails")
    public void testIngestDocumentRollbackOnChunkFailure() {
        CreateDocumentRequest request = createValidRequest();

        when(documentRepository.save(any(KnowledgeDocument.class))).thenAnswer(invocation -> {
            KnowledgeDocument doc = invocation.getArgument(0);
            doc.setId("doc-rollback");
            return doc;
        });

        when(chunkRepository.saveAll(anyList())).thenThrow(new RuntimeException("Database error saving chunks"));

        assertThrows(RuntimeException.class, () -> ingestionService.ingestDocument(request));

        verify(chunkRepository, times(1)).deleteByDocumentId("doc-rollback");
        verify(documentRepository, times(1)).deleteById("doc-rollback");
    }

    @Test
    @DisplayName("4. Retrieve document by ID successfully and throw ResourceNotFoundException when missing")
    public void testGetDocumentById() {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId("doc-1");
        doc.setTitle("Found Document");

        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(doc));
        when(documentRepository.findById("doc-2")).thenReturn(Optional.empty());

        assertEquals("Found Document", ingestionService.getDocumentById("doc-1").getTitle());
        assertThrows(ResourceNotFoundException.class, () -> ingestionService.getDocumentById("doc-2"));
    }

    @Test
    @DisplayName("5. List active documents and category filtering")
    public void testListActiveDocuments() {
        KnowledgeDocument doc1 = new KnowledgeDocument();
        doc1.setId("doc-1");
        doc1.setStatus(KnowledgeDocumentStatus.ACTIVE);

        when(documentRepository.findByStatus(KnowledgeDocumentStatus.ACTIVE)).thenReturn(List.of(doc1));
        when(documentRepository.findByCategoryAndStatus(KnowledgeCategory.SECURITY, KnowledgeDocumentStatus.ACTIVE))
                .thenReturn(List.of(doc1));

        assertEquals(1, ingestionService.listActiveDocuments().size());
        assertEquals(1, ingestionService.listActiveDocumentsByCategory(KnowledgeCategory.SECURITY).size());
    }

    @Test
    @DisplayName("6. Deactivate document sets status to INACTIVE")
    public void testDeactivateDocument() {
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId("doc-deactivate");
        doc.setStatus(KnowledgeDocumentStatus.ACTIVE);

        when(documentRepository.findById("doc-deactivate")).thenReturn(Optional.of(doc));

        ingestionService.deactivateDocument("doc-deactivate");

        assertEquals(KnowledgeDocumentStatus.INACTIVE, doc.getStatus());
        verify(documentRepository, times(1)).save(doc);
    }
}
