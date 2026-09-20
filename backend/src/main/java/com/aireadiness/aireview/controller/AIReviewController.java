package com.aireadiness.aireview.controller;

import com.aireadiness.aireview.dto.AIReviewResponse;
import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.service.AIReviewService;
import com.aireadiness.model.User;
import com.aireadiness.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analyses/{analysisId}")
public class AIReviewController {

    private final AIReviewService aiReviewService;
    private final UserRepository userRepository;

    public AIReviewController(AIReviewService aiReviewService, UserRepository userRepository) {
        this.aiReviewService = aiReviewService;
        this.userRepository = userRepository;
    }

    private String getUserIdFromAuth(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
    }

    @PostMapping("/ai-reviews")
    public ResponseEntity<List<AIReviewResponse>> generateReviewsForAnalysis(
            @PathVariable String analysisId,
            Authentication authentication
    ) {
        String userId = getUserIdFromAuth(authentication);
        List<AIReview> reviews = aiReviewService.generateReviewsForAnalysis(analysisId, userId);
        List<AIReviewResponse> responses = reviews.stream()
                .map(AIReviewResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/findings/{findingId}/ai-review")
    public ResponseEntity<AIReviewResponse> generateReviewForFinding(
            @PathVariable String analysisId,
            @PathVariable String findingId,
            Authentication authentication
    ) {
        String userId = getUserIdFromAuth(authentication);
        AIReview review = aiReviewService.generateReviewForFinding(analysisId, findingId, userId);
        return ResponseEntity.ok(AIReviewResponse.fromDomain(review));
    }

    @GetMapping("/ai-reviews")
    public ResponseEntity<List<AIReviewResponse>> getReviewsForAnalysis(
            @PathVariable String analysisId,
            Authentication authentication
    ) {
        String userId = getUserIdFromAuth(authentication);
        List<AIReview> reviews = aiReviewService.getReviewsForAnalysis(analysisId, userId);
        List<AIReviewResponse> responses = reviews.stream()
                .map(AIReviewResponse::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/findings/{findingId}/ai-review")
    public ResponseEntity<AIReviewResponse> getReviewForFinding(
            @PathVariable String analysisId,
            @PathVariable String findingId,
            Authentication authentication
    ) {
        String userId = getUserIdFromAuth(authentication);
        AIReview review = aiReviewService.getReviewForFinding(analysisId, findingId, userId);
        return ResponseEntity.ok(AIReviewResponse.fromDomain(review));
    }
}
