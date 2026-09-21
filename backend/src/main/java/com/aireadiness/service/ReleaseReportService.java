package com.aireadiness.service;

import com.aireadiness.dto.report.ReleaseReportResponse;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.Analysis;
import com.aireadiness.model.Project;
import com.aireadiness.model.Release;
import com.aireadiness.model.User;
import com.aireadiness.model.report.ReleaseReport;
import com.aireadiness.repository.AnalysisRepository;
import com.aireadiness.repository.ProjectRepository;
import com.aireadiness.repository.ReleaseRepository;
import com.aireadiness.repository.ReleaseReportRepository;
import com.aireadiness.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReleaseReportService {

    private final ReleaseReportRepository releaseReportRepository;
    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;

    public ReleaseReportService(
            ReleaseReportRepository releaseReportRepository,
            ReleaseRepository releaseRepository,
            ProjectRepository projectRepository,
            AnalysisRepository analysisRepository,
            UserRepository userRepository
    ) {
        this.releaseReportRepository = releaseReportRepository;
        this.releaseRepository = releaseRepository;
        this.projectRepository = projectRepository;
        this.analysisRepository = analysisRepository;
        this.userRepository = userRepository;
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

    private Project verifyProjectOwnership(String projectId, String userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }

    public List<ReleaseReportResponse> getAllUserReports() {
        User user = getAuthenticatedUser();
        List<Project> userProjects = projectRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        
        Map<String, String> projectNames = new java.util.HashMap<>();
        for (Project project : userProjects) {
            if (project.getId() != null) {
                String name = (project.getName() != null && !project.getName().isBlank()) 
                        ? project.getName() 
                        : "Untitled Project";
                projectNames.put(project.getId(), name);
            }
        }

        List<ReleaseReportResponse> allReports = new ArrayList<>();

        for (Project project : userProjects) {
            if (project.getId() == null) continue;
            List<Release> releases = releaseRepository.findByProjectIdAndUserIdOrderByCreatedAtDesc(project.getId(), user.getId());
            for (Release release : releases) {
                ReleaseReport report = getOrCreateReportForRelease(project.getId(), release, user.getId());
                ReleaseReportResponse resp = mapToResponse(report);
                resp.setProjectName(projectNames.get(project.getId()));
                allReports.add(resp);
            }
        }

        return allReports;
    }

    public List<ReleaseReportResponse> getProjectReports(String projectId) {
        User user = getAuthenticatedUser();
        Project project = verifyProjectOwnership(projectId, user.getId());

        List<Release> releases = releaseRepository.findByProjectIdAndUserIdOrderByCreatedAtDesc(projectId, user.getId());

        return releases.stream()
                .map(release -> {
                    ReleaseReport report = getOrCreateReportForRelease(projectId, release, user.getId());
                    ReleaseReportResponse resp = mapToResponse(report);
                    resp.setProjectName(project.getName());
                    return resp;
                })
                .collect(Collectors.toList());
    }

    public ReleaseReportResponse getReleaseReport(String projectId, String releaseId) {
        User user = getAuthenticatedUser();
        Project project = verifyProjectOwnership(projectId, user.getId());

        Release release = releaseRepository.findByIdAndUserId(releaseId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        if (!projectId.equals(release.getProjectId())) {
            throw new ResourceNotFoundException("Release " + releaseId + " does not belong to project " + projectId);
        }

        ReleaseReport report = getOrCreateReportForRelease(projectId, release, user.getId());
        ReleaseReportResponse resp = mapToResponse(report);
        resp.setProjectName(project.getName());
        return resp;
    }

    private ReleaseReport getOrCreateReportForRelease(String projectId, Release release, String userId) {
        Optional<Analysis> latestAnalysisOpt = analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(release.getId(), userId);

        Double score = null;
        String level = null;
        String risk = null;
        String analysisId = null;
        String status = "NOT_ANALYZED";

        if (latestAnalysisOpt.isPresent()) {
            Analysis analysis = latestAnalysisOpt.get();
            analysisId = analysis.getId();
            status = (analysis.getStatus() != null && !analysis.getStatus().isBlank()) ? analysis.getStatus() : "COMPLETED";
            if (analysis.getReadinessScore() != null && analysis.getReadinessScore().getReadinessScore() != null) {
                score = analysis.getReadinessScore().getReadinessScore().doubleValue();
                if (analysis.getReadinessScore().getReadinessLevel() != null) {
                    level = analysis.getReadinessScore().getReadinessLevel().name();
                }
            }
            if (analysis.getRiskSummary() != null && analysis.getRiskSummary().getOverallRiskLevel() != null) {
                risk = analysis.getRiskSummary().getOverallRiskLevel().name();
            }
        } else if (release.getStatus() != null && !release.getStatus().isBlank() && !"READY_FOR_ANALYSIS".equalsIgnoreCase(release.getStatus())) {
            status = release.getStatus();
        }

        Optional<ReleaseReport> existing = releaseReportRepository.findByProjectIdAndReleaseIdAndUserId(projectId, release.getId(), userId);
        if (existing.isPresent()) {
            ReleaseReport rep = existing.get();
            boolean dirty = false;

            if (status != null && !status.equals(rep.getStatus())) {
                rep.setStatus(status);
                dirty = true;
            }
            if (release.getVersion() != null && !release.getVersion().equals(rep.getVersion())) {
                rep.setVersion(release.getVersion());
                dirty = true;
            }
            if (release.getName() != null && !release.getName().equals(rep.getReleaseName())) {
                rep.setReleaseName(release.getName());
                dirty = true;
            }
            if ((score == null && rep.getReadinessScore() != null) || (score != null && !score.equals(rep.getReadinessScore()))) {
                rep.setReadinessScore(score);
                dirty = true;
            }
            if ((level == null && rep.getReadinessLevel() != null) || (level != null && !level.equals(rep.getReadinessLevel()))) {
                rep.setReadinessLevel(level);
                dirty = true;
            }
            if ((risk == null && rep.getRiskLevel() != null) || (risk != null && !risk.equals(rep.getRiskLevel()))) {
                rep.setRiskLevel(risk);
                dirty = true;
            }
            if ((analysisId == null && rep.getLatestAnalysisId() != null) || (analysisId != null && !analysisId.equals(rep.getLatestAnalysisId()))) {
                rep.setLatestAnalysisId(analysisId);
                dirty = true;
            }

            if (dirty) {
                return releaseReportRepository.save(rep);
            }
            return rep;
        }

        String reportNumber = "REP-" + (release.getId().length() > 8 ? release.getId().substring(0, 8).toUpperCase() : release.getId().toUpperCase());
        ReleaseReport newReport = new ReleaseReport(
                projectId,
                release.getId(),
                userId,
                release.getVersion(),
                reportNumber,
                release.getName(),
                status
        );
        newReport.setReadinessScore(score);
        newReport.setReadinessLevel(level);
        newReport.setRiskLevel(risk);
        newReport.setLatestAnalysisId(analysisId);

        try {
            return releaseReportRepository.save(newReport);
        } catch (org.springframework.dao.DuplicateKeyException ex) {
            return releaseReportRepository.findByProjectIdAndReleaseIdAndUserId(projectId, release.getId(), userId)
                    .orElse(newReport);
        }
    }

    private ReleaseReportResponse mapToResponse(ReleaseReport report) {
        ReleaseReportResponse res = new ReleaseReportResponse(
                report.getId(),
                report.getProjectId(),
                report.getReleaseId(),
                report.getUserId(),
                report.getVersion(),
                report.getReportNumber(),
                report.getReleaseName(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
        res.setReadinessScore(report.getReadinessScore());
        res.setReadinessLevel(report.getReadinessLevel());
        res.setRiskLevel(report.getRiskLevel());
        res.setLatestAnalysisId(report.getLatestAnalysisId());
        return res;
    }
}
