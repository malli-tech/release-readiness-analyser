package com.aireadiness.recommendation.service;

import com.aireadiness.exception.ResourceNotFoundException;
import com.aireadiness.model.Analysis;
import com.aireadiness.model.Finding;
import com.aireadiness.recommendation.catalog.RecommendationTemplate;
import com.aireadiness.recommendation.catalog.RecommendationTemplateCatalog;
import com.aireadiness.recommendation.model.Recommendation;
import com.aireadiness.recommendation.model.RecommendationPriority;
import com.aireadiness.recommendation.model.RecommendationStatus;
import com.aireadiness.recommendation.repository.RecommendationRepository;
import com.aireadiness.repository.AnalysisRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RecommendationTemplateCatalog catalog;
    private final RecommendationRepository recommendationRepository;
    private final AnalysisRepository analysisRepository;

    public RecommendationService(
            RecommendationTemplateCatalog catalog,
            RecommendationRepository recommendationRepository
    ) {
        this(catalog, recommendationRepository, null);
    }

    @Autowired
    public RecommendationService(
            RecommendationTemplateCatalog catalog,
            @Autowired(required = false) RecommendationRepository recommendationRepository,
            @Autowired(required = false) AnalysisRepository analysisRepository
    ) {
        this.catalog = catalog;
        this.recommendationRepository = recommendationRepository;
        this.analysisRepository = analysisRepository;
    }

    public List<Recommendation> generateRecommendations(Analysis analysis) {
        if (analysis == null) {
            return Collections.emptyList();
        }

        List<Finding> findings = analysis.getFindings();
        String analysisId = analysis.getId();
        List<String> warnings = analysis.getWarnings();

        return generateRecommendations(findings, analysisId, warnings);
    }

    public List<Recommendation> generateRecommendations(List<Finding> findings, String analysisId, List<String> warnings) {
        if (findings == null || findings.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> processedFindingIds = new HashSet<>();
        List<Recommendation> recommendations = new ArrayList<>();

        for (Finding finding : findings) {
            if (finding == null) continue;

            String findingId = finding.getId();
            if (findingId != null && !processedFindingIds.add(findingId)) {
                log.debug("Skipping duplicate finding ID in recommendation generation: {}", findingId);
                continue;
            }

            String ruleId = finding.getRuleId();
            Optional<RecommendationTemplate> templateOpt = catalog.findTemplate(ruleId);
            if (templateOpt.isEmpty()) {
                String warningMsg = "Unsupported rule ID for recommendation mapping: " + (ruleId != null ? ruleId : "UNKNOWN");
                log.warn(warningMsg);
                if (warnings != null) {
                    warnings.add(warningMsg);
                }
                continue;
            }

            RecommendationTemplate template = templateOpt.get();
            Recommendation rec = mapToRecommendation(analysisId, finding, template);
            recommendations.add(rec);
        }

        // Sort deterministically: Priority (CRITICAL -> HIGH -> MEDIUM -> LOW) -> Category -> RuleId -> FindingId
        recommendations.sort(Comparator
                .comparing(Recommendation::getPriority, Comparator.nullsLast(Enum::compareTo))
                .thenComparing(r -> r.getCategory() != null ? r.getCategory() : "")
                .thenComparing(r -> r.getRuleId() != null ? r.getRuleId() : "")
                .thenComparing(r -> r.getFindingId() != null ? r.getFindingId() : "")
        );

        if (recommendationRepository != null && analysisId != null && !analysisId.trim().isEmpty()) {
            try {
                recommendationRepository.deleteByAnalysisId(analysisId);
                recommendations = recommendationRepository.saveAll(recommendations);
            } catch (Exception e) {
                log.warn("Failed to persist recommendations to database for analysisId {}: {}", analysisId, e.getMessage());
            }
        }

        return recommendations;
    }

    public List<Recommendation> getRecommendationsByAnalysisId(String analysisId) {
        if (recommendationRepository == null || analysisId == null || analysisId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return recommendationRepository.findByAnalysisId(analysisId);
    }

    public List<Recommendation> getRecommendationsForAnalysis(String analysisId, String userId) {
        Analysis analysis = getAnalysisWithOwnershipCheck(analysisId, userId);
        List<Recommendation> existing = recommendationRepository != null ? recommendationRepository.findByAnalysisId(analysisId) : Collections.emptyList();
        if (existing != null && !existing.isEmpty()) {
            return existing;
        }
        return generateRecommendations(analysis);
    }

    public Recommendation getRecommendationForFinding(String analysisId, String findingId, String userId) {
        Analysis analysis = getAnalysisWithOwnershipCheck(analysisId, userId);
        if (recommendationRepository != null) {
            Optional<Recommendation> existingOpt = recommendationRepository.findByAnalysisIdAndFindingId(analysisId, findingId);
            if (existingOpt.isPresent()) {
                return existingOpt.get();
            }
        }

        // Finding check
        Finding targetFinding = analysis.getFindings() != null ? analysis.getFindings().stream()
                .filter(f -> f.getId() != null && f.getId().equals(findingId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Finding not found with id: " + findingId + " in analysis: " + analysisId))
                : null;

        if (targetFinding == null) {
            throw new ResourceNotFoundException("Finding not found with id: " + findingId + " in analysis: " + analysisId);
        }

        List<Recommendation> generated = generateRecommendations(analysis);
        return generated.stream()
                .filter(r -> r.getFindingId() != null && r.getFindingId().equals(findingId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found for finding: " + findingId));
    }

    private Analysis getAnalysisWithOwnershipCheck(String analysisId, String userId) {
        if (analysisRepository == null) {
            throw new ResourceNotFoundException("Analysis not found with id: " + analysisId);
        }
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found with id: " + analysisId));

        if (!analysis.getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Analysis not found with id: " + analysisId);
        }

        return analysis;
    }

    private Recommendation mapToRecommendation(String analysisId, Finding finding, RecommendationTemplate template) {
        Recommendation rec = new Recommendation();
        rec.setAnalysisId(analysisId != null ? analysisId : finding.getAnalysisId());
        rec.setFindingId(finding.getId());
        rec.setRuleId(finding.getRuleId() != null ? finding.getRuleId() : template.getRuleId());
        rec.setCategory(finding.getCategory());
        rec.setSeverity(finding.getSeverity());
        rec.setTitle(template.getTitle());
        rec.setSummary(template.getSummary());
        rec.setRecommendedAction(template.getRecommendedAction());

        RecommendationPriority priority = resolvePriority(finding.getSeverity(), finding.getCategory(), template.getDefaultPriority());
        rec.setPriority(priority);
        rec.setEffort(template.getDefaultEffort());
        rec.setStatus(RecommendationStatus.OPEN);

        rec.setFilePath(finding.getFilePath());
        rec.setLineNumber(finding.getLineNumber());

        rec.setCreatedAt(Instant.now());
        rec.setUpdatedAt(Instant.now());

        return rec;
    }

    public RecommendationPriority resolvePriority(String severity, String category, RecommendationPriority defaultPriority) {
        if (severity != null) {
            String sev = severity.trim().toUpperCase();
            if ("CRITICAL".equals(sev)) {
                return RecommendationPriority.CRITICAL;
            }
            if ("HIGH".equals(sev)) {
                if ("SECURITY".equalsIgnoreCase(category)) {
                    return RecommendationPriority.CRITICAL;
                }
                return RecommendationPriority.HIGH;
            }
            if ("MEDIUM".equals(sev)) {
                return RecommendationPriority.MEDIUM;
            }
            if ("LOW".equals(sev) || "INFO".equals(sev)) {
                return RecommendationPriority.LOW;
            }
        }
        return defaultPriority != null ? defaultPriority : RecommendationPriority.MEDIUM;
    }
}
