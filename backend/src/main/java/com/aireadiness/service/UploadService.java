package com.aireadiness.service;

import com.aireadiness.dto.upload.UploadResponse;
import com.aireadiness.exception.InvalidArchiveException;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.Project;
import com.aireadiness.model.Release;
import com.aireadiness.model.UploadMetadata;
import com.aireadiness.model.User;
import com.aireadiness.repository.ProjectRepository;
import com.aireadiness.repository.ReleaseRepository;
import com.aireadiness.repository.UploadRepository;
import com.aireadiness.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class UploadService {

    private final UploadRepository uploadRepository;
    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final long maxFileSize;

    public UploadService(
            UploadRepository uploadRepository,
            ReleaseRepository releaseRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            WorkspaceService workspaceService,
            @Value("${app.upload.max-file-size-mb:50}") int maxFileSizeMb
    ) {
        this.uploadRepository = uploadRepository;
        this.releaseRepository = releaseRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.workspaceService = workspaceService;
        this.maxFileSize = (long) maxFileSizeMb * 1024 * 1024;
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

    public UploadResponse uploadProject(String releaseId, MultipartFile file, String uploadMode) {
        User user = getAuthenticatedUser();

        // 1. Release validation & ownership
        Release release = releaseRepository.findByIdAndUserId(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        Project project = projectRepository.findByIdAndUserId(release.getProjectId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + release.getProjectId()));

        // 2. Validate file existence and extension
        if (file == null || file.isEmpty()) {
            throw new InvalidArchiveException("Upload file is empty or missing.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new InvalidArchiveException("Only .zip archives are supported.");
        }

        if (file.getSize() > maxFileSize) {
            throw new InvalidArchiveException("File size exceeds maximum allowed upload limit.");
        }

        // 3. Normalize mode
        String normalizedMode = "SELECTED_CONTENT".equalsIgnoreCase(uploadMode)
                ? "SELECTED_CONTENT"
                : "COMPLETE_PROJECT";

        // 4. Validate ZIP magic bytes (PK\x03\x04 or PK\x05\x06)
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[4];
            int read = is.read(header);
            if (read < 4 || header[0] != 0x50 || header[1] != 0x4B || (header[2] != 0x03 && header[2] != 0x05 && header[2] != 0x07)) {
                throw new InvalidArchiveException("Invalid archive contents: file is not a valid ZIP archive.");
            }
        } catch (InvalidArchiveException iae) {
            throw iae;
        } catch (Exception e) {
            throw new InvalidArchiveException("Failed to read archive header.");
        }

        // 5. Safe extraction to isolated temporary workspace
        WorkspaceService.ExtractionResult result;
        try (InputStream is = file.getInputStream()) {
            result = workspaceService.extractZipSafely(is);
        } catch (InvalidArchiveException iae) {
            throw iae;
        } catch (Exception e) {
            throw new InvalidArchiveException("Invalid archive contents.");
        }

        // 6. Save upload metadata to MongoDB (metadata only, no source code in DB)
        UploadMetadata metadata = new UploadMetadata(
                release.getId(),
                user.getId(),
                normalizedMode,
                originalFilename,
                file.getSize(),
                result.getFileCount(),
                result.getWorkspaceId(),
                "READY"
        );

        UploadMetadata saved = uploadRepository.save(metadata);

        return new UploadResponse(
                saved.getId(),
                saved.getReleaseId(),
                saved.getUploadMode(),
                saved.getOriginalFilename(),
                saved.getFileSize(),
                saved.getFileCount(),
                saved.getStatus(),
                saved.getUploadedAt(),
                "Project uploaded and extracted safely to isolated workspace."
        );
    }

    public UploadResponse getLatestUpload(String releaseId) {
        User user = getAuthenticatedUser();
        Release release = releaseRepository.findByIdAndUserId(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        UploadMetadata metadata = uploadRepository.findFirstByReleaseIdAndUserIdOrderByUploadedAtDesc(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No uploads found for release: " + releaseId));

        return new UploadResponse(
                metadata.getId(),
                metadata.getReleaseId(),
                metadata.getUploadMode(),
                metadata.getOriginalFilename(),
                metadata.getFileSize(),
                metadata.getFileCount(),
                metadata.getStatus(),
                metadata.getUploadedAt(),
                "Upload metadata retrieved."
        );
    }
}
