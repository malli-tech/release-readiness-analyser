import React from 'react';
import { Release } from '@/types/release';

export interface ReleaseRowActions {
  detailsHref: string;
  uploadHref: string;
  analysisHref: string;
  reportHref: string;
}

export function getReleaseRowActionHrefs(release: Release): ReleaseRowActions {
  return {
    detailsHref: `/releases/${release.id}`,
    uploadHref: `/releases/${release.id}/upload`,
    analysisHref: `/releases/${release.id}/analysis`,
    reportHref: `/projects/${release.projectId}/reports/${release.id}`,
  };
}

/**
 * Release Action Navigation Route Verification Test
 */
export function verifyReleaseActionNavigationContracts() {
  const sampleV1Release: Release = {
    id: 'rel-v1-001',
    projectId: 'proj-alpha-123',
    version: '1.0.0',
    name: 'Version 1.0 Release Candidate',
    status: 'ANALYZED',
    createdAt: '2026-09-20T10:00:00Z',
  };

  const sampleV2Release: Release = {
    id: 'rel-v2-002',
    projectId: 'proj-alpha-123',
    version: '2.0.0',
    name: 'Version 2.0 Release Candidate',
    status: 'READY',
    createdAt: '2026-09-21T09:00:00Z',
  };

  const v1Actions = getReleaseRowActionHrefs(sampleV1Release);
  const v2Actions = getReleaseRowActionHrefs(sampleV2Release);

  // 1. Verify v1 Report Href exact match
  if (v1Actions.reportHref !== '/projects/proj-alpha-123/reports/rel-v1-001') {
    throw new Error(`v1 Report href mismatch. Expected '/projects/proj-alpha-123/reports/rel-v1-001', got '${v1Actions.reportHref}'`);
  }

  // 2. Verify v2 Report Href exact match
  if (v2Actions.reportHref !== '/projects/proj-alpha-123/reports/rel-v2-002') {
    throw new Error(`v2 Report href mismatch. Expected '/projects/proj-alpha-123/reports/rel-v2-002', got '${v2Actions.reportHref}'`);
  }

  // 3. Verify all four actions exist for v1
  if (!v1Actions.detailsHref || !v1Actions.uploadHref || !v1Actions.analysisHref || !v1Actions.reportHref) {
    throw new Error('All 4 release row actions (Details, Upload, Analysis, Report) must generate valid hrefs');
  }

  // 4. Verify project isolation (no cross-project leakage)
  const crossProjectRelease: Release = {
    id: 'rel-v1-999',
    projectId: 'proj-beta-456',
    version: '1.0.0',
    name: 'Beta Release',
    status: 'NOT_ANALYZED',
    createdAt: '2026-09-21T10:00:00Z',
  };
  const betaActions = getReleaseRowActionHrefs(crossProjectRelease);
  if (betaActions.reportHref !== '/projects/proj-beta-456/reports/rel-v1-999') {
    throw new Error(`Cross-project report href leak detected. Expected '/projects/proj-beta-456/reports/rel-v1-999', got '${betaActions.reportHref}'`);
  }

  return true;
}

verifyReleaseActionNavigationContracts();
