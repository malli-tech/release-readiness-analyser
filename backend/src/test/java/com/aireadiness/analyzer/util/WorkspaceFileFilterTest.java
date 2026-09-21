package com.aireadiness.analyzer.util;

import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class WorkspaceFileFilterTest {

    @Test
    void testIgnoredDirectories() {
        String[] ignoredPaths = {
                "node_modules/depd/index.js",
                ".git/config",
                "target/classes/App.class",
                "build/libs/app.jar",
                "dist/bundle.js",
                "coverage/lcov.info",
                "vendor/autoload.php",
                ".next/server/page.js",
                "venv/lib/python.py",
                "__pycache__/app.pyc"
        };

        for (String p : ignoredPaths) {
            assertTrue(WorkspaceFileFilter.isIgnoredPath(p), "Expected ignored path: " + p);
            assertTrue(WorkspaceFileFilter.isIgnoredPath(Paths.get(p)), "Expected ignored Path object: " + p);
        }
    }

    @Test
    void testLegitimateApplicationSourcePaths() {
        String[] legitPaths = {
                "src/main/java/com/example/App.java",
                "src/index.js",
                "app/main.py",
                "pom.xml",
                "package.json",
                "requirements.txt"
        };

        for (String p : legitPaths) {
            assertFalse(WorkspaceFileFilter.isIgnoredPath(p), "Expected legitimate path: " + p);
            assertFalse(WorkspaceFileFilter.isIgnoredPath(Paths.get(p)), "Expected legitimate Path object: " + p);
        }
    }
}
