package com.aireadiness.controller;

import com.aireadiness.dto.project.CreateProjectRequest;
import com.aireadiness.dto.project.ProjectResponse;
import com.aireadiness.dto.project.UpdateProjectRequest;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.service.AuthService;
import com.aireadiness.service.JwtService;
import com.aireadiness.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private void mockAuth(String token, String email) {
        UserDetails userDetails = new User(email, "password", Collections.emptyList());
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(eq(token), any(UserDetails.class))).thenReturn(true);
    }

    @Test
    @DisplayName("1. Create project successfully should return 201 Created")
    public void testCreateProjectSuccess() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        CreateProjectRequest request = new CreateProjectRequest(
                "AI Release Analyzer",
                "Release readiness evaluation system",
                "WEB_APPLICATION",
                "Java",
                "Spring Boot",
                "https://github.com/org/repo"
        );

        ProjectResponse response = new ProjectResponse(
                "proj-1", "usr-1", "AI Release Analyzer",
                "Release readiness evaluation system", "WEB_APPLICATION",
                "Java", "Spring Boot", "https://github.com/org/repo",
                Instant.now(), Instant.now()
        );

        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("proj-1"))
                .andExpect(jsonPath("$.name").value("AI Release Analyzer"))
                .andExpect(jsonPath("$.projectType").value("WEB_APPLICATION"));
    }

    @Test
    @DisplayName("2. Create project validation failure should return 400 Bad Request")
    public void testCreateProjectValidationFailure() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        CreateProjectRequest invalidRequest = new CreateProjectRequest(
                "", // blank name
                "Some description",
                "", // blank type
                "", // blank language
                "",
                "invalid-url" // invalid URL format
        );

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.projectType").exists())
                .andExpect(jsonPath("$.validationErrors.primaryLanguage").exists());
    }

    @Test
    @DisplayName("3. Get user's projects should return 200 OK and list of projects")
    public void testGetUserProjects() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        ProjectResponse p1 = new ProjectResponse("proj-1", "usr-1", "Project 1", "Desc", "REST_API", "Java", "Spring", "https://github.com/repo", Instant.now(), Instant.now());
        ProjectResponse p2 = new ProjectResponse("proj-2", "usr-1", "Project 2", "Desc", "WEB_APP", "TypeScript", "Next.js", "https://github.com/repo", Instant.now(), Instant.now());

        when(projectService.getUserProjects()).thenReturn(Arrays.asList(p1, p2));

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("proj-1"))
                .andExpect(jsonPath("$[1].id").value("proj-2"));
    }

    @Test
    @DisplayName("4. Get own project should return 200 OK and project response")
    public void testGetOwnProject() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        ProjectResponse project = new ProjectResponse("proj-1", "usr-1", "Project 1", "Desc", "REST_API", "Java", "Spring", "https://github.com/repo", Instant.now(), Instant.now());
        when(projectService.getProject("proj-1")).thenReturn(project);

        mockMvc.perform(get("/api/projects/proj-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("proj-1"))
                .andExpect(jsonPath("$.name").value("Project 1"));
    }

    @Test
    @DisplayName("5. Get another user's project should return 404 Not Found")
    public void testGetAnotherUserProject() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        when(projectService.getProject("other-proj"))
                .thenThrow(new ResourceNotFoundException("Project not found with id: other-proj"));

        mockMvc.perform(get("/api/projects/other-proj")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Project not found with id: other-proj"));
    }

    @Test
    @DisplayName("6. Update own project should return 200 OK and updated project")
    public void testUpdateOwnProject() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        UpdateProjectRequest request = new UpdateProjectRequest(
                "Updated Project Name",
                "Updated Description",
                "MICROSERVICE",
                "Go",
                "Gin",
                "https://github.com/repo/new"
        );

        ProjectResponse updatedResponse = new ProjectResponse(
                "proj-1", "usr-1", "Updated Project Name",
                "Updated Description", "MICROSERVICE",
                "Go", "Gin", "https://github.com/repo/new",
                Instant.now(), Instant.now()
        );

        when(projectService.updateProject(eq("proj-1"), any(UpdateProjectRequest.class))).thenReturn(updatedResponse);

        mockMvc.perform(put("/api/projects/proj-1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Project Name"))
                .andExpect(jsonPath("$.projectType").value("MICROSERVICE"))
                .andExpect(jsonPath("$.primaryLanguage").value("Go"));
    }

    @Test
    @DisplayName("7. Attempt to update another user's project should return 404 Not Found")
    public void testUpdateAnotherUserProject() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        UpdateProjectRequest request = new UpdateProjectRequest(
                "Hacked Name", "Hacked Desc", "WEB_APP", "Java", "Spring", "https://github.com/repo"
        );

        when(projectService.updateProject(eq("other-proj"), any(UpdateProjectRequest.class)))
                .thenThrow(new ResourceNotFoundException("Project not found with id: other-proj"));

        mockMvc.perform(put("/api/projects/other-proj")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("8. Delete own project should return 200 OK")
    public void testDeleteOwnProject() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        doNothing().when(projectService).deleteProject("proj-1");

        mockMvc.perform(delete("/api/projects/proj-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Project deleted successfully"));
    }

    @Test
    @DisplayName("9. Attempt to delete another user's project should return 404 Not Found")
    public void testDeleteAnotherUserProject() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        doThrow(new ResourceNotFoundException("Project not found with id: other-proj"))
                .when(projectService).deleteProject("other-proj");

        mockMvc.perform(delete("/api/projects/other-proj")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("10. Unauthenticated project access should return 403 Forbidden")
    public void testUnauthenticatedProjectAccess() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/projects/proj-1"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/projects/proj-1"))
                .andExpect(status().isForbidden());
    }
}
