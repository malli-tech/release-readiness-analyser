package com.aireadiness.service;

import com.aireadiness.analyzer.dependency.DependencyAnalyzer;
import com.aireadiness.analyzer.performance.PerformanceAnalyzer;
import com.aireadiness.analyzer.quality.CodeQualityAnalyzer;
import com.aireadiness.analyzer.security.SecurityAnalyzer;
import com.aireadiness.analyzer.testing.TestingAnalyzer;
import com.aireadiness.dto.analysis.AnalysisResponse;
import com.aireadiness.model.*;
import com.aireadiness.repository.AnalysisRepository;
import com.aireadiness.repository.ProjectRepository;
import com.aireadiness.repository.ReleaseRepository;
import com.aireadiness.repository.UploadRepository;
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

import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnifiedAnalysisServiceTest {

    @Mock private AnalysisRepository analysisRepository;
    @Mock private ReleaseRepository releaseRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UploadRepository uploadRepository;
    @Mock private UserRepository userRepository;
    @Mock private WorkspaceService workspaceService;
    @Mock private ProjectDetectionService projectDetectionService;
    @Mock private CodeQualityAnalyzer codeQualityAnalyzer;
    @Mock private TestingAnalyzer testingAnalyzer;
    @Mock private DependencyAnalyzer dependencyAnalyzer;
    @Mock private SecurityAnalyzer securityAnalyzer;
    @Mock private PerformanceAnalyzer performanceAnalyzer;
    @org.mockito.Spy private com.aireadiness.risk.RiskEngine riskEngine = new com.aireadiness.risk.RiskEngine();

    @InjectMocks
    private AnalysisService analysisService;

    private User testUser;
    private Project testProject;
    private Release testRelease;
    private UploadMetadata testUpload;

    private void setupAuthentication() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "test@example.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-1");
        testUser.setEmail("test@example.com");

        testProject = new Project();
        testProject.setId("proj-1");
        testProject.setName("Test App");
        testProject.setUserId("user-1");

        testRelease = new Release();
        testRelease.setId("rel-1");
        testRelease.setProjectId("proj-1");
        testRelease.setVersion("v1.0.0");
        testRelease.setUserId("user-1");

        testUpload = new UploadMetadata();
        testUpload.setReleaseId("rel-1");
        testUpload.setUserId("user-1");
        testUpload.setWorkspaceId("ws-1");
        testUpload.setStatus("READY");
        testUpload.setUploadMode("COMPLETE_PROJECT");

        setupAuthentication();

        lenient().when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        lenient().when(releaseRepository.findByIdAndUserId("rel-1", "user-1")).thenReturn(Optional.of(testRelease));
        lenient().when(projectRepository.findByIdAndUserId("proj-1", "user-1")).thenReturn(Optional.of(testProject));
        lenient().when(uploadRepository.findFirstByReleaseIdAndUserIdOrderByUploadedAtDesc("rel-1", "user-1")).thenReturn(Optional.of(testUpload));
        lenient().when(workspaceService.getWorkspacePath("ws-1")).thenReturn(Path.of("/tmp/ws-1"));

        lenient().when(dependencyAnalyzer.getType()).thenReturn("DEPENDENCIES");
        lenient().when(securityAnalyzer.getType()).thenReturn("SECURITY");
        lenient().when(performanceAnalyzer.getType()).thenReturn("PERFORMANCE");
    }

    @Test
    @DisplayName("Should aggregate findings across all 5 static analyzers into UnifiedAnalysisSummary")
    void testUnifiedAnalysisAggregatesAllAnalyzers() {
        setupAuthentication();
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        profile.setProjectType("Maven");
        ProjectStructure structure = new ProjectStructure();
        structure.setSourceFileCount(10);
        profile.setProjectStructure(structure);

        when(projectDetectionService.detectProject(any(), any())).thenReturn(profile);

        Finding f1 = createFinding("CODE_QUALITY", "QUALITY_LONG_METHOD", "MEDIUM", "UserService.java", 15);
        Finding f2 = createFinding("TESTING", "TESTING_NO_TESTS", "HIGH", "OrderService.java", 1);
        Finding f3 = createFinding("DEPENDENCY", "DEPENDENCY_UNPINNED", "LOW", "pom.xml", 20);
        Finding f4 = createFinding("SECURITY", "SECURITY_HARDCODED_SECRET", "HIGH", "UserService.java", 45);
        Finding f5 = createFinding("PERFORMANCE", "PERFORMANCE_N_PLUS_ONE_QUERY", "HIGH", "OrderService.java", 100);

        when(codeQualityAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(f1));
        when(testingAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(f2));
        when(dependencyAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(f3));
        when(securityAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(f4));
        when(performanceAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(f5));

        when(analysisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisResponse response = analysisService.startAnalysis("rel-1");

        assertNotNull(response);
        assertNotNull(response.getUnifiedAnalysisSummary());

        UnifiedAnalysisSummary summary = response.getUnifiedAnalysisSummary();
        assertEquals(5, summary.getTotalFindings());
        assertEquals(3, summary.getHighFindings()); // f2, f4, f5
        assertEquals(1, summary.getMediumFindings()); // f1
        assertEquals(1, summary.getLowFindings()); // f3
        assertEquals(0, summary.getInfoFindings());

        assertEquals(5, summary.getFindingsByCategory().values().stream().mapToInt(Integer::intValue).sum());
        assertEquals(5, summary.getFindingsBySeverity().values().stream().mapToInt(Integer::intValue).sum());

        assertEquals(3, summary.getAffectedFiles()); // UserService.java, OrderService.java, pom.xml
        assertEquals("COMPLETE", summary.getCompleteness());

        assertTrue(summary.getCompletedAnalyzers().containsAll(List.of("PROJECT_DETECTION", "CODE_QUALITY", "TESTING", "DEPENDENCIES", "SECURITY", "PERFORMANCE")));
        assertTrue(summary.getFailedAnalyzers().isEmpty());
    }

    @Test
    @DisplayName("Should deduplicate identical findings across analysis execution")
    void testUnifiedAnalysisDeduplication() {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        when(projectDetectionService.detectProject(any(), any())).thenReturn(profile);

        Finding f1 = createFinding("SECURITY", "SECURITY_HARDCODED_SECRET", "HIGH", "Config.java", 10);
        Finding f2 = createFinding("SECURITY", "SECURITY_HARDCODED_SECRET", "HIGH", "Config.java", 10); // Duplicate of f1

        when(securityAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(f1, f2));
        when(analysisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisResponse response = analysisService.startAnalysis("rel-1");

        assertEquals(1, response.getFindings().size());
        assertEquals(1, response.getUnifiedAnalysisSummary().getTotalFindings());
    }

    @Test
    @DisplayName("Should maintain fault tolerance and preserve successful findings when one analyzer fails")
    void testAnalyzerFailureFaultTolerance() {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        when(projectDetectionService.detectProject(any(), any())).thenReturn(profile);

        Finding f1 = createFinding("CODE_QUALITY", "QUALITY_LONG_METHOD", "MEDIUM", "Main.java", 10);
        when(codeQualityAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(f1));
        when(testingAnalyzer.analyze(any(), any(), any(), any(), any())).thenThrow(new RuntimeException("Testing engine crash"));

        when(analysisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisResponse response = analysisService.startAnalysis("rel-1");

        assertNotNull(response);
        UnifiedAnalysisSummary summary = response.getUnifiedAnalysisSummary();
        assertEquals(1, summary.getTotalFindings());
        assertTrue(summary.getCompletedAnalyzers().contains("CODE_QUALITY"));
        assertTrue(summary.getFailedAnalyzers().contains("TESTING"));
        assertEquals("PARTIAL", summary.getCompleteness());
        assertTrue(summary.getWarnings().stream().anyMatch(w -> w.contains("Testing analysis failed")));
    }

    @Test
    @DisplayName("Should enforce SELECTED_CONTENT PARTIAL completeness semantics with warning")
    void testSelectedContentCompletenessSemantics() {
        testUpload.setUploadMode("SELECTED_CONTENT");

        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("TypeScript");
        when(projectDetectionService.detectProject(any(), any())).thenReturn(profile);

        when(analysisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisResponse response = analysisService.startAnalysis("rel-1");

        UnifiedAnalysisSummary summary = response.getUnifiedAnalysisSummary();
        assertEquals("PARTIAL", summary.getCompleteness());
        assertTrue(summary.getWarnings().stream().anyMatch(w -> w.contains("selected uploaded content")));
    }

    @Test
    @DisplayName("Should enforce UNKNOWN completeness for unsupported projects")
    void testUnsupportedProjectCompleteness() {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("UNKNOWN");
        profile.setProjectType("UNKNOWN");
        when(projectDetectionService.detectProject(any(), any())).thenReturn(profile);

        when(analysisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisResponse response = analysisService.startAnalysis("rel-1");

        UnifiedAnalysisSummary summary = response.getUnifiedAnalysisSummary();
        assertEquals("UNKNOWN", summary.getCompleteness());
        assertEquals(0, summary.getTotalFindings());
    }

    @Test
    @DisplayName("Should sort unified findings deterministically by Severity -> Category -> Path -> Line -> Rule")
    void testDeterministicFindingOrder() {
        ProjectProfile profile = new ProjectProfile();
        profile.setPrimaryLanguage("Java");
        when(projectDetectionService.detectProject(any(), any())).thenReturn(profile);

        Finding lowFinding = createFinding("CODE_QUALITY", "RULE_LOW", "LOW", "A.java", 10);
        Finding highFinding = createFinding("SECURITY", "RULE_HIGH", "HIGH", "B.java", 5);
        Finding medFinding2 = createFinding("PERFORMANCE", "RULE_MED2", "MEDIUM", "B.java", 10);
        Finding medFinding1 = createFinding("CODE_QUALITY", "RULE_MED1", "MEDIUM", "A.java", 1);

        when(codeQualityAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(lowFinding, medFinding1));
        when(securityAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(highFinding));
        when(performanceAnalyzer.analyze(any(), any(), any(), any(), any())).thenReturn(List.of(medFinding2));

        when(analysisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisResponse response = analysisService.startAnalysis("rel-1");

        List<Finding> findings = response.getFindings();
        assertEquals(4, findings.size());

        // 1st: HIGH severity
        assertEquals("HIGH", findings.get(0).getSeverity());
        assertEquals("SECURITY", findings.get(0).getCategory());

        // 2nd: MEDIUM severity (CODE_QUALITY comes before PERFORMANCE alphabetically)
        assertEquals("MEDIUM", findings.get(1).getSeverity());
        assertEquals("CODE_QUALITY", findings.get(1).getCategory());

        // 3rd: MEDIUM severity (PERFORMANCE)
        assertEquals("MEDIUM", findings.get(2).getSeverity());
        assertEquals("PERFORMANCE", findings.get(2).getCategory());

        // 4th: LOW severity
        assertEquals("LOW", findings.get(3).getSeverity());
    }

    private Finding createFinding(String category, String ruleId, String severity, String filePath, int lineNumber) {
        Finding f = new Finding();
        f.setCategory(category);
        f.setRuleId(ruleId);
        f.setSeverity(severity);
        f.setFilePath(filePath);
        f.setLineNumber(lineNumber);
        f.setTitle("Finding " + ruleId);
        f.setDescription("Description for " + ruleId);
        f.setEvidence("Evidence code snippet for " + ruleId);
        return f;
    }
}
