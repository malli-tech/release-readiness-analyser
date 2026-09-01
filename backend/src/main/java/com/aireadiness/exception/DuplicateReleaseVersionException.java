package com.aireadiness.exception;

public class DuplicateReleaseVersionException extends RuntimeException {
    public DuplicateReleaseVersionException(String message) {
        super(message);
    }
}
