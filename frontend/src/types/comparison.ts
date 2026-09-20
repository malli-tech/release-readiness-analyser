export interface ReleaseComparisonOverview {
  releaseId: string;
  projectId: string;
  projectName?: string;
  version: string;
  name: string;
  status: string;
  analysisId?: string;
  runNumber?: number;
  completeness?: string;
}

export interface FindingsComparisonDto {
  baseTotal: number;
  targetTotal: number;
  delta: number;
}

export interface SeverityComparisonDto {
  baseCounts: Record<string, number>;
  targetCounts: Record<string, number>;
  deltas: Record<string, number>;
}

export interface CategoryComparisonDto {
  baseCounts: Record<string, number>;
  targetCounts: Record<string, number>;
  deltas: Record<string, number>;
}

export interface AffectedFilesComparisonDto {
  baseAffectedFiles: number;
  targetAffectedFiles: number;
  delta: number;
}

export interface RiskComparisonDto {
  baseRiskLevel?: string;
  targetRiskLevel?: string;
  baseWeightedRiskPoints?: number;
  targetWeightedRiskPoints?: number;
  deltaWeightedRiskPoints?: number;
}

export interface ReadinessComparisonDto {
  baseScore?: number;
  targetScore?: number;
  scoreDelta?: number;
  baseLevel?: string;
  targetLevel?: string;
  baseConfidence?: string;
  targetConfidence?: string;
}

export interface RecommendationComparisonDto {
  baseTotal: number;
  targetTotal: number;
  totalDelta: number;
  basePriorityCounts?: Record<string, number>;
  targetPriorityCounts?: Record<string, number>;
  baseStatusCounts?: Record<string, number>;
  targetStatusCounts?: Record<string, number>;
  baseCategoryCounts?: Record<string, number>;
  targetCategoryCounts?: Record<string, number>;
}

export interface AIReviewComparisonDto {
  baseOverallStatus?: string;
  targetOverallStatus?: string;
  baseConfidence?: string;
  targetConfidence?: string;
  baseTotalReviews?: number;
  targetTotalReviews?: number;
  baseCompletedReviews?: number;
  targetCompletedReviews?: number;
}

export interface CoverageComparisonDto {
  baseCompleteness?: string;
  targetCompleteness?: string;
  baseCompletedAnalyzers?: string[];
  targetCompletedAnalyzers?: string[];
  baseFailedAnalyzers?: string[];
  targetFailedAnalyzers?: string[];
  baseSkippedAnalyzers?: string[];
  targetSkippedAnalyzers?: string[];
}

export interface FindingChangeItem {
  findingId?: string;
  ruleId?: string;
  category?: string;
  severity?: string;
  title?: string;
  filePath?: string;
  lineNumber?: number;
  changeType: 'NEW' | 'RESOLVED' | 'UNCHANGED' | string;
}

export interface FindingChangesDto {
  newCount: number;
  resolvedCount: number;
  unchangedCount: number;
  items: FindingChangeItem[];
  limitationNote?: string;
}

export interface VersionComparisonResponse {
  baseRelease: ReleaseComparisonOverview;
  targetRelease: ReleaseComparisonOverview;
  findingsComparison: FindingsComparisonDto;
  severityComparison: SeverityComparisonDto;
  categoryComparison: CategoryComparisonDto;
  affectedFilesComparison: AffectedFilesComparisonDto;
  riskComparison?: RiskComparisonDto;
  readinessComparison?: ReadinessComparisonDto;
  recommendationComparison?: RecommendationComparisonDto;
  aiReviewComparison?: AIReviewComparisonDto;
  coverageComparison?: CoverageComparisonDto;
  findingChanges: FindingChangesDto;
}
