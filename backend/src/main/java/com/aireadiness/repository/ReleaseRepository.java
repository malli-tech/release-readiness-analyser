package com.aireadiness.repository;

import com.aireadiness.model.Release;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseRepository extends MongoRepository<Release, String> {
    List<Release> findByProjectIdAndUserIdOrderByCreatedAtDesc(String projectId, String userId);
    List<Release> findByProjectIdOrderByCreatedAtDesc(String projectId);
    Optional<Release> findByIdAndUserId(String id, String userId);
    Optional<Release> findByProjectIdAndVersion(String projectId, String version);
    boolean existsByProjectIdAndVersion(String projectId, String version);
    boolean existsByProjectIdAndVersionAndIdNot(String projectId, String version, String id);
    void deleteByIdAndUserId(String id, String userId);
    void deleteByProjectIdAndUserId(String projectId, String userId);
}
