package com.aireadiness.dto.project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name must not exceed 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Project type is required")
    private String projectType;

    @NotBlank(message = "Primary language is required")
    private String primaryLanguage;

    private String framework;

    @Pattern(regexp = "^(https?://.+)?$", message = "Repository URL must start with http:// or https://")
    private String repositoryUrl;

    public CreateProjectRequest() {
    }

    public CreateProjectRequest(String name, String description, String projectType, String primaryLanguage, String framework, String repositoryUrl) {
        this.name = name;
        this.description = description;
        this.projectType = projectType;
        this.primaryLanguage = primaryLanguage;
        this.framework = framework;
        this.repositoryUrl = repositoryUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }
}
