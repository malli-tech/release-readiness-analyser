package com.aireadiness.recommendation;

import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.User;
import com.aireadiness.recommendation.model.Recommendation;
import com.aireadiness.recommendation.model.RecommendationEffort;
import com.aireadiness.recommendation.model.RecommendationPriority;
import com.aireadiness.recommendation.model.RecommendationStatus;
import com.aireadiness.recommendation.service.RecommendationService;
import com.aireadiness.repository.UserRepository;
import com.aireadiness.service.JwtService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    private void mockAuth(String token, String email, String userId) {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(email, "password", Collections.emptyList());
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(eq(token), any(UserDetails.class))).thenReturn(true);

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
    }

    private Recommendation createSampleRecommendation(String id, String analysisId, String findingId) {
        Recommendation rec = new Recommendation();
        rec.setId(id);
        rec.setAnalysisId(analysisId);
        rec.setFindingId(findingId);
        rec.setRuleId("SECURITY_SQL_INJECTION_RISK");
        rec.setCategory("SECURITY");
        rec.setSeverity("HIGH");
        rec.setTitle("Use Parameterized SQL Queries");
        rec.setSummary("Unsanitized input detected in database query.");
        rec.setRecommendedAction("Replace string concatenation in SQL queries with JDBC PreparedStatement parameter placeholders.");
        rec.setPriority(RecommendationPriority.CRITICAL);
        rec.setEffort(RecommendationEffort.MEDIUM);
        rec.setStatus(RecommendationStatus.OPEN);
        rec.setFilePath("src/UserRepository.java");
        rec.setLineNumber(42);
        rec.setCreatedAt(Instant.now());
        rec.setUpdatedAt(Instant.now());
        return rec;
    }

    @Test
    @DisplayName("1. GET /api/analyses/{analysisId}/recommendations retrieves recommendations list (200 OK)")
    public void testGetRecommendationsForAnalysisSuccess() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "developer@univ.edu", "user-100");

        Recommendation rec = createSampleRecommendation("rec-1", "ans-100", "find-1");
        when(recommendationService.getRecommendationsForAnalysis("ans-100", "user-100")).thenReturn(List.of(rec));

        mockMvc.perform(get("/api/analyses/ans-100/recommendations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("rec-1"))
                .andExpect(jsonPath("$[0].analysisId").value("ans-100"))
                .andExpect(jsonPath("$[0].findingId").value("find-1"))
                .andExpect(jsonPath("$[0].ruleId").value("SECURITY_SQL_INJECTION_RISK"))
                .andExpect(jsonPath("$[0].category").value("SECURITY"))
                .andExpect(jsonPath("$[0].priority").value("CRITICAL"))
                .andExpect(jsonPath("$[0].effort").value("MEDIUM"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].filePath").value("src/UserRepository.java"))
                .andExpect(jsonPath("$[0].lineNumber").value(42));
    }

    @Test
    @DisplayName("2. GET /api/analyses/{analysisId}/findings/{findingId}/recommendation retrieves finding recommendation (200 OK)")
    public void testGetRecommendationForFindingSuccess() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "developer@univ.edu", "user-100");

        Recommendation rec = createSampleRecommendation("rec-2", "ans-100", "find-2");
        when(recommendationService.getRecommendationForFinding("ans-100", "find-2", "user-100")).thenReturn(rec);

        mockMvc.perform(get("/api/analyses/ans-100/findings/find-2/recommendation")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rec-2"))
                .andExpect(jsonPath("$.findingId").value("find-2"))
                .andExpect(jsonPath("$.title").value("Use Parameterized SQL Queries"));
    }

    @Test
    @DisplayName("3. GET /api/analyses/{analysisId}/recommendations returns 404 when analysis not found or not owned")
    public void testGetRecommendationsNotFound() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "developer@univ.edu", "user-100");

        when(recommendationService.getRecommendationsForAnalysis("ans-unowned", "user-100"))
                .thenThrow(new ResourceNotFoundException("Analysis not found with id: ans-unowned"));

        mockMvc.perform(get("/api/analyses/ans-unowned/recommendations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
