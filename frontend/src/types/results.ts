import { RiskSummary, ReadinessScore } from './analysis';

export interface ReleaseInfo {
  releaseId: string;
  projectId: string;
  projectName?: string;
  version: string;
  name: string;
  description?: string;
  status: string;
  createdAt: string;
  updatedAt?: string;
}

export interface AnalysisSummaryDto {
  analysisId?: string;
  runNumber?: number;
  status?: string;
  completeness?: 'COMPLETE' | 'PARTIAL' | 'NOT_AVAILABLE' | 'UNKNOWN' | string;
  totalFindings?: number;
  findingsBySeverity?: Record<string, number>;
  findingsByCategory?: Record<string, number>;
  affectedFiles?: number;
  completedAnalyzers?: string[];
  failedAnalyzers?: string[];
  skippedAnalyzers?: string[];
  warnings?: string[];
  startedAt?: string;
  completedAt?: string;
}

export interface AIReviewSummaryDto {
  totalReviews: number;
  completedReviews: number;
  failedReviews: number;
  pendingReviews: number;
  overallStatus: 'COMPLETED' | 'FAILED' | 'PENDING' | 'PARTIAL' | 'NO_REVIEWS' | string;
  model?: string;
  confidence?: string;
  summary?: string;
  failureCategory?: string;
  latestReviewTimestamp?: string;
}

export interface RecommendationSummaryDto {
  totalRecommendations: number;
  countsByPriority?: Record<string, number>;
  countsByStatus?: Record<string, number>;
  countsByCategory?: Record<string, number>;
}

export interface FindingSummaryDto {
  totalFindings: number;
  highFindings: number;
  mediumFindings: number;
  lowFindings: number;
  infoFindings: number;
  affectedFilesCount: number;
  categoryBreakdown?: Record<string, number>;
  severityBreakdown?: Record<string, number>;
}

export interface ReleaseResultsResponse {
  releaseInfo: ReleaseInfo;
  analysisSummary?: AnalysisSummaryDto | null;
  riskSummary?: RiskSummary | null;
  readinessScore?: ReadinessScore | null;
  aiReviewSummary?: AIReviewSummaryDto | null;
  recommendationSummary?: RecommendationSummaryDto | null;
  findingSummary?: FindingSummaryDto | null;
}
