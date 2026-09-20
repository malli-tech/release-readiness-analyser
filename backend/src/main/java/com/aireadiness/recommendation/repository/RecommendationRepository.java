package com.aireadiness.recommendation.repository;

import com.aireadiness.recommendation.model.Recommendation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecommendationRepository extends MongoRepository<Recommendation, String> {

    List<Recommendation> findByAnalysisId(String analysisId);

    Optional<Recommendation> findByAnalysisIdAndFindingId(String analysisId, String findingId);

    void deleteByAnalysisId(String analysisId);
}
