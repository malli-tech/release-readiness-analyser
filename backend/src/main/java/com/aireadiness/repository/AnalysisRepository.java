package com.aireadiness.repository;

import com.aireadiness.model.Analysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRepository extends MongoRepository<Analysis, String> {
    Optional<Analysis> findByIdAndUserId(String id, String userId);
    List<Analysis> findByReleaseIdAndUserId(String releaseId, String userId);
    Optional<Analysis> findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(String releaseId, String userId);
    void deleteByReleaseId(String releaseId);
}
