package com.aireadiness.model;

import java.util.ArrayList;
import java.util.List;

public class SecuritySummary {

    private int totalSecurityFindings;
    private int criticalFindings;
    private int highFindings;
    private int mediumFindings;
    private int lowFindings;
    private int hardcodedSecretsDetected;
    private int insecureTransportFindings;
    private int dangerousExecutionFindings;
    private int injectionRiskFindings;
    private int deserializationFindings;
    private int weakCryptographyFindings;
    private int configurationFindings;
    private int sensitiveFilesDetected;
    private String securityCompleteness = "UNKNOWN"; // COMPLETE, PARTIAL, UNKNOWN
    private List<String> securityWarnings = new ArrayList<>();
    private String disclaimer = "This is a static heuristic security analysis. It does not prove exploitability and does not replace a full security assessment, dependency vulnerability scan, penetration test, or manual security review.";

    public SecuritySummary() {
    }

    public int getTotalSecurityFindings() {
        return totalSecurityFindings;
    }

    public void setTotalSecurityFindings(int totalSecurityFindings) {
        this.totalSecurityFindings = totalSecurityFindings;
    }

    public int getCriticalFindings() {
        return criticalFindings;
    }

    public void setCriticalFindings(int criticalFindings) {
        this.criticalFindings = criticalFindings;
    }

    public int getHighFindings() {
        return highFindings;
    }

    public void setHighFindings(int highFindings) {
        this.highFindings = highFindings;
    }

    public int getMediumFindings() {
        return mediumFindings;
    }

    public void setMediumFindings(int mediumFindings) {
        this.mediumFindings = mediumFindings;
    }

    public int getLowFindings() {
        return lowFindings;
    }

    public void setLowFindings(int lowFindings) {
        this.lowFindings = lowFindings;
    }

    public int getHardcodedSecretsDetected() {
        return hardcodedSecretsDetected;
    }

    public void setHardcodedSecretsDetected(int hardcodedSecretsDetected) {
        this.hardcodedSecretsDetected = hardcodedSecretsDetected;
    }

    public int getInsecureTransportFindings() {
        return insecureTransportFindings;
    }

    public void setInsecureTransportFindings(int insecureTransportFindings) {
        this.insecureTransportFindings = insecureTransportFindings;
    }

    public int getDangerousExecutionFindings() {
        return dangerousExecutionFindings;
    }

    public void setDangerousExecutionFindings(int dangerousExecutionFindings) {
        this.dangerousExecutionFindings = dangerousExecutionFindings;
    }

    public int getInjectionRiskFindings() {
        return injectionRiskFindings;
    }

    public void setInjectionRiskFindings(int injectionRiskFindings) {
        this.injectionRiskFindings = injectionRiskFindings;
    }

    public int getDeserializationFindings() {
        return deserializationFindings;
    }

    public void setDeserializationFindings(int deserializationFindings) {
        this.deserializationFindings = deserializationFindings;
    }

    public int getWeakCryptographyFindings() {
        return weakCryptographyFindings;
    }

    public void setWeakCryptographyFindings(int weakCryptographyFindings) {
        this.weakCryptographyFindings = weakCryptographyFindings;
    }

    public int getConfigurationFindings() {
        return configurationFindings;
    }

    public void setConfigurationFindings(int configurationFindings) {
        this.configurationFindings = configurationFindings;
    }

    public int getSensitiveFilesDetected() {
        return sensitiveFilesDetected;
    }

    public void setSensitiveFilesDetected(int sensitiveFilesDetected) {
        this.sensitiveFilesDetected = sensitiveFilesDetected;
    }

    public String getSecurityCompleteness() {
        return securityCompleteness;
    }

    public void setSecurityCompleteness(String securityCompleteness) {
        this.securityCompleteness = securityCompleteness;
    }

    public List<String> getSecurityWarnings() {
        return securityWarnings;
    }

    public void setSecurityWarnings(List<String> securityWarnings) {
        this.securityWarnings = securityWarnings != null ? securityWarnings : new ArrayList<>();
    }

    public String getDisclaimer() {
        return disclaimer;
    }

    public void setDisclaimer(String disclaimer) {
        this.disclaimer = disclaimer;
    }
}
