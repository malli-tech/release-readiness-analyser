package com.aireadiness.service;

import com.aireadiness.dto.report.ReleaseReportResponse;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.Analysis;
import com.aireadiness.model.Project;
import com.aireadiness.model.ReadinessLevel;
import com.aireadiness.model.ReadinessScore;
import com.aireadiness.model.Release;
import com.aireadiness.model.RiskLevel;
import com.aireadiness.model.RiskSummary;
import com.aireadiness.model.User;
import com.aireadiness.model.report.ReleaseReport;
import com.aireadiness.repository.AnalysisRepository;
import com.aireadiness.repository.ProjectRepository;
import com.aireadiness.repository.ReleaseRepository;
import com.aireadiness.repository.ReleaseReportRepository;
import com.aireadiness.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseReportServiceTest {

    @Mock
    private ReleaseReportRepository releaseReportRepository;

    @Mock
    private ReleaseRepository releaseRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReleaseReportService releaseReportService;

    private User testUser;
    private Project testProject;
    private Release testRelease;

    private void setupAuthentication() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user@example.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-100");
        testUser.setEmail("user@example.com");

        testProject = new Project();
        testProject.setId("proj-1");
        testProject.setName("E-Commerce App");
        testProject.setUserId("user-100");

        testRelease = new Release();
        testRelease.setId("rel-1");
        testRelease.setProjectId("proj-1");
        testRelease.setUserId("user-100");
        testRelease.setVersion("v1.0.0");
        testRelease.setName("Initial Release");
        testRelease.setStatus("COMPLETED");

        setupAuthentication();
        lenient().when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("Should return all release reports across user projects with populated analysis metrics")
    void testGetAllUserReports() {
        when(projectRepository.findByUserIdOrderByCreatedAtDesc("user-100")).thenReturn(List.of(testProject));
        when(releaseRepository.findByProjectIdAndUserIdOrderByCreatedAtDesc("proj-1", "user-100"))
                .thenReturn(List.of(testRelease));

        Analysis mockAnalysis = new Analysis();
        mockAnalysis.setId("ans-1");
        mockAnalysis.setStatus("COMPLETED");

        ReadinessScore score = new ReadinessScore();
        score.setReadinessScore(java.math.BigDecimal.valueOf(88.5));
        score.setReadinessLevel(ReadinessLevel.GOOD);
        mockAnalysis.setReadinessScore(score);

        RiskSummary risk = new RiskSummary();
        risk.setOverallRiskLevel(RiskLevel.LOW);
        mockAnalysis.setRiskSummary(risk);

        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc("rel-1", "user-100"))
                .thenReturn(Optional.of(mockAnalysis));

        when(releaseReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ReleaseReportResponse> reports = releaseReportService.getAllUserReports();

        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals("E-Commerce App", reports.get(0).getProjectName());
        assertEquals(88.5, reports.get(0).getReadinessScore());
        assertEquals("GOOD", reports.get(0).getReadinessLevel());
        assertEquals("LOW", reports.get(0).getRiskLevel());
    }

    @Test
    @DisplayName("Should handle projects with null names safely without throwing NullPointerException")
    void testGetAllUserReportsWithNullProjectName() {
        Project nullNameProject = new Project();
        nullNameProject.setId("proj-null-name");
        nullNameProject.setName(null);
        nullNameProject.setUserId("user-100");

        Release nullNameRelease = new Release();
        nullNameRelease.setId("rel-null-proj");
        nullNameRelease.setProjectId("proj-null-name");
        nullNameRelease.setUserId("user-100");
        nullNameRelease.setVersion("v1.0.0");
        nullNameRelease.setName("Release for Null Name Project");

        when(projectRepository.findByUserIdOrderByCreatedAtDesc("user-100")).thenReturn(List.of(testProject, nullNameProject));
        when(releaseRepository.findByProjectIdAndUserIdOrderByCreatedAtDesc("proj-1", "user-100")).thenReturn(List.of(testRelease));
        when(releaseRepository.findByProjectIdAndUserIdOrderByCreatedAtDesc("proj-null-name", "user-100")).thenReturn(List.of(nullNameRelease));

        when(releaseReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ReleaseReportResponse> reports = releaseReportService.getAllUserReports();

        assertNotNull(reports);
        assertEquals(2, reports.size());
        assertEquals("E-Commerce App", reports.get(0).getProjectName());
        assertEquals("Untitled Project", reports.get(1).getProjectName());
    }

    @Test
    @DisplayName("Should return reports belonging only to specified project")
    void testGetProjectReports() {
        when(projectRepository.findByIdAndUserId("proj-1", "user-100")).thenReturn(Optional.of(testProject));
        when(releaseRepository.findByProjectIdAndUserIdOrderByCreatedAtDesc("proj-1", "user-100"))
                .thenReturn(List.of(testRelease));

        ReleaseReport mockReport = new ReleaseReport("proj-1", "rel-1", "user-100", "v1.0.0", "REP-REL-1", "Initial Release", "COMPLETED");
        mockReport.setId("rep-1");

        when(releaseReportRepository.findByProjectIdAndReleaseIdAndUserId("proj-1", "rel-1", "user-100"))
                .thenReturn(Optional.of(mockReport));

        List<ReleaseReportResponse> reports = releaseReportService.getProjectReports("proj-1");

        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals("rep-1", reports.get(0).getId());
        assertEquals("proj-1", reports.get(0).getProjectId());
        assertEquals("rel-1", reports.get(0).getReleaseId());
        assertEquals("v1.0.0", reports.get(0).getVersion());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when requesting unowned project reports")
    void testGetProjectReportsUnowned() {
        when(projectRepository.findByIdAndUserId("proj-other", "user-100")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> releaseReportService.getProjectReports("proj-other"));
    }

    @Test
    @DisplayName("Should return release report for valid project and release")
    void testGetReleaseReport() {
        when(projectRepository.findByIdAndUserId("proj-1", "user-100")).thenReturn(Optional.of(testProject));
        when(releaseRepository.findByIdAndUserId("rel-1", "user-100")).thenReturn(Optional.of(testRelease));

        ReleaseReport mockReport = new ReleaseReport("proj-1", "rel-1", "user-100", "v1.0.0", "REP-REL-1", "Initial Release", "COMPLETED");
        mockReport.setId("rep-1");

        when(releaseReportRepository.findByProjectIdAndReleaseIdAndUserId("proj-1", "rel-1", "user-100"))
                .thenReturn(Optional.of(mockReport));

        ReleaseReportResponse response = releaseReportService.getReleaseReport("proj-1", "rel-1");

        assertNotNull(response);
        assertEquals("rep-1", response.getId());
        assertEquals("rel-1", response.getReleaseId());
        assertEquals("proj-1", response.getProjectId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when release does not belong to project")
    void testGetReleaseReportMismatchedProject() {
        Release otherRelease = new Release();
        otherRelease.setId("rel-2");
        otherRelease.setProjectId("proj-other");
        otherRelease.setUserId("user-100");

        when(projectRepository.findByIdAndUserId("proj-1", "user-100")).thenReturn(Optional.of(testProject));
        when(releaseRepository.findByIdAndUserId("rel-2", "user-100")).thenReturn(Optional.of(otherRelease));

        assertThrows(ResourceNotFoundException.class, () -> releaseReportService.getReleaseReport("proj-1", "rel-2"));
    }

    @Test
    @DisplayName("Should return NOT_ANALYZED report with null metrics for unanalyzed release")
    void testGetReleaseReportUnanalyzedRelease() {
        Release unanalyzedRelease = new Release();
        unanalyzedRelease.setId("rel-unanalyzed");
        unanalyzedRelease.setProjectId("proj-1");
        unanalyzedRelease.setUserId("user-100");
        unanalyzedRelease.setVersion("v0.1.0");
        unanalyzedRelease.setName("Unanalyzed Release");
        unanalyzedRelease.setStatus("READY_FOR_ANALYSIS");

        when(projectRepository.findByIdAndUserId("proj-1", "user-100")).thenReturn(Optional.of(testProject));
        when(releaseRepository.findByIdAndUserId("rel-unanalyzed", "user-100")).thenReturn(Optional.of(unanalyzedRelease));
        when(analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc("rel-unanalyzed", "user-100"))
                .thenReturn(Optional.empty());
        when(releaseReportRepository.findByProjectIdAndReleaseIdAndUserId("proj-1", "rel-unanalyzed", "user-100"))
                .thenReturn(Optional.empty());
        when(releaseReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReleaseReportResponse response = releaseReportService.getReleaseReport("proj-1", "rel-unanalyzed");

        assertNotNull(response);
        assertEquals("NOT_ANALYZED", response.getStatus());
        assertNull(response.getReadinessScore());
        assertNull(response.getReadinessLevel());
        assertNull(response.getRiskLevel());
        assertNull(response.getLatestAnalysisId());
        assertEquals("E-Commerce App", response.getProjectName());
    }

    @Test
    @DisplayName("Should update existing report without creating duplicate documents on repeated calls")
    void testDuplicateReleaseReportPrevention() {
        when(projectRepository.findByIdAndUserId("proj-1", "user-100")).thenReturn(Optional.of(testProject));
        when(releaseRepository.findByIdAndUserId("rel-1", "user-100")).thenReturn(Optional.of(testRelease));

        ReleaseReport existingReport = new ReleaseReport("proj-1", "rel-1", "user-100", "v1.0.0", "REP-REL-1", "Initial Release", "COMPLETED");
        existingReport.setId("rep-existing-99");

        when(releaseReportRepository.findByProjectIdAndReleaseIdAndUserId("proj-1", "rel-1", "user-100"))
                .thenReturn(Optional.of(existingReport));
        lenient().when(releaseReportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReleaseReportResponse response1 = releaseReportService.getReleaseReport("proj-1", "rel-1");
        ReleaseReportResponse response2 = releaseReportService.getReleaseReport("proj-1", "rel-1");

        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals("rep-existing-99", response1.getId());
        assertEquals("rep-existing-99", response2.getId());
        verify(releaseReportRepository, never()).save(any());
    }
}
