package com.aireadiness.repository;

import com.aireadiness.model.report.ReleaseReport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReleaseReportRepository extends MongoRepository<ReleaseReport, String> {
    List<ReleaseReport> findByProjectIdAndUserIdOrderByCreatedAtDesc(String projectId, String userId);
    Optional<ReleaseReport> findByProjectIdAndReleaseIdAndUserId(String projectId, String releaseId, String userId);
    Optional<ReleaseReport> findByReleaseIdAndUserId(String releaseId, String userId);
    boolean existsByReleaseId(String releaseId);
}
