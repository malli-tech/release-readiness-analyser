import { Finding, FindingCategory } from './finding';
import { ReleaseStatus } from './release';

export interface DetectionEvidence {
  technology: string;
  confidence: 'HIGH' | 'MEDIUM' | 'LOW';
  evidence: string[];
}

export interface FileDescriptor {
  relativePath: string;
  filename: string;
  extension: string;
  size: number;
  isDirectory: boolean;
  fileType: string;
}

export interface ProjectStructure {
  totalFiles: number;
  totalDirectories: number;
  sourceFileCount: number;
  testFileCount: number;
  manifestFileCount: number;
  configFileCount: number;
  docFileCount: number;
  sampleFiles: FileDescriptor[];
}

export interface ProjectProfile {
  primaryLanguage: string;
  languages: string[];
  framework: string;
  frameworks: string[];
  buildSystem: string;
  projectType: 'BACKEND' | 'FRONTEND' | 'FULL_STACK' | 'MOBILE' | 'LIBRARY' | 'CLI' | 'UNKNOWN';
  testFrameworks: string[];
  database: string;
  databases: string[];
  packageManager?: string;
  entryPoints: string[];
  detectedManifests: string[];
  projectStructure?: ProjectStructure;
  analysisCompleteness: 'COMPLETE' | 'PARTIAL' | 'UNKNOWN';
  detectionWarnings: string[];
  detectionEvidences: DetectionEvidence[];
}

export interface AnalysisPlan {
  analyzers: string[];
  rationale: Record<string, string>;
}

export interface TestingSummary {
  testFiles: number;
  sourceFiles: number;
  testPresenceRatio: number;
  detectedFrameworks: string[];
  testsDetected: number;
  assertionsDetected: number;
  skippedTestsDetected: number;
  emptyTestsDetected: number;
  todoTestsDetected: number;
  sourceFilesWithoutTests: number;
  testFilesWithoutObviousAssertions: number;
  testingCompleteness: 'STRONG' | 'MODERATE' | 'WEAK' | 'UNKNOWN' | 'PARTIAL';
  testingWarnings: string[];
  disclaimer: string;
}

export interface DependencySummary {
  manifestFiles: string[];
  detectedPackageManagers: string[];
  dependencyCount: number;
  directDependencyCount: number;
  devDependencyCount: number;
  unpinnedDependencyCount: number;
  broadVersionDependencyCount: number;
  duplicateDependencyCount: number;
  dependencyManagementWarnings: string[];
  dependencyCompleteness: 'COMPLETE' | 'PARTIAL' | 'UNKNOWN';
  dependencyWarnings: string[];
  disclaimer: string;
}

export interface SecuritySummary {
  totalSecurityFindings: number;
  criticalFindings: number;
  highFindings: number;
  mediumFindings: number;
  lowFindings: number;
  hardcodedSecretsDetected: number;
  insecureTransportFindings: number;
  dangerousExecutionFindings: number;
  injectionRiskFindings: number;
  deserializationFindings: number;
  weakCryptographyFindings: number;
  configurationFindings: number;
  sensitiveFilesDetected: number;
  securityCompleteness: 'COMPLETE' | 'PARTIAL' | 'UNKNOWN';
  securityWarnings: string[];
  disclaimer: string;
}

export interface AnalysisResponse {
  id: string;
  projectId: string;
  releaseId: string;
  runNumber: number;
  status: string;
  startedAt: string;
  completedAt?: string;
  projectProfile: ProjectProfile;
  analysisPlan: AnalysisPlan;
  findings: Finding[];
  categoryScores?: Record<string, number>;
  readinessScore?: number;
  warnings: string[];
  testingSummary?: TestingSummary;
  dependencySummary?: DependencySummary;
  securitySummary?: SecuritySummary;
  message: string;
}

// Legacy UI mock interfaces preserved for backwards compatibility
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
  change: number;
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
