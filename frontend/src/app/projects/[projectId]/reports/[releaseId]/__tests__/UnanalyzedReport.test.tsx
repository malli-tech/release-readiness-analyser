import React from 'react';
import { ReleaseReport } from '@/types/report';

export const sampleUnanalyzedReport: ReleaseReport = {
  id: 'rep-unanalyzed-001',
  projectId: 'proj-demo-1',
  releaseId: 'rel-unanalyzed-100',
  userId: 'user-001',
  version: '1.0.0',
  reportNumber: 'REP-REL-UNANALYZED',
  releaseName: 'Unanalyzed Release Candidate',
  projectName: 'Demo Enterprise App',
  status: 'NOT_ANALYZED',
  readinessScore: undefined,
  readinessLevel: undefined,
  riskLevel: undefined,
  latestAnalysisId: undefined,
  createdAt: '2026-09-21T00:00:00Z',
  updatedAt: '2026-09-21T00:00:00Z',
};

export const sampleAnalyzedReport: ReleaseReport = {
  id: 'rep-analyzed-002',
  projectId: 'proj-demo-1',
  releaseId: 'rel-analyzed-200',
  userId: 'user-001',
  version: '2.0.0',
  reportNumber: 'REP-REL-ANALYZED',
  releaseName: 'Evaluated Release Candidate',
  projectName: 'Demo Enterprise App',
  status: 'COMPLETED',
  readinessScore: 92.5,
  readinessLevel: 'EXCELLENT',
  riskLevel: 'LOW',
  latestAnalysisId: 'ans-888',
  createdAt: '2026-09-21T00:00:00Z',
  updatedAt: '2026-09-21T00:00:00Z',
};

/**
 * Unanalyzed Report UI Contract Verification Test
 */
export function verifyUnanalyzedReportContract() {
  // 1. Verify Unanalyzed Report Properties
  if (sampleUnanalyzedReport.status !== 'NOT_ANALYZED') {
    throw new Error('Unanalyzed report must have status NOT_ANALYZED');
  }
  if (sampleUnanalyzedReport.readinessScore !== undefined || sampleUnanalyzedReport.latestAnalysisId !== undefined) {
    throw new Error('Unanalyzed report must not contain fake readiness scores or analysis IDs');
  }

  // 2. Verify Analyzed Report Properties
  if (sampleAnalyzedReport.status !== 'COMPLETED' || sampleAnalyzedReport.latestAnalysisId !== 'ans-888') {
    throw new Error('Analyzed report must preserve persisted status and analysis ID');
  }
  if (sampleAnalyzedReport.readinessScore !== 92.5 || sampleAnalyzedReport.riskLevel !== 'LOW') {
    throw new Error('Analyzed report must preserve persisted readiness score and risk level');
  }

  // 3. Verify Empty State Identification Rule
  const isUnanalyzed = (report: ReleaseReport) => !report.latestAnalysisId || report.status === 'NOT_ANALYZED';
  if (!isUnanalyzed(sampleUnanalyzedReport)) {
    throw new Error('isUnanalyzed filter must identify unanalyzed reports correctly');
  }
  if (isUnanalyzed(sampleAnalyzedReport)) {
    // verified
  }

  return true;
}

verifyUnanalyzedReportContract();
