package com.aireadiness.recommendation.controller;

import com.aireadiness.model.User;
import com.aireadiness.recommendation.dto.RecommendationResponse;
import com.aireadiness.recommendation.model.Recommendation;
import com.aireadiness.recommendation.service.RecommendationService;
import com.aireadiness.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analyses/{analysisId}")
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    public RecommendationController(RecommendationService recommendationService, UserRepository userRepository) {
        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
    }

    private String getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalArgumentException("Unauthenticated user");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationResponse>> getRecommendationsForAnalysis(
            @PathVariable String analysisId,
            Authentication authentication
    ) {
        String userId = getUserIdFromAuth(authentication);
        List<Recommendation> recs = recommendationService.getRecommendationsForAnalysis(analysisId, userId);
        List<RecommendationResponse> responses = recs.stream()
                .map(RecommendationResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/findings/{findingId}/recommendation")
    public ResponseEntity<RecommendationResponse> getRecommendationForFinding(
            @PathVariable String analysisId,
            @PathVariable String findingId,
            Authentication authentication
    ) {
        String userId = getUserIdFromAuth(authentication);
        Recommendation rec = recommendationService.getRecommendationForFinding(analysisId, findingId, userId);
        return ResponseEntity.ok(RecommendationResponse.fromDomain(rec));
    }
}
