package com.aireadiness.controller;

import com.aireadiness.dto.comparison.VersionComparisonResponse;
import com.aireadiness.model.User;
import com.aireadiness.repository.UserRepository;
import com.aireadiness.service.VersionComparisonService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VersionComparisonController {

    private final VersionComparisonService versionComparisonService;
    private final UserRepository userRepository;

    public VersionComparisonController(VersionComparisonService versionComparisonService, UserRepository userRepository) {
        this.versionComparisonService = versionComparisonService;
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

    @GetMapping("/api/projects/{projectId}/releases/compare")
    public ResponseEntity<VersionComparisonResponse> compareReleases(
            @PathVariable String projectId,
            @RequestParam String baseReleaseId,
            @RequestParam String targetReleaseId,
            Authentication authentication
    ) {
        String userId = getUserIdFromAuth(authentication);
        VersionComparisonResponse response = versionComparisonService.compareReleases(
                projectId, baseReleaseId, targetReleaseId, userId
        );
        return ResponseEntity.ok(response);
    }
}
