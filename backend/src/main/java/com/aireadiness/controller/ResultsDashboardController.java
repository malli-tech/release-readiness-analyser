package com.aireadiness.controller;

import com.aireadiness.dto.results.ReleaseResultsResponse;
import com.aireadiness.model.User;
import com.aireadiness.repository.UserRepository;
import com.aireadiness.service.ResultsDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResultsDashboardController {

    private final ResultsDashboardService resultsDashboardService;
    private final UserRepository userRepository;

    public ResultsDashboardController(ResultsDashboardService resultsDashboardService, UserRepository userRepository) {
        this.resultsDashboardService = resultsDashboardService;
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

    @GetMapping("/api/releases/{releaseId}/results")
    public ResponseEntity<ReleaseResultsResponse> getReleaseResults(
            @PathVariable String releaseId,
            Authentication authentication
    ) {
        String userId = getUserIdFromAuth(authentication);
        ReleaseResultsResponse response = resultsDashboardService.getReleaseResults(releaseId, userId);
        return ResponseEntity.ok(response);
    }
}
