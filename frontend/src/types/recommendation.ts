export type RecommendationPriority = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
export type RecommendationEffort = 'LOW' | 'MEDIUM' | 'HIGH';
export type RecommendationStatus = 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'IGNORED';

export interface RecommendationRecord {
  id: string;
  analysisId: string;
  findingId: string;
  ruleId: string;
  category: string;
  severity: string;
  title: string;
  summary: string;
  recommendedAction: string;
  priority: RecommendationPriority;
  effort: RecommendationEffort;
  status: RecommendationStatus;
  filePath?: string;
  lineNumber?: number;
  createdAt?: string;
  updatedAt?: string;
}
