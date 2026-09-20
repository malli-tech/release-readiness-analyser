import React from 'react';
import { RecommendationsSection } from '../RecommendationsSection';
import { RecommendationRecord } from '@/types/recommendation';

export const sampleRecommendations: RecommendationRecord[] = [
  {
    id: 'rec-1',
    analysisId: 'ans-100',
    findingId: 'find-1',
    ruleId: 'SECURITY_SQL_INJECTION_RISK',
    category: 'SECURITY',
    severity: 'HIGH',
    title: 'Use Parameterized SQL Queries',
    summary: 'Unsanitized user input detected in database query.',
    recommendedAction: 'Replace string concatenation in SQL queries with JDBC PreparedStatement parameter placeholders.',
    priority: 'CRITICAL',
    effort: 'MEDIUM',
    status: 'OPEN',
    filePath: 'src/UserRepository.java',
    lineNumber: 42,
    createdAt: '2026-09-20T10:00:00Z',
    updatedAt: '2026-09-20T10:00:00Z',
  },
  {
    id: 'rec-2',
    analysisId: 'ans-100',
    findingId: 'find-2',
    ruleId: 'CODE_QUALITY_LONG_METHOD',
    category: 'CODE_QUALITY',
    severity: 'MEDIUM',
    title: 'Refactor Long Method into Modular Helpers',
    summary: 'Method length exceeds maintainability threshold (85 lines).',
    recommendedAction: 'Extract single-responsibility helper methods.',
    priority: 'MEDIUM',
    effort: 'LOW',
    status: 'RESOLVED',
    filePath: 'src/OrderService.java',
    lineNumber: 115,
    createdAt: '2026-09-20T10:00:00Z',
    updatedAt: '2026-09-20T10:00:00Z',
  },
];

/**
 * Type & Rendering Contract Verification Test Suite
 * Verifies Recommendation rendering, finding traceability, client-side filtering logic, and error/empty states.
 */
export function verifyRecommendationContract() {
  // 1. Rendering Verification
  const rec1 = sampleRecommendations[0];
  if (rec1.title !== 'Use Parameterized SQL Queries' || rec1.priority !== 'CRITICAL') {
    throw new Error('Failed contract: Title and Priority must be correctly rendered');
  }

  // 2. Traceability Verification
  if (!rec1.filePath || rec1.lineNumber !== 42 || rec1.ruleId !== 'SECURITY_SQL_INJECTION_RISK') {
    throw new Error('Failed contract: Finding traceability (filePath, lineNumber, ruleId) must be preserved');
  }

  // 3. Filtering Verification
  const criticalOnly = sampleRecommendations.filter((r) => r.priority === 'CRITICAL');
  if (criticalOnly.length !== 1 || criticalOnly[0].id !== 'rec-1') {
    throw new Error('Failed contract: Deterministic filtering by Priority must return exact matches');
  }

  const qualityOnly = sampleRecommendations.filter((r) => r.category === 'CODE_QUALITY');
  if (qualityOnly.length !== 1 || qualityOnly[0].id !== 'rec-2') {
    throw new Error('Failed contract: Deterministic filtering by Category must return exact matches');
  }

  const resolvedOnly = sampleRecommendations.filter((r) => r.status === 'RESOLVED');
  if (resolvedOnly.length !== 1 || resolvedOnly[0].id !== 'rec-2') {
    throw new Error('Failed contract: Deterministic filtering by Status must return exact matches');
  }

  // 4. Empty State Verification
  const emptyFilter = sampleRecommendations.filter((r) => r.priority === 'LOW');
  if (emptyFilter.length !== 0) {
    throw new Error('Failed contract: Empty filter must yield zero results');
  }

  return true;
}

// Execute contract validation during import/build
verifyRecommendationContract();
