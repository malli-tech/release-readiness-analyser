package com.aireadiness.knowledge.controller;

import com.aireadiness.knowledge.dto.*;
import com.aireadiness.knowledge.model.KnowledgeChunk;
import com.aireadiness.knowledge.model.KnowledgeDocument;
import com.aireadiness.knowledge.service.KnowledgeIngestionService;
import com.aireadiness.knowledge.service.KnowledgeRetrievalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeIngestionService ingestionService;
    private final KnowledgeRetrievalService retrievalService;

    public KnowledgeController(
            KnowledgeIngestionService ingestionService,
            KnowledgeRetrievalService retrievalService
    ) {
        this.ingestionService = ingestionService;
        this.retrievalService = retrievalService;
    }

    @PostMapping("/documents")
    public ResponseEntity<KnowledgeDocumentResponse> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        KnowledgeDocument doc = ingestionService.ingestDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(KnowledgeDocumentResponse.fromDomain(doc));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<KnowledgeDocumentResponse>> listActiveDocuments() {
        List<KnowledgeDocument> docs = ingestionService.listActiveDocuments();
        List<KnowledgeDocumentResponse> responses = docs.stream()
                .map(KnowledgeDocumentResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<KnowledgeDocumentResponse> getDocumentById(@PathVariable String id) {
        KnowledgeDocument doc = ingestionService.getDocumentById(id);
        return ResponseEntity.ok(KnowledgeDocumentResponse.fromDomain(doc));
    }

    @GetMapping("/documents/{id}/chunks")
    public ResponseEntity<List<KnowledgeChunkResponse>> getChunksForDocument(@PathVariable String id) {
        List<KnowledgeChunk> chunks = ingestionService.getChunksForDocument(id);
        List<KnowledgeChunkResponse> responses = chunks.stream()
                .map(KnowledgeChunkResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/retrieve")
    public ResponseEntity<KnowledgeRetrievalResponse> retrieve(@Valid @RequestBody KnowledgeRetrievalRequest request) {
        KnowledgeRetrievalResponse response = retrievalService.retrieve(request);
        return ResponseEntity.ok(response);
    }
}
