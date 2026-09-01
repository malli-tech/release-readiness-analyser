package com.aireadiness.controller;

import com.aireadiness.dto.release.CreateReleaseRequest;
import com.aireadiness.dto.release.ReleaseResponse;
import com.aireadiness.dto.release.UpdateReleaseRequest;
import com.aireadiness.exception.DuplicateReleaseVersionException;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.service.JwtService;
import com.aireadiness.service.ReleaseService;
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
public class ReleaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReleaseService releaseService;

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
    @DisplayName("1. Create release successfully should return 201 Created with status NOT_ANALYZED")
    public void testCreateReleaseSuccess() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        CreateReleaseRequest request = new CreateReleaseRequest("v1.0", "Initial Release", "First submission");
        ReleaseResponse response = new ReleaseResponse(
                "rel-1", "proj-1", "v1.0", "Initial Release",
                "First submission", "NOT_ANALYZED", Instant.now(), Instant.now()
        );

        when(releaseService.createRelease(eq("proj-1"), any(CreateReleaseRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects/proj-1/releases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("rel-1"))
                .andExpect(jsonPath("$.version").value("v1.0"))
                .andExpect(jsonPath("$.status").value("NOT_ANALYZED"));
    }

    @Test
    @DisplayName("2. Create release with invalid data should return 400 Bad Request")
    public void testCreateReleaseInvalidData() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        CreateReleaseRequest invalidRequest = new CreateReleaseRequest("", "", "Desc");

        mockMvc.perform(post("/api/projects/proj-1/releases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.version").exists())
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    @DisplayName("3. Duplicate release version should return 409 Conflict")
    public void testDuplicateReleaseVersion() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        CreateReleaseRequest request = new CreateReleaseRequest("v1.0", "Initial Release", "Desc");

        when(releaseService.createRelease(eq("proj-1"), any(CreateReleaseRequest.class)))
                .thenThrow(new DuplicateReleaseVersionException("Release version already exists for this project."));

        mockMvc.perform(post("/api/projects/proj-1/releases")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Release version already exists for this project."));
    }

    @Test
    @DisplayName("4. Get project releases should return 200 OK and list of releases")
    public void testGetProjectReleases() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        ReleaseResponse r1 = new ReleaseResponse("rel-1", "proj-1", "v1.1", "Update", "Desc", "NOT_ANALYZED", Instant.now(), Instant.now());
        ReleaseResponse r2 = new ReleaseResponse("rel-2", "proj-1", "v1.0", "Init", "Desc", "NOT_ANALYZED", Instant.now(), Instant.now());

        when(releaseService.getProjectReleases("proj-1")).thenReturn(Arrays.asList(r1, r2));

        mockMvc.perform(get("/api/projects/proj-1/releases")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].version").value("v1.1"))
                .andExpect(jsonPath("$[1].version").value("v1.0"));
    }

    @Test
    @DisplayName("5. Get release by ID should return 200 OK and release response")
    public void testGetRelease() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        ReleaseResponse response = new ReleaseResponse("rel-1", "proj-1", "v1.0", "Init", "Desc", "NOT_ANALYZED", Instant.now(), Instant.now());
        when(releaseService.getRelease("rel-1")).thenReturn(response);

        mockMvc.perform(get("/api/releases/rel-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rel-1"))
                .andExpect(jsonPath("$.version").value("v1.0"))
                .andExpect(jsonPath("$.status").value("NOT_ANALYZED"));
    }

    @Test
    @DisplayName("6. Update release should return 200 OK and updated release")
    public void testUpdateRelease() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        UpdateReleaseRequest request = new UpdateReleaseRequest("v1.0-patch1", "Patched Release", "Bug fix release");
        ReleaseResponse response = new ReleaseResponse("rel-1", "proj-1", "v1.0-patch1", "Patched Release", "Bug fix release", "NOT_ANALYZED", Instant.now(), Instant.now());

        when(releaseService.updateRelease(eq("rel-1"), any(UpdateReleaseRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/releases/rel-1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("v1.0-patch1"))
                .andExpect(jsonPath("$.name").value("Patched Release"));
    }

    @Test
    @DisplayName("7. Delete release should return 200 OK")
    public void testDeleteRelease() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        doNothing().when(releaseService).deleteRelease("rel-1");

        mockMvc.perform(delete("/api/releases/rel-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Release deleted successfully"));
    }

    @Test
    @DisplayName("8. Unauthorized release access should return 404 Not Found")
    public void testUnauthorizedReleaseAccess() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        when(releaseService.getRelease("other-rel"))
                .thenThrow(new ResourceNotFoundException("Release not found with id: other-rel"));

        mockMvc.perform(get("/api/releases/other-rel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("9. Unauthorized release update should return 404 Not Found")
    public void testUnauthorizedReleaseUpdate() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        UpdateReleaseRequest request = new UpdateReleaseRequest("v2.0", "Hacked", "Desc");

        when(releaseService.updateRelease(eq("other-rel"), any(UpdateReleaseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Release not found with id: other-rel"));

        mockMvc.perform(put("/api/releases/other-rel")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("10. Unauthorized release deletion should return 404 Not Found")
    public void testUnauthorizedReleaseDeletion() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        doThrow(new ResourceNotFoundException("Release not found with id: other-rel"))
                .when(releaseService).deleteRelease("other-rel");

        mockMvc.perform(delete("/api/releases/other-rel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }
}
