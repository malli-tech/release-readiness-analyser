export type FindingSeverity = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export type FindingCategory =
  | 'Security'
  | 'Testing'
  | 'Code Quality'
  | 'Dependencies'
  | 'Performance'
  | 'CODE_QUALITY'
  | 'TESTING'
  | 'DEPENDENCY'
  | 'DEPENDENCIES'
  | 'SECURITY'
  | 'PERFORMANCE';

export interface Finding {
  id?: string;
  analysisId?: string;
  releaseId?: string;
  title: string;
  category: FindingCategory;
  severity: FindingSeverity;
  filePath: string;
  lineNumber?: number;
  description: string;
  codeSnippet?: string;
  highlightedLine?: number;
  whatIsWrong?: string;
  whyItMatters?: string;
  whatToReview?: string;
  recommendedAction?: string;
  aiExplanation?: string;
  ruleId?: string;
  evidence?: string;
  confidence?: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  impact?: string;
  status?: string;
}
