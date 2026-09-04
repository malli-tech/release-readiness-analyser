package com.aireadiness.model;

public class FileDescriptor {

    private String relativePath;
    private String filename;
    private String extension;
    private long size;
    private boolean isDirectory;
    private String fileType; // SOURCE_CODE | TEST_CODE | MANIFEST | CONFIGURATION | DOCUMENTATION | BINARY | OTHER

    public FileDescriptor() {
    }

    public FileDescriptor(String relativePath, String filename, String extension, long size, boolean isDirectory, String fileType) {
        this.relativePath = relativePath;
        this.filename = filename;
        this.extension = extension;
        this.size = size;
        this.isDirectory = isDirectory;
        this.fileType = fileType;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
