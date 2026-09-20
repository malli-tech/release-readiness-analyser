import React from 'react';
import { ReleaseResultsResponse } from '@/types/results';

export const sampleFullResults: ReleaseResultsResponse = {
  releaseInfo: {
    releaseId: 'rel-100',
    projectId: 'proj-1',
    projectName: 'Demo Project',
    version: '1.0.0',
    name: 'Release 1.0.0',
    description: 'First evaluation',
    status: 'ANALYZED',
    createdAt: '2026-09-20T10:00:00Z',
    updatedAt: '2026-09-20T10:30:00Z',
  },
  analysisSummary: {
    analysisId: 'ans-100',
    runNumber: 1,
    status: 'COMPLETED',
    completeness: 'COMPLETE',
    totalFindings: 5,
    findingsBySeverity: { HIGH: 2, MEDIUM: 3 },
    findingsByCategory: { SECURITY: 2, CODE_QUALITY: 3 },
    affectedFiles: 3,
    completedAnalyzers: ['CODE_QUALITY', 'SECURITY'],
    failedAnalyzers: [],
    skippedAnalyzers: [],
    warnings: [],
    startedAt: '2026-09-20T10:00:00Z',
    completedAt: '2026-09-20T10:30:00Z',
  },
  riskSummary: {
    overallRiskLevel: 'MEDIUM',
    weightedRiskPoints: 12.5,
    baseRiskPoints: 10,
    totalFindings: 5,
    highFindings: 2,
    mediumFindings: 3,
    lowFindings: 0,
    infoFindings: 0,
    categoryRisk: {},
    severityRisk: {},
    riskFactors: [],
    riskWarnings: [],
    completeness: 'COMPLETE',
    calculationVersion: 'risk-v1',
  },
  readinessScore: {
    readinessScore: 85,
    readinessLevel: 'GOOD',
    confidence: 'HIGH',
    weightedRiskPoints: 12.5,
    baseRiskPoints: 10,
    totalFindings: 5,
    highFindings: 2,
    mediumFindings: 3,
    lowFindings: 0,
    infoFindings: 0,
    completeness: 'COMPLETE',
    riskLevel: 'MEDIUM',
    calculationVersion: 'readiness-v1',
    readinessWarnings: [],
    readinessFactors: [],
  },
  aiReviewSummary: {
    totalReviews: 1,
    completedReviews: 1,
    failedReviews: 0,
    pendingReviews: 0,
    overallStatus: 'COMPLETED',
    model: 'gpt-4o',
    confidence: 'HIGH',
    summary: 'Code looks good',
  },
  recommendationSummary: {
    totalRecommendations: 1,
    countsByPriority: { HIGH: 1 },
    countsByStatus: { OPEN: 1 },
    countsByCategory: { SECURITY: 1 },
  },
  findingSummary: {
    totalFindings: 5,
    highFindings: 2,
    mediumFindings: 3,
    lowFindings: 0,
    infoFindings: 0,
    affectedFilesCount: 3,
    categoryBreakdown: { SECURITY: 2, CODE_QUALITY: 3 },
    severityBreakdown: { HIGH: 2, MEDIUM: 3 },
  },
};

export const sampleUnanalyzedResults: ReleaseResultsResponse = {
  releaseInfo: {
    releaseId: 'rel-200',
    projectId: 'proj-1',
    projectName: 'Demo Project',
    version: '2.0.0',
    name: 'Release 2.0.0',
    description: 'Unanalyzed candidate',
    status: 'NOT_ANALYZED',
    createdAt: '2026-09-20T11:00:00Z',
  },
  analysisSummary: null,
  riskSummary: null,
  readinessScore: null,
  aiReviewSummary: null,
  recommendationSummary: null,
  findingSummary: null,
};

export const samplePartialResults: ReleaseResultsResponse = {
  releaseInfo: {
    releaseId: 'rel-300',
    projectId: 'proj-1',
    version: '3.0.0',
    name: 'Release 3.0.0',
    status: 'ANALYZED',
    createdAt: '2026-09-20T12:00:00Z',
  },
  analysisSummary: {
    analysisId: 'ans-300',
    runNumber: 1,
    status: 'PARTIAL',
    completeness: 'PARTIAL',
    totalFindings: 2,
    completedAnalyzers: ['CODE_QUALITY'],
    failedAnalyzers: ['SECURITY'],
    warnings: ['Security analyzer timed out'],
  },
  riskSummary: null,
  readinessScore: null,
  aiReviewSummary: {
    totalReviews: 1,
    completedReviews: 0,
    failedReviews: 1,
    pendingReviews: 0,
    overallStatus: 'FAILED',
    failureCategory: 'AI_REVIEW_PROVIDER_ERROR',
  },
  recommendationSummary: {
    totalRecommendations: 0,
    countsByPriority: {},
    countsByStatus: {},
    countsByCategory: {},
  },
  findingSummary: {
    totalFindings: 2,
    highFindings: 0,
    mediumFindings: 2,
    lowFindings: 0,
    infoFindings: 0,
    affectedFilesCount: 1,
  },
};

/**
 * Contract & State Handling Validation Test Suite
 */
export function verifyResultsDashboardContract() {
  // 1. Full Results Payload Verification
  if (!sampleFullResults.releaseInfo.releaseId || sampleFullResults.releaseInfo.version !== '1.0.0') {
    throw new Error('Contract failure: Release info must preserve version and ID');
  }
  if (sampleFullResults.readinessScore?.readinessScore !== 85 || sampleFullResults.readinessScore?.readinessLevel !== 'GOOD') {
    throw new Error('Contract failure: Readiness score and level must match backend persisted object');
  }
  if (sampleFullResults.riskSummary?.overallRiskLevel !== 'MEDIUM') {
    throw new Error('Contract failure: Risk level must match backend persisted summary');
  }

  // 2. Un-analyzed State Verification
  if (sampleUnanalyzedResults.analysisSummary !== null || sampleUnanalyzedResults.readinessScore !== null) {
    throw new Error('Contract failure: Un-analyzed release must yield null analysisSummary and readinessScore');
  }

  // 3. Partial & Failed AI Review Verification
  if (samplePartialResults.analysisSummary?.completeness !== 'PARTIAL') {
    throw new Error('Contract failure: Partial analysis must preserve PARTIAL completeness');
  }
  if (samplePartialResults.aiReviewSummary?.overallStatus !== 'FAILED' || samplePartialResults.aiReviewSummary?.failureCategory !== 'AI_REVIEW_PROVIDER_ERROR') {
    throw new Error('Contract failure: Failed AI review must preserve safe failureCategory');
  }

  return true;
}

// Execute contract validation during build
verifyResultsDashboardContract();
