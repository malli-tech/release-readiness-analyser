package com.aireadiness.knowledge.repository;

import com.aireadiness.knowledge.model.KnowledgeChunk;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeChunkRepository extends MongoRepository<KnowledgeChunk, String> {

    List<KnowledgeChunk> findByDocumentIdOrderByChunkIndexAsc(String documentId);

    void deleteByDocumentId(String documentId);
}
