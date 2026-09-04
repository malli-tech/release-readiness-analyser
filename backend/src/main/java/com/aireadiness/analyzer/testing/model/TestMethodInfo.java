package com.aireadiness.analyzer.testing.model;

import java.util.ArrayList;
import java.util.List;

public class TestMethodInfo {

    private String name;
    private int startLine;
    private int endLine;
    private List<String> lines = new ArrayList<>();
    private boolean isSkipped;
    private boolean isEmpty;
    private boolean hasAssertion;
    private boolean hasTodo;

    public TestMethodInfo() {
    }

    public TestMethodInfo(String name, int startLine, int endLine, List<String> lines, boolean isSkipped, boolean isEmpty, boolean hasAssertion, boolean hasTodo) {
        this.name = name;
        this.startLine = startLine;
        this.endLine = endLine;
        this.lines = lines;
        this.isSkipped = isSkipped;
        this.isEmpty = isEmpty;
        this.hasAssertion = hasAssertion;
        this.hasTodo = hasTodo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
    }

    public boolean isSkipped() {
        return isSkipped;
    }

    public void setSkipped(boolean skipped) {
        isSkipped = skipped;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void setEmpty(boolean empty) {
        isEmpty = empty;
    }

    public boolean isHasAssertion() {
        return hasAssertion;
    }

    public void setHasAssertion(boolean hasAssertion) {
        this.hasAssertion = hasAssertion;
    }

    public boolean isHasTodo() {
        return hasTodo;
    }

    public void setHasTodo(boolean hasTodo) {
        this.hasTodo = hasTodo;
    }
}
