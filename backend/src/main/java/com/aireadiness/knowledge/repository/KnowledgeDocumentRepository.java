package com.aireadiness.knowledge.repository;

import com.aireadiness.knowledge.model.KnowledgeCategory;
import com.aireadiness.knowledge.model.KnowledgeDocument;
import com.aireadiness.knowledge.model.KnowledgeDocumentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends MongoRepository<KnowledgeDocument, String> {

    List<KnowledgeDocument> findByStatus(KnowledgeDocumentStatus status);

    List<KnowledgeDocument> findByCategoryAndStatus(KnowledgeCategory category, KnowledgeDocumentStatus status);
}
