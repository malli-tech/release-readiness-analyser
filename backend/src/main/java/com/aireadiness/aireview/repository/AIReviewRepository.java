package com.aireadiness.aireview.repository;

import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.model.AIReviewStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIReviewRepository extends MongoRepository<AIReview, String> {

    List<AIReview> findByAnalysisId(String analysisId);

    Optional<AIReview> findByAnalysisIdAndFindingId(String analysisId, String findingId);

    List<AIReview> findByAnalysisIdAndStatus(String analysisId, AIReviewStatus status);

    void deleteByAnalysisId(String analysisId);
}
