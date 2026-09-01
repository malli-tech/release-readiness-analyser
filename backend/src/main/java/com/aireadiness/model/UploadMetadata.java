package com.aireadiness.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "uploads")
public class UploadMetadata {

    @Id
    private String id;

    @Indexed
    private String releaseId;

    @Indexed
    private String userId;

    private String uploadMode; // COMPLETE_PROJECT | SELECTED_CONTENT

    private String originalFilename;

    private long fileSize;

    private int fileCount;

    private String workspaceId;

    private String status = "READY"; // UPLOADING | EXTRACTING | READY | FAILED

    private Instant uploadedAt = Instant.now();

    public UploadMetadata() {
    }

    public UploadMetadata(String releaseId, String userId, String uploadMode, String originalFilename, long fileSize, int fileCount, String workspaceId, String status) {
        this.releaseId = releaseId;
        this.userId = userId;
        this.uploadMode = uploadMode;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.fileCount = fileCount;
        this.workspaceId = workspaceId;
        this.status = status;
        this.uploadedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUploadMode() {
        return uploadMode;
    }

    public void setUploadMode(String uploadMode) {
        this.uploadMode = uploadMode;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public String getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
