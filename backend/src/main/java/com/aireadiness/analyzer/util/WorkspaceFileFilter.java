package com.aireadiness.analyzer.util;

import java.nio.file.Path;
import java.util.Set;

public class WorkspaceFileFilter {

    private static final Set<String> IGNORED_DIRECTORIES = Set.of(
            "node_modules",
            ".git",
            "target",
            "build",
            "dist",
            "coverage",
            "vendor",
            ".next",
            "venv",
            "__pycache__"
    );

    public static boolean isIgnoredPath(Path relativePath) {
        if (relativePath == null) return false;
        for (Path segment : relativePath) {
            String segName = segment.toString().toLowerCase();
            if (IGNORED_DIRECTORIES.contains(segName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIgnoredPath(String relativePathString) {
        if (relativePathString == null || relativePathString.trim().isEmpty()) return false;
        String normalized = relativePathString.replace('\\', '/');
        String[] parts = normalized.split("/");
        for (String part : parts) {
            if (IGNORED_DIRECTORIES.contains(part.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
