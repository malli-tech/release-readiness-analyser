package com.aireadiness.knowledge.service;

import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.knowledge.dto.CreateDocumentRequest;
import com.aireadiness.knowledge.model.*;
import com.aireadiness.knowledge.repository.KnowledgeChunkRepository;
import com.aireadiness.knowledge.repository.KnowledgeDocumentRepository;
import com.aireadiness.knowledge.util.KnowledgeChunker;
import com.aireadiness.knowledge.util.KnowledgeTextNormalizer;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeIngestionService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeChunkRepository chunkRepository;
    private final KnowledgeTextNormalizer textNormalizer;
    private final KnowledgeChunker chunker;
    private final EmbeddingService embeddingService;

    public KnowledgeIngestionService(
            KnowledgeDocumentRepository documentRepository,
            KnowledgeChunkRepository chunkRepository,
            KnowledgeTextNormalizer textNormalizer,
            KnowledgeChunker chunker,
            EmbeddingService embeddingService
    ) {
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.textNormalizer = textNormalizer;
        this.chunker = chunker;
        this.embeddingService = embeddingService;
    }

    public KnowledgeDocument ingestDocument(CreateDocumentRequest request) {
        validateRequest(request);

        // Normalize text content
        String normalizedContent = textNormalizer.normalize(request.getContent());
        if (normalizedContent.isEmpty()) {
            throw new IllegalArgumentException("Document content must not be blank after normalization");
        }

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(request.getTitle().trim());
        doc.setSource(request.getSource().trim());
        doc.setSourceType(request.getSourceType());
        doc.setCategory(request.getCategory());
        doc.setTechnology(request.getTechnology() != null ? request.getTechnology().trim() : null);
        doc.setVersion(request.getVersion() != null ? request.getVersion().trim() : null);
        doc.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        doc.setContent(normalizedContent);
        doc.setStatus(KnowledgeDocumentStatus.ACTIVE);
        doc.setCreatedAt(Instant.now());
        doc.setUpdatedAt(Instant.now());

        // Save document to generate document ID
        KnowledgeDocument savedDoc = documentRepository.save(doc);

        try {
            // Chunk document
            List<KnowledgeChunk> chunks = chunker.chunk(savedDoc);
            if (chunks.isEmpty()) {
                throw new IllegalStateException("Failed to generate knowledge chunks from normalized content");
            }

            // Generate embeddings for all chunks before persistence
            List<String> chunkTexts = chunks.stream()
                    .map(KnowledgeChunk::getText)
                    .collect(Collectors.toList());
            List<List<Double>> embeddings = embeddingService.embedBatch(chunkTexts);

            if (embeddings == null || embeddings.size() != chunks.size()) {
                throw new IllegalStateException("Generated embeddings count does not match chunks count");
            }

            for (int i = 0; i < chunks.size(); i++) {
                chunks.get(i).setEmbedding(embeddings.get(i));
            }

            // Save chunks with embeddings
            chunkRepository.saveAll(chunks);

            // Update chunk count on document
            savedDoc.setChunkCount(chunks.size());
            savedDoc.setUpdatedAt(Instant.now());
            return documentRepository.save(savedDoc);
        } catch (Exception e) {
            // Atomic rollback: Delete document if chunking, embedding generation, or persistence fails
            try {
                chunkRepository.deleteByDocumentId(savedDoc.getId());
                documentRepository.deleteById(savedDoc.getId());
            } catch (Exception rollbackEx) {
                // Log/ignore cleanup error to propagate root cause
            }
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
                throw e;
            }
            throw new RuntimeException("Knowledge document ingestion failed during chunking/embedding/persistence: " + e.getMessage(), e);
        }
    }

    public void reEmbedDocument(String documentId) {
        KnowledgeDocument doc = getDocumentById(documentId);
        List<KnowledgeChunk> chunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);

        if (chunks.isEmpty()) {
            return;
        }

        List<String> chunkTexts = chunks.stream()
                .map(KnowledgeChunk::getText)
                .collect(Collectors.toList());
        List<List<Double>> embeddings = embeddingService.embedBatch(chunkTexts);

        if (embeddings == null || embeddings.size() != chunks.size()) {
            throw new IllegalStateException("Re-embedding generated vector count does not match chunk count for document: " + documentId);
        }

        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).setEmbedding(embeddings.get(i));
        }

        chunkRepository.saveAll(chunks);
        doc.setUpdatedAt(Instant.now());
        documentRepository.save(doc);
    }

    public KnowledgeDocument getDocumentById(String id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge document not found with id: " + id));
    }

    public List<KnowledgeDocument> listActiveDocuments() {
        return documentRepository.findByStatus(KnowledgeDocumentStatus.ACTIVE);
    }

    public List<KnowledgeDocument> listActiveDocumentsByCategory(KnowledgeCategory category) {
        return documentRepository.findByCategoryAndStatus(category, KnowledgeDocumentStatus.ACTIVE);
    }

    public List<KnowledgeChunk> getChunksForDocument(String documentId) {
        // Ensure parent document exists
        getDocumentById(documentId);
        return chunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);
    }

    public void deactivateDocument(String id) {
        KnowledgeDocument doc = getDocumentById(id);
        doc.setStatus(KnowledgeDocumentStatus.INACTIVE);
        doc.setUpdatedAt(Instant.now());
        documentRepository.save(doc);
    }

    private void validateRequest(CreateDocumentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("CreateDocumentRequest must not be null");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required and must not be blank");
        }
        if (request.getSource() == null || request.getSource().trim().isEmpty()) {
            throw new IllegalArgumentException("Source is required and must not be blank");
        }
        if (request.getSourceType() == null) {
            throw new IllegalArgumentException("SourceType is required");
        }
        if (request.getCategory() == null) {
            throw new IllegalArgumentException("Category is required");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required and must not be blank");
        }
    }
}
