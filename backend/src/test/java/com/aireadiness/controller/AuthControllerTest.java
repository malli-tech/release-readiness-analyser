package com.aireadiness.controller;

import com.aireadiness.dto.auth.AuthResponse;
import com.aireadiness.dto.auth.LoginRequest;
import com.aireadiness.dto.auth.MessageResponse;
import com.aireadiness.dto.auth.RegisterRequest;
import com.aireadiness.dto.auth.UserResponse;
import com.aireadiness.exception.EmailAlreadyExistsException;
import com.aireadiness.exception.InvalidCredentialsException;
import com.aireadiness.security.CustomUserDetailsService;
import com.aireadiness.security.JwtAuthFilter;
import com.aireadiness.service.AuthService;
import com.aireadiness.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("1. Successful Registration should return 201 Created")
    public void testSuccessfulRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "Password123");
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new MessageResponse("Registration successful"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    @DisplayName("2. Duplicate Email Registration should return 409 Conflict")
    public void testDuplicateEmailRegistration() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "duplicate@example.com", "Password123");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already registered: duplicate@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email already registered: duplicate@example.com"));
    }

    @Test
    @DisplayName("3. Invalid Registration data should return 400 Bad Request")
    public void testInvalidRegistrationData() throws Exception {
        RegisterRequest request = new RegisterRequest("", "invalid-email", "123"); // blank name, invalid email, short password

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    @Test
    @DisplayName("4. Successful Login should return 200 OK and JWT token")
    public void testSuccessfulLogin() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "Password123");
        UserResponse userResponse = new UserResponse("usr-1", "Test User", "test@example.com", "STUDENT", Instant.now());
        AuthResponse authResponse = new AuthResponse("mock.jwt.token", userResponse);

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"));
    }

    @Test
    @DisplayName("5. Invalid Password during Login should return 401 Unauthorized")
    public void testInvalidPasswordLogin() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    @DisplayName("6. Unknown Email during Login should return 401 Unauthorized")
    public void testUnknownEmailLogin() throws Exception {
        LoginRequest request = new LoginRequest("unknown@example.com", "Password123");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}
