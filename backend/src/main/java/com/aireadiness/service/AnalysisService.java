package com.aireadiness.service;

import com.aireadiness.analyzer.dependency.DependencyAnalyzer;
import com.aireadiness.analyzer.quality.CodeQualityAnalyzer;
import com.aireadiness.analyzer.security.SecurityAnalyzer;
import com.aireadiness.analyzer.testing.TestingAnalyzer;
import com.aireadiness.dto.analysis.AnalysisResponse;
import com.aireadiness.exception.InvalidArchiveException;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.*;
import com.aireadiness.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Service
public class AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final UploadRepository uploadRepository;
    private final UserRepository userRepository;
    private final WorkspaceService workspaceService;
    private final ProjectDetectionService projectDetectionService;
    private final CodeQualityAnalyzer codeQualityAnalyzer;
    private final TestingAnalyzer testingAnalyzer;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final SecurityAnalyzer securityAnalyzer;

    public AnalysisService(
            AnalysisRepository analysisRepository,
            ReleaseRepository releaseRepository,
            ProjectRepository projectRepository,
            UploadRepository uploadRepository,
            UserRepository userRepository,
            WorkspaceService workspaceService,
            ProjectDetectionService projectDetectionService,
            CodeQualityAnalyzer codeQualityAnalyzer,
            TestingAnalyzer testingAnalyzer,
            DependencyAnalyzer dependencyAnalyzer,
            SecurityAnalyzer securityAnalyzer
    ) {
        this.analysisRepository = analysisRepository;
        this.releaseRepository = releaseRepository;
        this.projectRepository = projectRepository;
        this.uploadRepository = uploadRepository;
        this.userRepository = userRepository;
        this.workspaceService = workspaceService;
        this.projectDetectionService = projectDetectionService;
        this.codeQualityAnalyzer = codeQualityAnalyzer;
        this.testingAnalyzer = testingAnalyzer;
        this.dependencyAnalyzer = dependencyAnalyzer;
        this.securityAnalyzer = securityAnalyzer;
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new UsernameNotFoundException("Unauthenticated user");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    public AnalysisResponse startAnalysis(String releaseId) {
        User user = getAuthenticatedUser();

        // 1. Ownership & Release verification
        Release release = releaseRepository.findByIdAndUserId(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        Project project = projectRepository.findByIdAndUserId(release.getProjectId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + release.getProjectId()));

        // 2. Upload state & workspace verification
        UploadMetadata upload = uploadRepository.findFirstByReleaseIdAndUserIdOrderByUploadedAtDesc(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No upload found for release: " + releaseId));

        if (!"READY".equalsIgnoreCase(upload.getStatus())) {
            throw new InvalidArchiveException("Upload is not in READY status for release: " + releaseId);
        }

        Path workspacePath = workspaceService.getWorkspacePath(upload.getWorkspaceId());

        // 3. Static Project Detection
        ProjectProfile profile = projectDetectionService.detectProject(workspacePath, upload.getUploadMode());

        // 4. Analysis Plan Generation
        List<String> targetAnalyzers = List.of("CODE_QUALITY", "TESTING", "DEPENDENCIES", "SECURITY", "PERFORMANCE");
        Map<String, String> rationale = new HashMap<>();
        rationale.put("CODE_QUALITY", "Evaluates maintainability, complexity, and coding standards.");
        rationale.put("TESTING", "Assesses test coverage, assertions, and test structure.");
        rationale.put("DEPENDENCIES", "Scans manifest dependencies for updates and version alignment.");
        rationale.put("SECURITY", "Performs static vulnerability pattern checks.");
        rationale.put("PERFORMANCE", "Analyzes configuration and concurrency performance indicators.");
        AnalysisPlan plan = new AnalysisPlan(targetAnalyzers, rationale);

        // 5. Idempotency / Run Number calculation
        Optional<Analysis> latestAnalysisOpt = analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(releaseId, user.getId());
        int nextRunNumber = latestAnalysisOpt.map(a -> a.getRunNumber() + 1).orElse(1);

        // 6. Build initial Analysis record
        Analysis analysis = new Analysis(project.getId(), release.getId(), user.getId(), nextRunNumber, "ANALYZING");
        analysis.setProjectProfile(profile);
        analysis.setAnalysisPlan(plan);
        Analysis saved = analysisRepository.save(analysis);

        List<String> combinedWarnings = new ArrayList<>(profile.getDetectionWarnings());
        List<Finding> allFindings = new ArrayList<>();

        // 7. Execute Static Code Quality Analyzer (Part 8)
        if (plan.getAnalyzers() != null && plan.getAnalyzers().contains("CODE_QUALITY")) {
            List<Finding> qualityFindings = codeQualityAnalyzer.analyze(workspacePath, profile, saved.getId(), upload.getUploadMode(), combinedWarnings);
            if (qualityFindings != null) {
                allFindings.addAll(qualityFindings);
            }
        }

        // 8. Execute Static Testing Analyzer (Part 9)
        if (plan.getAnalyzers() != null && plan.getAnalyzers().contains("TESTING")) {
            List<Finding> testingFindings = testingAnalyzer.analyze(workspacePath, profile, saved.getId(), upload.getUploadMode(), combinedWarnings);
            if (testingFindings != null) {
                allFindings.addAll(testingFindings);
            }
            saved.setTestingSummary(testingAnalyzer.getLastSummary());
        }

        // 9. Execute Static Dependency Analyzer (Part 10)
        if (plan.getAnalyzers() != null && plan.getAnalyzers().contains(dependencyAnalyzer.getType())) {
            List<Finding> dependencyFindings = dependencyAnalyzer.analyze(workspacePath, profile, saved.getId(), upload.getUploadMode(), combinedWarnings);
            if (dependencyFindings != null) {
                allFindings.addAll(dependencyFindings);
            }
            saved.setDependencySummary(dependencyAnalyzer.getLastSummary());
        }

        // 10. Execute Static Security Analyzer (Part 11)
        if (plan.getAnalyzers() != null && plan.getAnalyzers().contains(securityAnalyzer.getType())) {
            List<Finding> securityFindings = securityAnalyzer.analyze(workspacePath, profile, saved.getId(), upload.getUploadMode(), combinedWarnings);
            if (securityFindings != null) {
                allFindings.addAll(securityFindings);
            }
            saved.setSecuritySummary(securityAnalyzer.getLastSummary());
        }

        allFindings.forEach(f -> f.setAnalysisId(saved.getId()));
        saved.setFindings(allFindings);
        saved.setWarnings(combinedWarnings);
        saved.setStatus("COMPLETED");
        saved.setCompletedAt(Instant.now());

        Analysis finalSaved = analysisRepository.save(saved);

        // Keep release READY_FOR_ANALYSIS until the complete analysis pipeline is implemented.
        release.setStatus("READY_FOR_ANALYSIS");
        release.setUpdatedAt(Instant.now());
        releaseRepository.save(release);

        return mapToResponse(finalSaved, "Static project detection, code quality, testing, dependency, and security analysis completed successfully.");
    }

    public AnalysisResponse getAnalysisById(String analysisId) {
        User user = getAuthenticatedUser();
        Analysis analysis = analysisRepository.findByIdAndUserId(analysisId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + analysisId));
        return mapToResponse(analysis, "Analysis record retrieved successfully.");
    }

    public AnalysisResponse getLatestAnalysisForRelease(String releaseId) {
        User user = getAuthenticatedUser();
        // Verify release ownership
        releaseRepository.findByIdAndUserId(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        Analysis analysis = analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No analysis found for release: " + releaseId));

        return mapToResponse(analysis, "Latest analysis record retrieved successfully.");
    }

    private AnalysisResponse mapToResponse(Analysis analysis, String message) {
        return new AnalysisResponse(
                analysis.getId(),
                analysis.getProjectId(),
                analysis.getReleaseId(),
                analysis.getRunNumber(),
                analysis.getStatus(),
                analysis.getStartedAt(),
                analysis.getCompletedAt(),
                analysis.getProjectProfile(),
                analysis.getAnalysisPlan(),
                analysis.getFindings(),
                analysis.getCategoryScores(),
                analysis.getReadinessScore(),
                analysis.getWarnings(),
                analysis.getTestingSummary(),
                analysis.getDependencySummary(),
                analysis.getSecuritySummary(),
                message
        );
    }
}
