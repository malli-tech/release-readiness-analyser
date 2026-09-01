package com.aireadiness.dto.upload;

import java.time.Instant;

public class UploadResponse {

    private String uploadId;
    private String releaseId;
    private String uploadMode;
    private String filename;
    private long fileSize;
    private int fileCount;
    private String status;
    private Instant uploadedAt;
    private String message;

    public UploadResponse() {
    }

    public UploadResponse(String uploadId, String releaseId, String uploadMode, String filename, long fileSize, int fileCount, String status, Instant uploadedAt, String message) {
        this.uploadId = uploadId;
        this.releaseId = releaseId;
        this.uploadMode = uploadMode;
        this.filename = filename;
        this.fileSize = fileSize;
        this.fileCount = fileCount;
        this.status = status;
        this.uploadedAt = uploadedAt;
        this.message = message;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(String releaseId) {
        this.releaseId = releaseId;
    }

    public String getUploadMode() {
        return uploadMode;
    }

    public void setUploadMode(String uploadMode) {
        this.uploadMode = uploadMode;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
