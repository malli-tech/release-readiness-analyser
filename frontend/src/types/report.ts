export interface ReleaseReport {
  id: string;
  projectId: string;
  releaseId: string;
  userId: string;
  version: string;
  reportNumber: string;
  releaseName?: string;
  projectName?: string;
  status: string;
  readinessScore?: number | null;
  readinessLevel?: string | null;
  riskLevel?: string | null;
  latestAnalysisId?: string | null;
  createdAt: string;
  updatedAt: string;
}
