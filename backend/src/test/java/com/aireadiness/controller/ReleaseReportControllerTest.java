package com.aireadiness.controller;

import com.aireadiness.dto.report.ReleaseReportResponse;
import com.aireadiness.service.ReleaseReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReleaseReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReleaseReportService releaseReportService;

    @InjectMocks
    private ReleaseReportController releaseReportController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(releaseReportController).build();
    }

    @Test
    @DisplayName("GET /api/reports should return all reports for authenticated user")
    void testGetAllUserReportsEndpoint() throws Exception {
        ReleaseReportResponse rep = new ReleaseReportResponse(
                "rep-1", "proj-1", "rel-1", "user-1", "v1.0.0", "REP-REL-1", "Release 1", "COMPLETED", Instant.now(), Instant.now()
        );
        rep.setProjectName("Test Project");
        rep.setReadinessScore(85.0);

        when(releaseReportService.getAllUserReports()).thenReturn(List.of(rep));

        mockMvc.perform(get("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("rep-1"))
                .andExpect(jsonPath("$[0].projectName").value("Test Project"))
                .andExpect(jsonPath("$[0].readinessScore").value(85.0));
    }

    @Test
    @DisplayName("GET /api/projects/{projectId}/reports should return project release reports")
    void testGetProjectReportsEndpoint() throws Exception {
        ReleaseReportResponse rep = new ReleaseReportResponse(
                "rep-1", "proj-1", "rel-1", "user-1", "v1.0.0", "REP-REL-1", "Release 1", "COMPLETED", Instant.now(), Instant.now()
        );

        when(releaseReportService.getProjectReports("proj-1")).thenReturn(List.of(rep));

        mockMvc.perform(get("/api/projects/proj-1/reports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("rep-1"))
                .andExpect(jsonPath("$[0].projectId").value("proj-1"))
                .andExpect(jsonPath("$[0].releaseId").value("rel-1"))
                .andExpect(jsonPath("$[0].version").value("v1.0.0"));
    }

    @Test
    @DisplayName("GET /api/projects/{projectId}/reports/{releaseId} should return specific release report")
    void testGetReleaseReportEndpoint() throws Exception {
        ReleaseReportResponse rep = new ReleaseReportResponse(
                "rep-1", "proj-1", "rel-1", "user-1", "v1.0.0", "REP-REL-1", "Release 1", "COMPLETED", Instant.now(), Instant.now()
        );

        when(releaseReportService.getReleaseReport("proj-1", "rel-1")).thenReturn(rep);

        mockMvc.perform(get("/api/projects/proj-1/reports/rel-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("rep-1"))
                .andExpect(jsonPath("$.projectId").value("proj-1"))
                .andExpect(jsonPath("$.releaseId").value("rel-1"));
    }
}
