package com.aireadiness.analyzer.testing.model;

import java.util.ArrayList;
import java.util.List;

public class ParsedFileInfo {

    private String relativePath;
    private String fileName;
    private boolean testFile;
    private List<String> lines = new ArrayList<>();
    private List<TestMethodInfo> testMethods = new ArrayList<>();

    public ParsedFileInfo() {
    }

    public ParsedFileInfo(String relativePath, String fileName, boolean testFile, List<String> lines) {
        this.relativePath = relativePath;
        this.fileName = fileName;
        this.testFile = testFile;
        this.lines = lines;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isTestFile() {
        return testFile;
    }

    public void setTestFile(boolean testFile) {
        this.testFile = testFile;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public List<TestMethodInfo> getTestMethods() {
        return testMethods;
    }

    public void setTestMethods(List<TestMethodInfo> testMethods) {
        this.testMethods = testMethods;
    }
}
