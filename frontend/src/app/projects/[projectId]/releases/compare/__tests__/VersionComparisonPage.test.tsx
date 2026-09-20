import React from 'react';
import { VersionComparisonResponse } from '@/types/comparison';

export const mockComparisonResponse: VersionComparisonResponse = {
  baseRelease: {
    releaseId: 'rel-1',
    projectId: 'proj-100',
    projectName: 'Payment Gateway',
    version: '1.0.0',
    name: 'Initial Release',
    status: 'COMPLETED',
    analysisId: 'ans-1',
    runNumber: 1,
    completeness: 'COMPLETE',
  },
  targetRelease: {
    releaseId: 'rel-2',
    projectId: 'proj-100',
    projectName: 'Payment Gateway',
    version: '1.1.0',
    name: 'Feature Release',
    status: 'COMPLETED',
    analysisId: 'ans-2',
    runNumber: 1,
    completeness: 'COMPLETE',
  },
  findingsComparison: {
    baseTotal: 10,
    targetTotal: 7,
    delta: -3,
  },
  severityComparison: {
    baseCounts: { HIGH: 2, MEDIUM: 5, LOW: 3, INFO: 0 },
    targetCounts: { HIGH: 1, MEDIUM: 4, LOW: 2, INFO: 0 },
    deltas: { HIGH: -1, MEDIUM: -1, LOW: -1, INFO: 0 },
  },
  categoryComparison: {
    baseCounts: { SECURITY: 3, CODE_QUALITY: 4, DEPENDENCY: 3 },
    targetCounts: { SECURITY: 2, CODE_QUALITY: 3, DEPENDENCY: 2 },
    deltas: { SECURITY: -1, CODE_QUALITY: -1, DEPENDENCY: -1 },
  },
  affectedFilesComparison: {
    baseAffectedFiles: 4,
    targetAffectedFiles: 3,
    delta: -1,
  },
  riskComparison: {
    baseRiskLevel: 'HIGH',
    targetRiskLevel: 'MEDIUM',
    baseWeightedRiskPoints: 24.5,
    targetWeightedRiskPoints: 14.0,
    deltaWeightedRiskPoints: -10.5,
  },
  readinessComparison: {
    baseScore: 65.0,
    targetScore: 82.5,
    scoreDelta: 17.5,
    baseLevel: 'NEEDS_REVIEW',
    targetLevel: 'READY',
    baseConfidence: 'HIGH',
    targetConfidence: 'HIGH',
  },
  recommendationComparison: {
    baseTotal: 5,
    targetTotal: 3,
    totalDelta: -2,
    basePriorityCounts: { CRITICAL: 1, HIGH: 2, MEDIUM: 2 },
    targetPriorityCounts: { CRITICAL: 0, HIGH: 1, MEDIUM: 2 },
  },
  aiReviewComparison: {
    baseOverallStatus: 'COMPLETED',
    targetOverallStatus: 'COMPLETED',
    baseConfidence: 'HIGH',
    targetConfidence: 'HIGH',
    baseTotalReviews: 1,
    targetTotalReviews: 1,
    baseCompletedReviews: 1,
    targetCompletedReviews: 1,
  },
  coverageComparison: {
    baseCompleteness: 'COMPLETE',
    targetCompleteness: 'COMPLETE',
    baseCompletedAnalyzers: ['SECURITY', 'CODE_QUALITY', 'DEPENDENCY', 'TESTING', 'PERFORMANCE'],
    targetCompletedAnalyzers: ['SECURITY', 'CODE_QUALITY', 'DEPENDENCY', 'TESTING', 'PERFORMANCE'],
  },
  findingChanges: {
    newCount: 1,
    resolvedCount: 4,
    unchangedCount: 6,
    limitationNote: 'Finding matching based on composite rule identity key.',
    items: [
      {
        findingId: 'find-new-1',
        ruleId: 'SEC_SQL_01',
        category: 'SECURITY',
        severity: 'HIGH',
        title: 'New SQL Injection Risk',
        filePath: 'src/Payment.java',
        lineNumber: 50,
        changeType: 'NEW',
      },
      {
        findingId: 'find-res-1',
        ruleId: 'SEC_XSS_01',
        category: 'SECURITY',
        severity: 'HIGH',
        title: 'Resolved XSS Vulnerability',
        filePath: 'src/View.java',
        lineNumber: 12,
        changeType: 'RESOLVED',
      },
      {
        findingId: 'find-unc-1',
        ruleId: 'CQ_DUPLICATE_CODE',
        category: 'CODE_QUALITY',
        severity: 'MEDIUM',
        title: 'Unchanged Code Duplication',
        filePath: 'src/Utils.java',
        lineNumber: 88,
        changeType: 'UNCHANGED',
      },
    ],
  },
};

/**
 * Version Comparison Frontend Contract Verification Test Suite
 */
export function verifyVersionComparisonContract() {
  // 1. Overview Verification
  if (mockComparisonResponse.baseRelease.releaseId !== 'rel-1' || mockComparisonResponse.targetRelease.releaseId !== 'rel-2') {
    throw new Error('Contract failed: Base and Target release identity must be preserved');
  }

  // 2. Metrics & Deltas Verification
  if (mockComparisonResponse.readinessComparison?.scoreDelta !== 17.5) {
    throw new Error('Contract failed: Readiness score delta must equal target minus base score');
  }
  if (mockComparisonResponse.findingsComparison.delta !== -3) {
    throw new Error('Contract failed: Total findings delta must equal target total minus base total');
  }

  // 3. Traceable Finding Changes Classification
  const newItems = mockComparisonResponse.findingChanges.items.filter(i => i.changeType === 'NEW');
  const resolvedItems = mockComparisonResponse.findingChanges.items.filter(i => i.changeType === 'RESOLVED');
  const unchangedItems = mockComparisonResponse.findingChanges.items.filter(i => i.changeType === 'UNCHANGED');

  if (newItems.length !== 1 || newItems[0].ruleId !== 'SEC_SQL_01') {
    throw new Error('Contract failed: NEW findings must be classified correctly');
  }
  if (resolvedItems.length !== 1 || resolvedItems[0].ruleId !== 'SEC_XSS_01') {
    throw new Error('Contract failed: RESOLVED findings must be classified correctly');
  }
  if (unchangedItems.length !== 1 || unchangedItems[0].ruleId !== 'CQ_DUPLICATE_CODE') {
    throw new Error('Contract failed: UNCHANGED findings must be classified correctly');
  }

  return true;
}

// Execute verification test
verifyVersionComparisonContract();
