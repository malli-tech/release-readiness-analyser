package com.aireadiness.service;

import com.aireadiness.aireview.model.AIReview;
import com.aireadiness.aireview.repository.AIReviewRepository;
import com.aireadiness.dto.results.ReleaseResultsResponse;
import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.*;
import com.aireadiness.recommendation.model.Recommendation;
import com.aireadiness.recommendation.repository.RecommendationRepository;
import com.aireadiness.repository.AnalysisRepository;
import com.aireadiness.repository.ProjectRepository;
import com.aireadiness.repository.ReleaseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class ResultsDashboardService {

    private final ReleaseRepository releaseRepository;
    private final ProjectRepository projectRepository;
    private final AnalysisRepository analysisRepository;
    private final AIReviewRepository aiReviewRepository;
    private final RecommendationRepository recommendationRepository;

    public ResultsDashboardService(
            ReleaseRepository releaseRepository,
            ProjectRepository projectRepository,
            AnalysisRepository analysisRepository,
            AIReviewRepository aiReviewRepository,
            RecommendationRepository recommendationRepository
    ) {
        this.releaseRepository = releaseRepository;
        this.projectRepository = projectRepository;
        this.analysisRepository = analysisRepository;
        this.aiReviewRepository = aiReviewRepository;
        this.recommendationRepository = recommendationRepository;
    }

    public ReleaseResultsResponse getReleaseResults(String releaseId, String userId) {
        // 1. Verify ownership & release existence
        Release release = releaseRepository.findByIdAndUserId(releaseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Release not found with id: " + releaseId));

        String projectName = projectRepository.findByIdAndUserId(release.getProjectId(), userId)
                .map(Project::getName)
                .orElse(null);

        ReleaseResultsResponse.ReleaseInfo releaseInfo = new ReleaseResultsResponse.ReleaseInfo(
                release.getId(),
                release.getProjectId(),
                projectName,
                release.getVersion(),
                release.getName(),
                release.getDescription(),
                release.getStatus(),
                release.getCreatedAt(),
                release.getUpdatedAt()
        );

        // 2. Fetch latest analysis for this release and user
        Optional<Analysis> optAnalysis = analysisRepository.findFirstByReleaseIdAndUserIdOrderByRunNumberDesc(releaseId, userId);

        if (optAnalysis.isEmpty()) {
            return new ReleaseResultsResponse(releaseInfo, null, null, null, null, null, null);
        }

        Analysis analysis = optAnalysis.get();

        // 3. Assemble AnalysisSummary & FindingSummary
        ReleaseResultsResponse.AnalysisSummaryDto analysisSummary = new ReleaseResultsResponse.AnalysisSummaryDto();
        analysisSummary.setAnalysisId(analysis.getId());
        analysisSummary.setRunNumber(analysis.getRunNumber());
        analysisSummary.setStatus(analysis.getStatus());
        analysisSummary.setStartedAt(analysis.getStartedAt());
        analysisSummary.setCompletedAt(analysis.getCompletedAt());
        analysisSummary.setWarnings(analysis.getWarnings() != null ? analysis.getWarnings() : Collections.emptyList());

        ReleaseResultsResponse.FindingSummaryDto findingSummary = new ReleaseResultsResponse.FindingSummaryDto();

        UnifiedAnalysisSummary uSummary = analysis.getUnifiedAnalysisSummary();
        if (uSummary != null) {
            analysisSummary.setCompleteness(uSummary.getCompleteness() != null ? uSummary.getCompleteness() : "UNKNOWN");
            analysisSummary.setTotalFindings(uSummary.getTotalFindings());
            analysisSummary.setFindingsBySeverity(uSummary.getFindingsBySeverity() != null ? uSummary.getFindingsBySeverity() : Collections.emptyMap());
            analysisSummary.setFindingsByCategory(uSummary.getFindingsByCategory() != null ? uSummary.getFindingsByCategory() : Collections.emptyMap());
            analysisSummary.setAffectedFiles(uSummary.getAffectedFiles());
            analysisSummary.setCompletedAnalyzers(uSummary.getCompletedAnalyzers() != null ? uSummary.getCompletedAnalyzers() : Collections.emptyList());
            analysisSummary.setFailedAnalyzers(uSummary.getFailedAnalyzers() != null ? uSummary.getFailedAnalyzers() : Collections.emptyList());
            analysisSummary.setSkippedAnalyzers(uSummary.getSkippedAnalyzers() != null ? uSummary.getSkippedAnalyzers() : Collections.emptyList());

            findingSummary.setTotalFindings(uSummary.getTotalFindings());
            findingSummary.setHighFindings(uSummary.getHighFindings());
            findingSummary.setMediumFindings(uSummary.getMediumFindings());
            findingSummary.setLowFindings(uSummary.getLowFindings());
            findingSummary.setInfoFindings(uSummary.getInfoFindings());
            findingSummary.setAffectedFilesCount(uSummary.getAffectedFiles());
            findingSummary.setCategoryBreakdown(uSummary.getFindingsByCategory() != null ? uSummary.getFindingsByCategory() : Collections.emptyMap());
            findingSummary.setSeverityBreakdown(uSummary.getFindingsBySeverity() != null ? uSummary.getFindingsBySeverity() : Collections.emptyMap());
        } else if (analysis.getFindings() != null) {
            List<Finding> findings = analysis.getFindings();
            analysisSummary.setTotalFindings(findings.size());
            analysisSummary.setCompleteness("PARTIAL");

            Map<String, Integer> bySev = new HashMap<>();
            Map<String, Integer> byCat = new HashMap<>();
            Set<String> files = new HashSet<>();
            int high = 0, med = 0, low = 0, info = 0;

            for (Finding f : findings) {
                if (f.getSeverity() != null) {
                    String s = f.getSeverity().toUpperCase();
                    bySev.put(s, bySev.getOrDefault(s, 0) + 1);
                    if ("HIGH".equals(s) || "CRITICAL".equals(s)) high++;
                    else if ("MEDIUM".equals(s)) med++;
                    else if ("LOW".equals(s)) low++;
                    else info++;
                }
                if (f.getCategory() != null) {
                    byCat.put(f.getCategory(), byCat.getOrDefault(f.getCategory(), 0) + 1);
                }
                if (f.getFilePath() != null && !f.getFilePath().isBlank()) {
                    files.add(f.getFilePath());
                }
            }

            analysisSummary.setFindingsBySeverity(bySev);
            analysisSummary.setFindingsByCategory(byCat);
            analysisSummary.setAffectedFiles(files.size());

            findingSummary.setTotalFindings(findings.size());
            findingSummary.setHighFindings(high);
            findingSummary.setMediumFindings(med);
            findingSummary.setLowFindings(low);
            findingSummary.setInfoFindings(info);
            findingSummary.setAffectedFilesCount(files.size());
            findingSummary.setCategoryBreakdown(byCat);
            findingSummary.setSeverityBreakdown(bySev);
        } else {
            analysisSummary.setCompleteness("NOT_AVAILABLE");
        }

        // 4. Extract authoritative persisted RiskSummary & ReadinessScore (No recalculation)
        RiskSummary riskSummary = analysis.getRiskSummary();
        ReadinessScore readinessScore = analysis.getReadinessScore();

        // 5. Aggregate safe AIReview summary
        List<AIReview> reviews = aiReviewRepository.findByAnalysisId(analysis.getId());
        ReleaseResultsResponse.AIReviewSummaryDto aiReviewSummary = new ReleaseResultsResponse.AIReviewSummaryDto();
        if (reviews != null && !reviews.isEmpty()) {
            aiReviewSummary.setTotalReviews(reviews.size());
            int completed = 0, failed = 0, pending = 0;
            String model = null;
            String confidence = null;
            String summaryText = null;
            String failureCategory = null;
            Instant latestTs = null;

            for (AIReview r : reviews) {
                if (r.getUpdatedAt() != null && (latestTs == null || r.getUpdatedAt().isAfter(latestTs))) {
                    latestTs = r.getUpdatedAt();
                }
                if (r.getModel() != null && model == null) {
                    model = r.getModel();
                }
                if (r.getConfidence() != null && confidence == null) {
                    confidence = r.getConfidence().name();
                }
                if (r.getSummary() != null && summaryText == null) {
                    summaryText = r.getSummary();
                }
                if (r.getErrorMessage() != null && failureCategory == null) {
                    failureCategory = r.getErrorMessage();
                }

                if (r.getStatus() != null) {
                    switch (r.getStatus()) {
                        case COMPLETED: completed++; break;
                        case FAILED: failed++; break;
                        case PENDING:
                        case IN_PROGRESS: pending++; break;
                    }
                }
            }

            aiReviewSummary.setCompletedReviews(completed);
            aiReviewSummary.setFailedReviews(failed);
            aiReviewSummary.setPendingReviews(pending);
            aiReviewSummary.setModel(model);
            aiReviewSummary.setConfidence(confidence);
            aiReviewSummary.setSummary(summaryText);
            aiReviewSummary.setFailureCategory(failureCategory);
            aiReviewSummary.setLatestReviewTimestamp(latestTs);

            if (failed > 0 && completed == 0) {
                aiReviewSummary.setOverallStatus("FAILED");
            } else if (completed == reviews.size()) {
                aiReviewSummary.setOverallStatus("COMPLETED");
            } else if (pending > 0) {
                aiReviewSummary.setOverallStatus("PENDING");
            } else if (completed > 0) {
                aiReviewSummary.setOverallStatus("PARTIAL");
            } else {
                aiReviewSummary.setOverallStatus("COMPLETED");
            }
        } else {
            aiReviewSummary.setOverallStatus("NO_REVIEWS");
        }

        // 6. Aggregate lightweight Recommendation summary
        List<Recommendation> recs = recommendationRepository.findByAnalysisId(analysis.getId());
        ReleaseResultsResponse.RecommendationSummaryDto recSummary = new ReleaseResultsResponse.RecommendationSummaryDto();
        if (recs != null && !recs.isEmpty()) {
            recSummary.setTotalRecommendations(recs.size());
            Map<String, Integer> byPriority = new HashMap<>();
            Map<String, Integer> byStatus = new HashMap<>();
            Map<String, Integer> byCategory = new HashMap<>();

            for (Recommendation r : recs) {
                if (r.getPriority() != null) {
                    String p = r.getPriority().name();
                    byPriority.put(p, byPriority.getOrDefault(p, 0) + 1);
                }
                if (r.getStatus() != null) {
                    String s = r.getStatus().name();
                    byStatus.put(s, byStatus.getOrDefault(s, 0) + 1);
                }
                if (r.getCategory() != null) {
                    byCategory.put(r.getCategory(), byCategory.getOrDefault(r.getCategory(), 0) + 1);
                }
            }
            recSummary.setCountsByPriority(byPriority);
            recSummary.setCountsByStatus(byStatus);
            recSummary.setCountsByCategory(byCategory);
        }

        return new ReleaseResultsResponse(
                releaseInfo,
                analysisSummary,
                riskSummary,
                readinessScore,
                aiReviewSummary,
                recSummary,
                findingSummary
        );
    }
}
