package com.aireadiness.controller;

import com.aireadiness.dto.auth.UserResponse;
import com.aireadiness.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("7. Protected endpoint GET /api/users/me without JWT should return 403 Forbidden")
    public void testProtectedEndpointWithoutJwt() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("8. Protected endpoint GET /api/users/me with valid JWT should return 200 OK and User info")
    public void testProtectedEndpointWithValidJwt() throws Exception {
        String token = "valid.jwt.token";
        String email = "test@example.com";
        UserDetails userDetails = new User(email, "password", Collections.emptyList());

        when(jwtService.extractUsername(token)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(eq(token), any(UserDetails.class))).thenReturn(true);

        UserResponse userResponse = new UserResponse("usr-1", "Test User", email, "STUDENT", Instant.now());
        when(authService.getCurrentUser(email)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("9. Protected endpoint GET /api/users/me with invalid JWT should return 403 Forbidden")
    public void testProtectedEndpointWithInvalidJwt() throws Exception {
        String invalidToken = "invalid.jwt.token";
        when(jwtService.extractUsername(invalidToken)).thenThrow(new RuntimeException("Invalid token"));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isForbidden());
    }
}
