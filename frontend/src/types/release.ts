export type ReleaseStatus = 'NOT_ANALYZED' | 'ANALYZING' | 'ANALYZED' | 'READY' | 'NEEDS_REVIEW' | 'NOT_READY' | 'QUEUED';

export interface Release {
  id: string;
  projectId: string;
  userId?: string;
  version: string;
  name: string;
  description?: string;
  status: ReleaseStatus | string;
  createdAt: string;
  updatedAt?: string;
  score?: number;
  readinessScore?: number;
  projectName?: string;
  riskLevel?: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';
  issuesCount?: { critical: number; high: number; medium: number; low: number; total: number } | number;
  commitHash?: string;
  branch?: string;
  fileSize?: string;
}



export interface CreateReleaseRequest {
  version: string;
  name: string;
  description?: string;
}

export interface UpdateReleaseRequest {
  version: string;
  name: string;
  description?: string;
}
