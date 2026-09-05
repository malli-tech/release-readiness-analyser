package com.aireadiness.service;

import com.aireadiness.analyzer.dependency.DependencyAnalyzer;
import com.aireadiness.analyzer.performance.PerformanceAnalyzer;
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
    private final PerformanceAnalyzer performanceAnalyzer;
    private final com.aireadiness.risk.RiskEngine riskEngine;
    private final com.aireadiness.readiness.ReadinessScoreEngine readinessScoreEngine;

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
            SecurityAnalyzer securityAnalyzer,
            PerformanceAnalyzer performanceAnalyzer,
            com.aireadiness.risk.RiskEngine riskEngine,
            com.aireadiness.readiness.ReadinessScoreEngine readinessScoreEngine
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
        this.performanceAnalyzer = performanceAnalyzer;
        this.riskEngine = riskEngine;
        this.readinessScoreEngine = readinessScoreEngine;
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
        List<Finding> rawFindings = new ArrayList<>();

        List<String> completedAnalyzers = new ArrayList<>();
        List<String> failedAnalyzers = new ArrayList<>();
        List<String> skippedAnalyzers = new ArrayList<>();

        // Track PROJECT_DETECTION as completed
        completedAnalyzers.add("PROJECT_DETECTION");

        // 7. Execute Static Code Quality Analyzer (Part 8)
        if (plan.getAnalyzers() != null && plan.getAnalyzers().contains("CODE_QUALITY")) {
            try {
                List<Finding> qualityFindings = codeQualityAnalyzer.analyze(workspacePath, profile, saved.getId(), upload.getUploadMode(), combinedWarnings);
                if (qualityFindings != null) {
                    rawFindings.addAll(qualityFindings);
                }
                completedAnalyzers.add("CODE_QUALITY");
            } catch (Exception e) {
                failedAnalyzers.add("CODE_QUALITY");
                combinedWarnings.add("Code Quality analysis failed: " + e.getMessage());
            }
        } else {
            skippedAnalyzers.add("CODE_QUALITY");
        }

        // 8. Execute Static Testing Analyzer (Part 9)
        if (plan.getAnalyzers() != null && plan.getAnalyzers().contains("TESTING")) {
            try {
                List<Finding> testingFindings = testingAnalyzer.analyze(workspacePath, profile, saved.getId(), upload.getUploadMode(), combinedWarnings);
                if (testingFindings != null) {
                    rawFindings.addAll(testingFindings);
                }
                saved.setTestingSummary(testingAnalyzer.getLastSummary());
                completedAnalyzers.add("TESTING");
            } catch (Exception e) {
                failedAnalyzers.add("TESTING");
                combinedWarnings.add("Testing analysis failed: " + e.getMessage());
            }
        } else {
            skippedAnalyzers.add("TESTING");
        }

        // 9. Execute Static Dependency Analyzer (Part 10)
        if (plan.getAnalyzers() != null && plan.getAnalyzers().contains(dependencyAnalyzer.getType())) {
            try {
                List<Finding> dependencyFindings = dependencyAnalyzer.analyze(workspacePath, profile, saved.getId(), upload.getUploadMode(), combinedWarnings);
                if (dependencyFindings != null) {
                    rawFindings.addAll(dependencyFindings);
                }
                saved.setDependencySummary(dependencyAnalyzer.getLastSummary());
                completedAnalyzers.add(dependencyAnalyzer.getType());
            } catch (Exception e) {
                failedAnalyzers.add(dependencyAnalyzer.getType());
                combinedWarnings.add("Dependency analysis failed: " + e.getMessage());
            }
        } else {
            skippedAnalyzers.add(dependencyAnalyzer.getType());
        }

        // 10. Execute Static Security Analyzer (Part 11)
        if (plan.getAnalyzers() != null && plan.getAnalyzers().contains(securityAnalyzer.getType())) {
            try {
                List<Finding> securityFindings = securityAnalyzer.analyze(workspacePath, profile, saved.getId(), upload.getUploadMode(), combinedWarnings);
                if (securityFindings != null) {
                    rawFindings.addAll(securityFindings);
                }
                saved.setSecuritySummary(securityAnalyzer.getLastSummary());
                completedAnalyzers.add(securityAnalyzer.getType());
            } catch (Exception e) {
                failedAnalyzers.add(securityAnalyzer.getType());
                combinedWarnings.add("Security analysis failed: " + e.getMessage());
            }
        } else {
            skippedAnalyzers.add(securityAnalyzer.getType());
        }

        // 11. Execute Static Performance Analyzer (Part 12)
        if (plan.getAnalyzers() != null && plan.getAnalyzers().contains(performanceAnalyzer.getType())) {
            try {
                List<Finding> performanceFindings = performanceAnalyzer.analyze(workspacePath, profile, saved.getId(), upload.getUploadMode(), combinedWarnings);
                if (performanceFindings != null) {
                    rawFindings.addAll(performanceFindings);
                }
                saved.setPerformanceSummary(performanceAnalyzer.getLastSummary());
                completedAnalyzers.add(performanceAnalyzer.getType());
            } catch (Exception e) {
                failedAnalyzers.add(performanceAnalyzer.getType());
                combinedWarnings.add("Performance analysis failed: " + e.getMessage());
            }
        } else {
            skippedAnalyzers.add(performanceAnalyzer.getType());
        }

        // 12. PART 13 — UNIFIED ANALYSIS
        List<Finding> unifiedFindings = deduplicateAndSortFindings(rawFindings, saved.getId());
        UnifiedAnalysisSummary unifiedSummary = buildUnifiedSummary(
                unifiedFindings,
                profile,
                saved,
                upload.getUploadMode(),
                completedAnalyzers,
                failedAnalyzers,
                skippedAnalyzers,
                combinedWarnings
        );

        // 13. PART 14 — RISK ENGINE
        RiskSummary riskSummary = null;
        try {
            riskSummary = riskEngine.calculateRisk(unifiedSummary, unifiedFindings, profile);
        } catch (Exception e) {
            combinedWarnings.add("Risk calculation failed: " + e.getMessage());
            riskSummary = new RiskSummary();
            riskSummary.setOverallRiskLevel(RiskLevel.UNKNOWN);
            riskSummary.setCompleteness(unifiedSummary != null ? unifiedSummary.getCompleteness() : "UNKNOWN");
            riskSummary.setRiskWarnings(List.of("Risk calculation failed: " + e.getMessage()));
        }

        // 14. PART 15 — READINESS SCORE
        ReadinessScore readinessScore = null;
        try {
            readinessScore = readinessScoreEngine.calculateReadiness(unifiedSummary, riskSummary, profile);
        } catch (Exception e) {
            combinedWarnings.add("Readiness score calculation failed: " + e.getMessage());
            readinessScore = new ReadinessScore();
            readinessScore.setReadinessLevel(ReadinessLevel.UNKNOWN);
            readinessScore.setConfidence(ReadinessConfidence.UNKNOWN);
            readinessScore.setCompleteness(unifiedSummary != null ? unifiedSummary.getCompleteness() : "UNKNOWN");
            readinessScore.setReadinessWarnings(List.of("Readiness score calculation failed: " + e.getMessage()));
        }

        saved.setFindings(unifiedFindings);
        saved.setWarnings(combinedWarnings);
        saved.setUnifiedAnalysisSummary(unifiedSummary);
        saved.setRiskSummary(riskSummary);
        saved.setReadinessScore(readinessScore);
        saved.setStatus("COMPLETED");
        saved.setCompletedAt(Instant.now());

        Analysis finalSaved = analysisRepository.save(saved);

        // Keep release READY_FOR_ANALYSIS until readiness scoring pipeline is completed.
        release.setStatus("READY_FOR_ANALYSIS");
        release.setUpdatedAt(Instant.now());
        releaseRepository.save(release);

        return mapToResponse(finalSaved, "Static project detection, code quality, testing, dependency, security, performance, unified analysis, risk evaluation, and readiness scoring completed successfully.");
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

    private List<Finding> deduplicateAndSortFindings(List<Finding> rawFindings, String analysisId) {
        Set<String> seenKeys = new HashSet<>();
        List<Finding> deduplicated = new ArrayList<>();

        for (Finding f : rawFindings) {
            if (f == null) continue;
            f.setAnalysisId(analysisId);

            // Normalize category
            if (f.getCategory() == null || f.getCategory().trim().isEmpty()) {
                f.setCategory("CODE_QUALITY");
            } else if ("DEPENDENCIES".equalsIgnoreCase(f.getCategory())) {
                f.setCategory("DEPENDENCY");
            } else {
                f.setCategory(f.getCategory().toUpperCase());
            }

            // Normalize severity
            if (f.getSeverity() == null || f.getSeverity().trim().isEmpty()) {
                f.setSeverity("MEDIUM");
            } else {
                String sev = f.getSeverity().toUpperCase();
                if ("CRITICAL".equals(sev)) {
                    sev = "HIGH";
                }
                f.setSeverity(sev);
            }

            String ruleId = f.getRuleId() != null ? f.getRuleId() : "";
            String filePath = f.getFilePath() != null ? f.getFilePath() : "";
            int line = f.getLineNumber() != null ? f.getLineNumber() : 0;
            int evidenceHash = f.getEvidence() != null ? f.getEvidence().hashCode() : 0;

            String key = analysisId + ":" + ruleId + ":" + filePath + ":" + line + ":" + evidenceHash;
            if (seenKeys.add(key)) {
                deduplicated.add(f);
            }
        }

        deduplicated.sort((f1, f2) -> {
            int r1 = getSeverityRank(f1.getSeverity());
            int r2 = getSeverityRank(f2.getSeverity());
            if (r1 != r2) return Integer.compare(r1, r2);

            int catComp = Objects.toString(f1.getCategory(), "").compareTo(Objects.toString(f2.getCategory(), ""));
            if (catComp != 0) return catComp;

            int pathComp = Objects.toString(f1.getFilePath(), "").compareTo(Objects.toString(f2.getFilePath(), ""));
            if (pathComp != 0) return pathComp;

            int line1 = f1.getLineNumber() != null ? f1.getLineNumber() : 0;
            int line2 = f2.getLineNumber() != null ? f2.getLineNumber() : 0;
            if (line1 != line2) return Integer.compare(line1, line2);

            return Objects.toString(f1.getRuleId(), "").compareTo(Objects.toString(f2.getRuleId(), ""));
        });

        return deduplicated;
    }

    private int getSeverityRank(String severity) {
        if (severity == null) return 2;
        switch (severity.toUpperCase()) {
            case "HIGH":
                return 1;
            case "MEDIUM":
                return 2;
            case "LOW":
                return 3;
            case "INFO":
                return 4;
            default:
                return 5;
        }
    }

    private UnifiedAnalysisSummary buildUnifiedSummary(
            List<Finding> unifiedFindings,
            ProjectProfile profile,
            Analysis analysis,
            String uploadMode,
            List<String> completedAnalyzers,
            List<String> failedAnalyzers,
            List<String> skippedAnalyzers,
            List<String> warnings
    ) {
        int high = 0, medium = 0, low = 0, info = 0;
        Map<String, Integer> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("CODE_QUALITY", 0);
        categoryCounts.put("TESTING", 0);
        categoryCounts.put("DEPENDENCY", 0);
        categoryCounts.put("SECURITY", 0);
        categoryCounts.put("PERFORMANCE", 0);

        Map<String, Integer> severityCounts = new LinkedHashMap<>();
        severityCounts.put("HIGH", 0);
        severityCounts.put("MEDIUM", 0);
        severityCounts.put("LOW", 0);
        severityCounts.put("INFO", 0);

        Set<String> affectedFilesSet = new HashSet<>();

        for (Finding f : unifiedFindings) {
            String sev = f.getSeverity();
            if ("HIGH".equalsIgnoreCase(sev)) {
                high++;
                severityCounts.put("HIGH", severityCounts.getOrDefault("HIGH", 0) + 1);
            } else if ("MEDIUM".equalsIgnoreCase(sev)) {
                medium++;
                severityCounts.put("MEDIUM", severityCounts.getOrDefault("MEDIUM", 0) + 1);
            } else if ("LOW".equalsIgnoreCase(sev)) {
                low++;
                severityCounts.put("LOW", severityCounts.getOrDefault("LOW", 0) + 1);
            } else if ("INFO".equalsIgnoreCase(sev)) {
                info++;
                severityCounts.put("INFO", severityCounts.getOrDefault("INFO", 0) + 1);
            }

            String cat = f.getCategory();
            if (cat != null) {
                categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
            }

            if (f.getFilePath() != null && !f.getFilePath().trim().isEmpty()) {
                affectedFilesSet.add(f.getFilePath().trim());
            }
        }

        int analyzedFiles = 0;
        if (profile != null && profile.getProjectStructure() != null) {
            analyzedFiles = profile.getProjectStructure().getSourceFileCount();
        }
        if (analyzedFiles == 0 && analysis.getPerformanceSummary() != null) {
            analyzedFiles = analysis.getPerformanceSummary().getAnalyzedSourceFiles();
        }

        String completeness = "UNKNOWN";
        if (profile != null && "UNKNOWN".equalsIgnoreCase(profile.getPrimaryLanguage()) && "UNKNOWN".equalsIgnoreCase(profile.getProjectType())) {
            completeness = "UNKNOWN";
        } else if ("SELECTED_CONTENT".equalsIgnoreCase(uploadMode)) {
            completeness = "PARTIAL";
            if (!warnings.contains("Unified analysis is based only on the selected uploaded content. Additional findings may exist in files that were not uploaded.")) {
                warnings.add("Unified analysis is based only on the selected uploaded content. Additional findings may exist in files that were not uploaded.");
            }
        } else if (!failedAnalyzers.isEmpty()) {
            completeness = "PARTIAL";
        } else if (analyzedFiles > 0 || !unifiedFindings.isEmpty()) {
            completeness = "COMPLETE";
        }

        UnifiedAnalysisSummary summary = new UnifiedAnalysisSummary();
        summary.setTotalFindings(unifiedFindings.size());
        summary.setHighFindings(high);
        summary.setMediumFindings(medium);
        summary.setLowFindings(low);
        summary.setInfoFindings(info);
        summary.setFindingsByCategory(categoryCounts);
        summary.setFindingsBySeverity(severityCounts);
        summary.setAffectedFiles(affectedFilesSet.size());
        summary.setAnalyzedFiles(analyzedFiles);
        summary.setCompletedAnalyzers(completedAnalyzers);
        summary.setFailedAnalyzers(failedAnalyzers);
        summary.setSkippedAnalyzers(skippedAnalyzers);
        summary.setCompleteness(completeness);
        summary.setWarnings(new ArrayList<>(warnings));

        return summary;
    }

    private AnalysisResponse mapToResponse(Analysis analysis, String message) {
        AnalysisResponse response = new AnalysisResponse(
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
                analysis.getPerformanceSummary(),
                message
        );
        response.setUnifiedAnalysisSummary(analysis.getUnifiedAnalysisSummary());
        response.setRiskSummary(analysis.getRiskSummary());
        return response;
    }
}
