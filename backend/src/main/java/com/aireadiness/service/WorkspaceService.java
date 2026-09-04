package com.aireadiness.service;

import com.aireadiness.exception.InvalidArchiveException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class WorkspaceService {

    private final String workspaceBaseDir;
    private final long maxExtractedSizeBytes;
    private final int maxEntriesCount;

    public WorkspaceService(
            @Value("${app.upload.workspace-base-dir:target/workspaces}") String workspaceBaseDir,
            @Value("${app.upload.max-extracted-size-mb:200}") int maxExtractedSizeMb,
            @Value("${app.upload.max-entries-count:10000}") int maxEntriesCount
    ) {
        this.workspaceBaseDir = workspaceBaseDir;
        this.maxExtractedSizeBytes = (long) maxExtractedSizeMb * 1024 * 1024;
        this.maxEntriesCount = maxEntriesCount;
    }

    public static class ExtractionResult {
        private final String workspaceId;
        private final int fileCount;
        private final long totalExtractedBytes;

        public ExtractionResult(String workspaceId, int fileCount, long totalExtractedBytes) {
            this.workspaceId = workspaceId;
            this.fileCount = fileCount;
            this.totalExtractedBytes = totalExtractedBytes;
        }

        public String getWorkspaceId() {
            return workspaceId;
        }

        public int getFileCount() {
            return fileCount;
        }

        public long getTotalExtractedBytes() {
            return totalExtractedBytes;
        }
    }

    public ExtractionResult extractZipSafely(InputStream zipInputStream) {
        String workspaceId = UUID.randomUUID().toString();
        Path targetDir = Paths.get(workspaceBaseDir, workspaceId).toAbsolutePath().normalize();

        try {
            Files.createDirectories(targetDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize temporary workspace", e);
        }

        int entriesCount = 0;
        long totalSize = 0;
        byte[] buffer = new byte[8192];

        try (ZipInputStream zis = new ZipInputStream(zipInputStream)) {
            ZipEntry entry;
            boolean hasEntries = false;

            while ((entry = zis.getNextEntry()) != null) {
                hasEntries = true;
                entriesCount++;

                if (entriesCount > maxEntriesCount) {
                    cleanupWorkspace(workspaceId);
                    throw new InvalidArchiveException("Archive exceeds maximum allowed entry count limit.");
                }

                String entryName = entry.getName();
                
                // Path Traversal check: normalize and ensure child is under targetDir
                Path resolvedPath = targetDir.resolve(entryName).normalize();
                if (!resolvedPath.startsWith(targetDir) || resolvedPath.equals(targetDir)) {
                    cleanupWorkspace(workspaceId);
                    throw new InvalidArchiveException("Invalid archive contents.");
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(resolvedPath);
                } else {
                    if (resolvedPath.getParent() != null) {
                        Files.createDirectories(resolvedPath.getParent());
                    }

                    try (FileOutputStream fos = new FileOutputStream(resolvedPath.toFile())) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            totalSize += len;
                            if (totalSize > maxExtractedSizeBytes) {
                                cleanupWorkspace(workspaceId);
                                throw new InvalidArchiveException("Archive exceeds maximum uncompressed size limit.");
                            }
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }

            if (!hasEntries) {
                cleanupWorkspace(workspaceId);
                throw new InvalidArchiveException("Archive is empty or corrupted.");
            }

            return new ExtractionResult(workspaceId, entriesCount, totalSize);
        } catch (InvalidArchiveException iae) {
            cleanupWorkspace(workspaceId);
            throw iae;
        } catch (Exception e) {
            cleanupWorkspace(workspaceId);
            throw new InvalidArchiveException("Invalid archive contents.");
        }
    }

    public Path getWorkspacePath(String workspaceId) {
        if (workspaceId == null || workspaceId.trim().isEmpty()) {
            throw new InvalidArchiveException("Workspace ID is invalid or missing.");
        }
        Path base = Paths.get(workspaceBaseDir).toAbsolutePath().normalize();
        Path targetDir = base.resolve(workspaceId).normalize();

        if (!targetDir.startsWith(base)) {
            throw new InvalidArchiveException("Invalid workspace path.");
        }
        if (!Files.exists(targetDir) || !Files.isDirectory(targetDir)) {
            throw new InvalidArchiveException("Workspace path does not exist or has expired.");
        }
        return targetDir;
    }

    public void cleanupWorkspace(String workspaceId) {
        if (workspaceId == null || workspaceId.trim().isEmpty()) return;
        try {
            Path targetDir = Paths.get(workspaceBaseDir, workspaceId).toAbsolutePath().normalize();
            if (Files.exists(targetDir)) {
                Files.walk(targetDir)
                        .sorted((a, b) -> b.compareTo(a))
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (Exception ignored) {
        }
    }
}
