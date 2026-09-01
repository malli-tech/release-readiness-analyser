import { Finding, FindingCategory, FindingSeverity } from './finding';
import { ReleaseStatus } from './release';

export interface CategoryScore {
  category: FindingCategory;
  score: number;
  weight: number;
  status: 'Good' | 'Needs Attention' | 'Critical';
  issuesCount: number;
  description: string;
}

export interface MetricComparison {
  name: string;
  previousValue: string | number;
  currentValue: string | number;
  change: number; // positive is improvement, negative is regression
  unit?: string;
  isPositiveChange: boolean;
}

export interface AIReview {
  summary: string;
  majorConcerns: string[];
  recommendedActions: string[];
  blockers: string[];
}

export interface AnalysisResult {
  id: string;
  releaseId: string;
  projectId: string;
  readinessScore: number;
  status: ReleaseStatus;
  createdAt: string;
  categoryScores: CategoryScore[];
  findingSummary: {
    critical: number;
    high: number;
    medium: number;
    low: number;
    total: number;
  };
  topFindings: Finding[];
  aiReview: AIReview;
  recommendations: string[];
  comparison?: {
    previousVersion: string;
    currentVersion: string;
    metrics: MetricComparison[];
  };
}
