package com.aireadiness.controller;

import com.aireadiness.dto.comparison.VersionComparisonResponse;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.User;
import com.aireadiness.repository.UserRepository;
import com.aireadiness.service.JwtService;
import com.aireadiness.service.VersionComparisonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class VersionComparisonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VersionComparisonService versionComparisonService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private void mockAuth(String token, String email, String userId) {
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        User dbUser = new User("Test User", email, "password", "STUDENT");
        dbUser.setId(userId);

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(eq(token), any(UserDetails.class))).thenReturn(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(dbUser));
    }

    @Test
    @DisplayName("1. GET /api/projects/{projectId}/releases/compare returns 200 OK with comparison response")
    public void testCompareReleasesSuccess() throws Exception {
        String token = "valid.token";
        String email = "user@example.com";
        String userId = "user-100";
        mockAuth(token, email, userId);

        VersionComparisonResponse.ReleaseComparisonOverview baseOverview = new VersionComparisonResponse.ReleaseComparisonOverview(
                "rel-1", "proj-1", "Alpha", "1.0.0", "Release 1", "ANALYZED", "ans-1", 1, "COMPLETE"
        );
        VersionComparisonResponse.ReleaseComparisonOverview targetOverview = new VersionComparisonResponse.ReleaseComparisonOverview(
                "rel-2", "proj-1", "Alpha", "1.1.0", "Release 2", "ANALYZED", "ans-2", 1, "COMPLETE"
        );
        VersionComparisonResponse.FindingsComparisonDto findingsDto = new VersionComparisonResponse.FindingsComparisonDto(10, 5, -5);

        VersionComparisonResponse responsePayload = new VersionComparisonResponse();
        responsePayload.setBaseRelease(baseOverview);
        responsePayload.setTargetRelease(targetOverview);
        responsePayload.setFindingsComparison(findingsDto);

        when(versionComparisonService.compareReleases("proj-1", "rel-1", "rel-2", userId))
                .thenReturn(responsePayload);

        mockMvc.perform(get("/api/projects/proj-1/releases/compare")
                        .param("baseReleaseId", "rel-1")
                        .param("targetReleaseId", "rel-2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseRelease.version").value("1.0.0"))
                .andExpect(jsonPath("$.targetRelease.version").value("1.1.0"))
                .andExpect(jsonPath("$.findingsComparison.delta").value(-5));
    }

    @Test
    @DisplayName("2. GET /api/projects/{projectId}/releases/compare returns 400 when releases belong to different projects")
    public void testCompareReleasesDifferentProjects() throws Exception {
        String token = "valid.token";
        String email = "user@example.com";
        String userId = "user-100";
        mockAuth(token, email, userId);

        when(versionComparisonService.compareReleases("proj-1", "rel-1", "rel-2", userId))
                .thenThrow(new IllegalArgumentException("Releases belong to different projects"));

        mockMvc.perform(get("/api/projects/proj-1/releases/compare")
                        .param("baseReleaseId", "rel-1")
                        .param("targetReleaseId", "rel-2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("3. GET /api/projects/{projectId}/releases/compare returns 404 when release not found")
    public void testCompareReleasesNotFound() throws Exception {
        String token = "valid.token";
        String email = "user@example.com";
        String userId = "user-100";
        mockAuth(token, email, userId);

        when(versionComparisonService.compareReleases("proj-1", "rel-missing", "rel-2", userId))
                .thenThrow(new ResourceNotFoundException("Release not found with id: rel-missing"));

        mockMvc.perform(get("/api/projects/proj-1/releases/compare")
                        .param("baseReleaseId", "rel-missing")
                        .param("targetReleaseId", "rel-2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
