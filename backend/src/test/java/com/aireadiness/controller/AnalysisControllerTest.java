package com.aireadiness.controller;

import com.aireadiness.dto.analysis.AnalysisResponse;
import com.aireadiness.exception.InvalidArchiveException;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.AnalysisPlan;
import com.aireadiness.model.ProjectProfile;
import com.aireadiness.service.AnalysisService;
import com.aireadiness.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalysisService analysisService;

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

    private AnalysisResponse createSampleResponse() {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("JAVA");
        profile.setFramework("SPRING_BOOT");
        profile.setBuildSystem("MAVEN");
        profile.setProjectType("BACKEND");
        profile.setAnalysisCompleteness("COMPLETE");

        AnalysisPlan plan = new AnalysisPlan(
                List.of("CODE_QUALITY", "TESTING", "DEPENDENCIES", "SECURITY", "PERFORMANCE"),
                Map.of("CODE_QUALITY", "Evaluates maintainability.")
        );

        return new AnalysisResponse(
                "ans-1", "proj-1", "rel-1", 1, "READY_FOR_ANALYSIS",
                Instant.now(), Instant.now(), profile, plan,
                Collections.emptyList(), Collections.emptyMap(), null,
                Collections.emptyList(), "Static project detection and analysis planning completed successfully."
        );
    }

    @Test
    @DisplayName("1. Authenticated user can trigger analysis for own release (201 Created)")
    public void testStartAnalysisSuccess() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        AnalysisResponse response = createSampleResponse();
        when(analysisService.startAnalysis("rel-1")).thenReturn(response);

        mockMvc.perform(post("/api/releases/rel-1/analysis")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("ans-1"))
                .andExpect(jsonPath("$.status").value("READY_FOR_ANALYSIS"))
                .andExpect(jsonPath("$.projectProfile.primaryLanguage").value("JAVA"))
                .andExpect(jsonPath("$.projectProfile.framework").value("SPRING_BOOT"));
    }

    @Test
    @DisplayName("2. Unauthenticated user analysis request is rejected (403 Forbidden)")
    public void testUnauthenticatedAnalysis() throws Exception {
        mockMvc.perform(post("/api/releases/rel-1/analysis"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("3. Triggering analysis on another user's release returns 404 Not Found")
    public void testUnauthorizedReleaseAnalysis() throws Exception {
        String token = "valid.token";
        mockAuth(token, "otheruser@univ.edu");

        when(analysisService.startAnalysis("other-rel"))
                .thenThrow(new ResourceNotFoundException("Release not found with id: other-rel"));

        mockMvc.perform(post("/api/releases/other-rel/analysis")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("4. Triggering analysis on release without upload returns 404 Not Found")
    public void testMissingUploadAnalysis() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        when(analysisService.startAnalysis("rel-no-upload"))
                .thenThrow(new ResourceNotFoundException("No upload found for release: rel-no-upload"));

        mockMvc.perform(post("/api/releases/rel-no-upload/analysis")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("5. Triggering analysis on release with non-READY upload returns 400 Bad Request")
    public void testNonReadyUploadAnalysis() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        when(analysisService.startAnalysis("rel-pending"))
                .thenThrow(new InvalidArchiveException("Upload is not in READY status for release: rel-pending"));

        mockMvc.perform(post("/api/releases/rel-pending/analysis")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    @DisplayName("6. Retrieve latest analysis for release returns 200 OK")
    public void testGetLatestAnalysisForRelease() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        AnalysisResponse response = createSampleResponse();
        when(analysisService.getLatestAnalysisForRelease("rel-1")).thenReturn(response);

        mockMvc.perform(get("/api/releases/rel-1/analysis")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ans-1"))
                .andExpect(jsonPath("$.projectProfile.primaryLanguage").value("JAVA"));
    }

    @Test
    @DisplayName("7. Retrieve analysis by ID returns 200 OK")
    public void testGetAnalysisById() throws Exception {
        String token = "valid.token";
        mockAuth(token, "student@univ.edu");

        AnalysisResponse response = createSampleResponse();
        when(analysisService.getAnalysisById("ans-1")).thenReturn(response);

        mockMvc.perform(get("/api/analyses/ans-1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("ans-1"));
    }
}
