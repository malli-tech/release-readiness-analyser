package com.aireadiness.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "releases")
@CompoundIndexes({
    @CompoundIndex(name = "project_version_idx", def = "{'projectId': 1, 'version': 1}", unique = true)
})
public class Release {

    @Id
    private String id;

    @Indexed
    private String projectId;

    @Indexed
    private String userId;

    private String version;

    private String name;

    private String description;

    private String status = "NOT_ANALYZED";

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    public Release() {
    }

    public Release(String projectId, String userId, String version, String name, String description) {
        this.projectId = projectId;
        this.userId = userId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.status = "NOT_ANALYZED";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
