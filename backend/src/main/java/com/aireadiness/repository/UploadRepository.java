package com.aireadiness.repository;

import com.aireadiness.model.UploadMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadRepository extends MongoRepository<UploadMetadata, String> {
    List<UploadMetadata> findByReleaseIdAndUserId(String releaseId, String userId);
    Optional<UploadMetadata> findFirstByReleaseIdAndUserIdOrderByUploadedAtDesc(String releaseId, String userId);
    void deleteByReleaseId(String releaseId);
}
