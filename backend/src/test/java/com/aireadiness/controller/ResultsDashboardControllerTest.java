package com.aireadiness.controller;

import com.aireadiness.dto.results.ReleaseResultsResponse;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.User;
import com.aireadiness.repository.UserRepository;
import com.aireadiness.service.JwtService;
import com.aireadiness.service.ResultsDashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
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
public class ResultsDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResultsDashboardService resultsDashboardService;

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
    @DisplayName("1. GET /api/releases/{releaseId}/results returns 200 OK with results payload")
    public void testGetReleaseResultsSuccess() throws Exception {
        String token = "valid.token";
        String email = "user@example.com";
        String userId = "user-100";
        mockAuth(token, email, userId);

        ReleaseResultsResponse.ReleaseInfo info = new ReleaseResultsResponse.ReleaseInfo(
                "rel-1", "proj-1", "Project Alpha", "v1.0.0", "Release 1", "Desc", "ANALYZED", Instant.now(), Instant.now()
        );
        ReleaseResultsResponse responsePayload = new ReleaseResultsResponse(info, null, null, null, null, null, null);

        when(resultsDashboardService.getReleaseResults("rel-1", userId)).thenReturn(responsePayload);

        mockMvc.perform(get("/api/releases/rel-1/results")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseInfo.releaseId").value("rel-1"))
                .andExpect(jsonPath("$.releaseInfo.projectName").value("Project Alpha"))
                .andExpect(jsonPath("$.releaseInfo.version").value("v1.0.0"));
    }

    @Test
    @DisplayName("2. GET /api/releases/{releaseId}/results returns 404 when release not found or unowned")
    public void testGetReleaseResultsNotFound() throws Exception {
        String token = "valid.token";
        String email = "user@example.com";
        String userId = "user-100";
        mockAuth(token, email, userId);

        when(resultsDashboardService.getReleaseResults("rel-999", userId))
                .thenThrow(new ResourceNotFoundException("Release not found with id: rel-999"));

        mockMvc.perform(get("/api/releases/rel-999/results")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Release not found with id: rel-999"));
    }

    @Test
    @DisplayName("3. GET /api/releases/{releaseId}/results requires authentication")
    public void testGetReleaseResultsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/releases/rel-1/results"))
                .andExpect(status().isForbidden());
    }
}
