package com.aireadiness.model;

import java.util.ArrayList;
import java.util.List;

public class ProjectStructure {

    private int totalFiles;
    private int totalDirectories;
    private int sourceFileCount;
    private int testFileCount;
    private int manifestFileCount;
    private int configFileCount;
    private int docFileCount;
    private List<FileDescriptor> sampleFiles = new ArrayList<>();

    public ProjectStructure() {
    }

    public ProjectStructure(int totalFiles, int totalDirectories, int sourceFileCount, int testFileCount, int manifestFileCount, int configFileCount, int docFileCount, List<FileDescriptor> sampleFiles) {
        this.totalFiles = totalFiles;
        this.totalDirectories = totalDirectories;
        this.sourceFileCount = sourceFileCount;
        this.testFileCount = testFileCount;
        this.manifestFileCount = manifestFileCount;
        this.configFileCount = configFileCount;
        this.docFileCount = docFileCount;
        this.sampleFiles = sampleFiles != null ? sampleFiles : new ArrayList<>();
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getTotalDirectories() {
        return totalDirectories;
    }

    public void setTotalDirectories(int totalDirectories) {
        this.totalDirectories = totalDirectories;
    }

    public int getSourceFileCount() {
        return sourceFileCount;
    }

    public void setSourceFileCount(int sourceFileCount) {
        this.sourceFileCount = sourceFileCount;
    }

    public int getTestFileCount() {
        return testFileCount;
    }

    public void setTestFileCount(int testFileCount) {
        this.testFileCount = testFileCount;
    }

    public int getManifestFileCount() {
        return manifestFileCount;
    }

    public void setManifestFileCount(int manifestFileCount) {
        this.manifestFileCount = manifestFileCount;
    }

    public int getConfigFileCount() {
        return configFileCount;
    }

    public void setConfigFileCount(int configFileCount) {
        this.configFileCount = configFileCount;
    }

    public int getDocFileCount() {
        return docFileCount;
    }

    public void setDocFileCount(int docFileCount) {
        this.docFileCount = docFileCount;
    }

    public List<FileDescriptor> getSampleFiles() {
        return sampleFiles;
    }

    public void setSampleFiles(List<FileDescriptor> sampleFiles) {
        this.sampleFiles = sampleFiles;
    }
}
