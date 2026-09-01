package com.aireadiness.controller;

import com.aireadiness.dto.auth.MessageResponse;
import com.aireadiness.dto.release.CreateReleaseRequest;
import com.aireadiness.dto.release.ReleaseResponse;
import com.aireadiness.dto.release.UpdateReleaseRequest;
import com.aireadiness.service.ReleaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReleaseController {

    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @PostMapping("/api/projects/{projectId}/releases")
    public ResponseEntity<ReleaseResponse> createRelease(
            @PathVariable String projectId,
            @Valid @RequestBody CreateReleaseRequest request
    ) {
        ReleaseResponse response = releaseService.createRelease(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/projects/{projectId}/releases")
    public ResponseEntity<List<ReleaseResponse>> getProjectReleases(@PathVariable String projectId) {
        List<ReleaseResponse> releases = releaseService.getProjectReleases(projectId);
        return ResponseEntity.ok(releases);
    }

    @GetMapping("/api/releases/{releaseId}")
    public ResponseEntity<ReleaseResponse> getRelease(@PathVariable String releaseId) {
        ReleaseResponse response = releaseService.getRelease(releaseId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/releases/{releaseId}")
    public ResponseEntity<ReleaseResponse> updateRelease(
            @PathVariable String releaseId,
            @Valid @RequestBody UpdateReleaseRequest request
    ) {
        ReleaseResponse response = releaseService.updateRelease(releaseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/releases/{releaseId}")
    public ResponseEntity<MessageResponse> deleteRelease(@PathVariable String releaseId) {
        releaseService.deleteRelease(releaseId);
        return ResponseEntity.ok(new MessageResponse("Release deleted successfully"));
    }
}
