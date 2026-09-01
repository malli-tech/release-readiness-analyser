package com.aireadiness.service;

import com.aireadiness.dto.project.CreateProjectRequest;
import com.aireadiness.dto.project.ProjectResponse;
import com.aireadiness.dto.project.UpdateProjectRequest;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.Project;
import com.aireadiness.model.User;
import com.aireadiness.repository.ProjectRepository;
import com.aireadiness.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new UsernameNotFoundException("Unauthenticated user");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public ProjectResponse createProject(CreateProjectRequest request) {
        User user = getAuthenticatedUser();

        Project project = new Project(
                user.getId(),
                request.getName().trim(),
                request.getDescription() != null ? request.getDescription().trim() : "",
                request.getProjectType().trim(),
                request.getPrimaryLanguage().trim(),
                request.getFramework() != null ? request.getFramework().trim() : "",
                request.getRepositoryUrl() != null ? request.getRepositoryUrl().trim() : ""
        );

        Project savedProject = projectRepository.save(project);
        return mapToResponse(savedProject);
    }

    public List<ProjectResponse> getUserProjects() {
        User user = getAuthenticatedUser();
        return projectRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProject(String id) {
        User user = getAuthenticatedUser();
        Project project = projectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return mapToResponse(project);
    }

    public ProjectResponse updateProject(String id, UpdateProjectRequest request) {
        User user = getAuthenticatedUser();
        Project project = projectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        project.setName(request.getName().trim());
        project.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        project.setProjectType(request.getProjectType().trim());
        project.setPrimaryLanguage(request.getPrimaryLanguage().trim());
        project.setFramework(request.getFramework() != null ? request.getFramework().trim() : "");
        project.setRepositoryUrl(request.getRepositoryUrl() != null ? request.getRepositoryUrl().trim() : "");
        project.setUpdatedAt(Instant.now());

        Project savedProject = projectRepository.save(project);
        return mapToResponse(savedProject);
    }

    public void deleteProject(String id) {
        User user = getAuthenticatedUser();
        Project project = projectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        projectRepository.delete(project);
    }

    private ProjectResponse mapToResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getUserId(),
                project.getName(),
                project.getDescription(),
                project.getProjectType(),
                project.getPrimaryLanguage(),
                project.getFramework(),
                project.getRepositoryUrl(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
