package com.aireadiness.controller;

import com.aireadiness.dto.upload.UploadResponse;
import com.aireadiness.service.UploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping("/api/releases/{releaseId}/upload")
    public ResponseEntity<UploadResponse> uploadProject(
            @PathVariable String releaseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadMode", defaultValue = "COMPLETE_PROJECT") String uploadMode
    ) {
        UploadResponse response = uploadService.uploadProject(releaseId, file, uploadMode);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/releases/{releaseId}/upload")
    public ResponseEntity<UploadResponse> getLatestUpload(@PathVariable String releaseId) {
        UploadResponse response = uploadService.getLatestUpload(releaseId);
        return ResponseEntity.ok(response);
    }
}
