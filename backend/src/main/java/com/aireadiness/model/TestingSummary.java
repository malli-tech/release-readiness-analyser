package com.aireadiness.model;

import java.util.ArrayList;
import java.util.List;

public class TestingSummary {

    private int testFiles;
    private int sourceFiles;
    private double testPresenceRatio;
    private List<String> detectedFrameworks = new ArrayList<>();
    private int testsDetected;
    private int assertionsDetected;
    private int skippedTestsDetected;
    private int emptyTestsDetected;
    private int todoTestsDetected;
    private int sourceFilesWithoutTests;
    private int testFilesWithoutObviousAssertions;
    private String testingCompleteness; // STRONG, MODERATE, WEAK, UNKNOWN, PARTIAL
    private List<String> testingWarnings = new ArrayList<>();
    private String disclaimer = "This is a static test-presence indicator, not runtime code coverage.";

    public TestingSummary() {
    }

    public int getTestFiles() {
        return testFiles;
    }

    public void setTestFiles(int testFiles) {
        this.testFiles = testFiles;
    }

    public int getSourceFiles() {
        return sourceFiles;
    }

    public void setSourceFiles(int sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public double getTestPresenceRatio() {
        return testPresenceRatio;
    }

    public void setTestPresenceRatio(double testPresenceRatio) {
        this.testPresenceRatio = testPresenceRatio;
    }

    public List<String> getDetectedFrameworks() {
        return detectedFrameworks;
    }

    public void setDetectedFrameworks(List<String> detectedFrameworks) {
        this.detectedFrameworks = detectedFrameworks;
    }

    public int getTestsDetected() {
        return testsDetected;
    }

    public void setTestsDetected(int testsDetected) {
        this.testsDetected = testsDetected;
    }

    public int getAssertionsDetected() {
        return assertionsDetected;
    }

    public void setAssertionsDetected(int assertionsDetected) {
        this.assertionsDetected = assertionsDetected;
    }

    public int getSkippedTestsDetected() {
        return skippedTestsDetected;
    }

    public void setSkippedTestsDetected(int skippedTestsDetected) {
        this.skippedTestsDetected = skippedTestsDetected;
    }

    public int getEmptyTestsDetected() {
        return emptyTestsDetected;
    }

    public void setEmptyTestsDetected(int emptyTestsDetected) {
        this.emptyTestsDetected = emptyTestsDetected;
    }

    public int getTodoTestsDetected() {
        return todoTestsDetected;
    }

    public void setTodoTestsDetected(int todoTestsDetected) {
        this.todoTestsDetected = todoTestsDetected;
    }

    public int getSourceFilesWithoutTests() {
        return sourceFilesWithoutTests;
    }

    public void setSourceFilesWithoutTests(int sourceFilesWithoutTests) {
        this.sourceFilesWithoutTests = sourceFilesWithoutTests;
    }

    public int getTestFilesWithoutObviousAssertions() {
        return testFilesWithoutObviousAssertions;
    }

    public void setTestFilesWithoutObviousAssertions(int testFilesWithoutObviousAssertions) {
        this.testFilesWithoutObviousAssertions = testFilesWithoutObviousAssertions;
    }

    public String getTestingCompleteness() {
        return testingCompleteness;
    }

    public void setTestingCompleteness(String testingCompleteness) {
        this.testingCompleteness = testingCompleteness;
    }

    public List<String> getTestingWarnings() {
        return testingWarnings;
    }

    public void setTestingWarnings(List<String> testingWarnings) {
        this.testingWarnings = testingWarnings;
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
