package com.aireadiness.aireview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.model.AIReviewConfidence;
import com.aireadiness.aireview.model.AIReviewStatus;
import com.aireadiness.aireview.service.AIReviewService;
import com.aireadiness.model.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AIReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AIReviewService aiReviewService;

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

    private AIReview createSampleReview(String id) {
        AIReview review = new AIReview();
        review.setId(id);
        review.setAnalysisId("ans-100");
        review.setFindingId("find-1");
        review.setRuleId("SECURITY_SQL_INJECTION");
        review.setCategory("SECURITY");
        review.setStatus(AIReviewStatus.COMPLETED);
        review.setSummary("SQL Injection risk explanation");
        review.setWhyItMatters("Critical database exposure");
        review.setWhatToReview(List.of("Check line 42"));
        review.setSuggestedFix("Use PreparedStatements");
        review.setConfidence(AIReviewConfidence.HIGH);
        review.setCreatedAt(Instant.now());
        review.setUpdatedAt(Instant.now());
        return review;
    }

    @Test
    @DisplayName("1. POST /api/analyses/{id}/ai-reviews triggers review generation (200 OK)")
    public void testGenerateReviewsSuccess() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "developer@univ.edu", "user-100");

        AIReview review = createSampleReview("rev-100");
        when(aiReviewService.generateReviewsForAnalysis("ans-100", "user-100")).thenReturn(List.of(review));

        mockMvc.perform(post("/api/analyses/ans-100/ai-reviews")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("rev-100"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].confidence").value("HIGH"))
                .andExpect(jsonPath("$[0].disclaimer").exists());
    }

    @Test
    @DisplayName("2. GET /api/analyses/{id}/ai-reviews retrieves analysis reviews (200 OK)")
    public void testGetReviewsSuccess() throws Exception {
        String token = "valid.jwt.token";
        mockAuth(token, "developer@univ.edu", "user-100");

        AIReview review = createSampleReview("rev-101");
        when(aiReviewService.getReviewsForAnalysis("ans-100", "user-100")).thenReturn(List.of(review));

        mockMvc.perform(get("/api/analyses/ans-100/ai-reviews")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("rev-101"));
    }
}
