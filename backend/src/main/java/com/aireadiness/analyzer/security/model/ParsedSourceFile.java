package com.aireadiness.analyzer.security.model;

import java.util.List;

public class ParsedSourceFile {

    private final String relativePath;
    private final String filename;
    private final String extension;
    private final List<String> lines;

    public ParsedSourceFile(String relativePath, String filename, String extension, List<String> lines) {
        this.relativePath = relativePath;
        this.filename = filename;
        this.extension = extension;
        this.lines = lines;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public String getFilename() {
        return filename;
    }

    public String getExtension() {
        return extension;
    }

    public List<String> getLines() {
        return lines;
    }
}
