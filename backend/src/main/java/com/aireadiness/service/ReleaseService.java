package com.aireadiness.service;

import com.aireadiness.dto.release.CreateReleaseRequest;
import com.aireadiness.dto.release.ReleaseResponse;
import com.aireadiness.dto.release.UpdateReleaseRequest;
import com.aireadiness.exception.DuplicateReleaseVersionException;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.Project;
import com.aireadiness.model.Release;
import com.aireadiness.model.User;
import com.aireadiness.repository.ProjectRepository;
import com.aireadiness.repository.ReleaseRepository;
import com.aireadiness.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReleaseService {

    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ReleaseService(
            ReleaseRepository releaseRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository
    ) {
        this.releaseRepository = releaseRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new UsernameNotFoundException("Unauthenticated user");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    private Project verifyProjectOwnership(String projectId, String userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    public ReleaseResponse createRelease(String projectId, CreateReleaseRequest request) {
        User user = getAuthenticatedUser();
        verifyProjectOwnership(projectId, user.getId());

        String version = request.getVersion().trim();
        if (releaseRepository.existsByProjectIdAndVersion(projectId, version)) {
            throw new DuplicateReleaseVersionException("Release version already exists for this project.");
        }

        Release release = new Release(
                projectId,
                user.getId(),
                version,
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : ""
        );

        Release savedRelease = releaseRepository.save(release);
        return mapToResponse(savedRelease);
    }

    public List<ReleaseResponse> getProjectReleases(String projectId) {
        User user = getAuthenticatedUser();
        verifyProjectOwnership(projectId, user.getId());

        return releaseRepository.findByProjectIdAndUserIdOrderByCreatedAtDesc(projectId, user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ReleaseResponse getRelease(String releaseId) {
        User user = getAuthenticatedUser();
        Release release = releaseRepository.findByIdAndUserId(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        return mapToResponse(release);
    }

    public ReleaseResponse updateRelease(String releaseId, UpdateReleaseRequest request) {
        User user = getAuthenticatedUser();
        Release release = releaseRepository.findByIdAndUserId(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        String newVersion = request.getVersion().trim();
        if (releaseRepository.existsByProjectIdAndVersionAndIdNot(release.getProjectId(), newVersion, releaseId)) {
            throw new DuplicateReleaseVersionException("Release version already exists for this project.");
        }

        release.setVersion(newVersion);
        release.setName(request.getName().trim());
        release.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        release.setUpdatedAt(Instant.now());

        Release savedRelease = releaseRepository.save(release);
        return mapToResponse(savedRelease);
    }

    public void deleteRelease(String releaseId) {
        User user = getAuthenticatedUser();
        Release release = releaseRepository.findByIdAndUserId(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        releaseRepository.delete(release);
    }

    private ReleaseResponse mapToResponse(Release release) {
        return new ReleaseResponse(
                release.getId(),
                release.getProjectId(),
                release.getVersion(),
                release.getName(),
                release.getDescription(),
                release.getStatus(),
                release.getCreatedAt(),
                release.getUpdatedAt()
        );
    }
}
