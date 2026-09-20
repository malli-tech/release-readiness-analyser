'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'next/navigation';
import Link from 'next/link';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import Alert from '@/components/ui/Alert';
import { fetchReleaseResults, ApiError } from '@/lib/api';
import { ReleaseResultsResponse } from '@/types/results';
import { formatDate } from '@/lib/utils';
import {
  GitBranch,
  FolderGit2,
  Calendar,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  Clock,
  Sparkles,
  BarChart3,
  Upload,
  RefreshCw,
  ArrowRight,
  ShieldAlert,
  FileCheck,
  Zap,
  ListOrdered,
  Info,
} from 'lucide-react';

export default function ReleaseResultsPage() {
  const params = useParams();
  const releaseId = params?.releaseId as string;

  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [results, setResults] = useState<ReleaseResultsResponse | null>(null);

  const loadResults = useCallback(async () => {
    if (!releaseId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await fetchReleaseResults(releaseId);
      setResults(data);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 404) {
          setError('Release evaluation results not found or you do not have permission to view this release.');
        } else {
          setError(err.message || 'Failed to fetch release results.');
        }
      } else {
        setError('Network error while retrieving evaluation results. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [releaseId]);

  useEffect(() => {
    loadResults();
  }, [loadResults]);

  const getRiskBadgeVariant = (level?: string): 'ready' | 'review' | 'critical' | 'warning' | 'info' | 'neutral' => {
    switch (level?.toUpperCase()) {
      case 'LOW':
        return 'ready';
      case 'MEDIUM':
        return 'review';
      case 'HIGH':
      case 'CRITICAL':
        return 'critical';
      default:
        return 'neutral';
    }
  };

  const getReadinessBadgeVariant = (level?: string): 'ready' | 'review' | 'critical' | 'warning' | 'info' | 'neutral' => {
    switch (level?.toUpperCase()) {
      case 'EXCELLENT':
      case 'GOOD':
      case 'READY':
        return 'ready';
      case 'FAIR':
      case 'NEEDS_REVIEW':
        return 'review';
      case 'POOR':
      case 'NOT_READY':
        return 'critical';
      default:
        return 'neutral';
    }
  };

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            {/* Loading State */}
            {loading && (
              <div className="py-24 text-center">
                <Spinner size="lg" label="Loading release evaluation results..." />
              </div>
            )}

            {/* Error State */}
            {!loading && error && (
              <div className="space-y-4">
                <Alert
                  type="error"
                  title="Results Unavailable"
                  message={error}
                />
                <div className="flex items-center gap-3">
                  <Button size="sm" onClick={loadResults} leftIcon={<RefreshCw className="w-3.5 h-3.5" />}>
                    Retry
                  </Button>
                  <Link href="/releases">
                    <Button variant="outline" size="sm">
                      Back to Releases
                    </Button>
                  </Link>
                </div>
              </div>
            )}

            {/* Results Content */}
            {!loading && !error && results && (
              <>
                {/* Header (Section A) */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 bg-white rounded-2xl border border-slate-200 shadow-xs">
                  <div className="space-y-1.5">
                    <div className="flex flex-wrap items-center gap-2 text-xs">
                      {results.releaseInfo.projectId && (
                        <Link
                          href={`/projects/${results.releaseInfo.projectId}`}
                          className="inline-flex items-center gap-1 font-semibold text-indigo-600 hover:underline"
                        >
                          <FolderGit2 className="w-3.5 h-3.5" />
                          <span>{results.releaseInfo.projectName || 'Project'}</span>
                        </Link>
                      )}
                      <span className="text-slate-400">•</span>
                      <span className="font-mono font-bold text-slate-700 bg-slate-100 px-2 py-0.5 rounded">
                        v{results.releaseInfo.version}
                      </span>
                      <Badge variant="neutral" dot size="sm">
                        {results.releaseInfo.status ? results.releaseInfo.status.replace(/_/g, ' ') : 'NOT ANALYZED'}
                      </Badge>
                      {results.analysisSummary?.completeness && (
                        <Badge
                          variant={results.analysisSummary.completeness === 'COMPLETE' ? 'ready' : results.analysisSummary.completeness === 'PARTIAL' ? 'review' : 'neutral'}
                          size="sm"
                        >
                          {results.analysisSummary.completeness}
                        </Badge>
                      )}
                    </div>

                    <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                      {results.releaseInfo.name} Evaluation Dashboard
                    </h1>

                    <p className="text-xs text-slate-500 flex items-center gap-2">
                      <Calendar className="w-3.5 h-3.5 text-slate-400" />
                      <span>
                        Created: {formatDate(results.releaseInfo.createdAt)}
                      </span>
                      {results.analysisSummary?.completedAt && (
                        <span>
                          • Analysis Completed: {formatDate(results.analysisSummary.completedAt)}
                        </span>
                      )}
                    </p>
                  </div>

                  <div className="flex items-center gap-2">
                    <Button variant="outline" size="sm" onClick={loadResults} leftIcon={<RefreshCw className="w-3.5 h-3.5" />}>
                      Refresh
                    </Button>
                  </div>
                </div>

                {/* Section: No Analysis Executed Yet */}
                {(!results.analysisSummary || results.releaseInfo.status === 'NOT_ANALYZED') && (
                  <Card>
                    <CardContent className="py-12 text-center space-y-4">
                      <div className="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto">
                        <Clock className="w-6 h-6" />
                      </div>
                      <div className="space-y-1">
                        <h3 className="text-base font-bold text-slate-900">Analysis Not Yet Executed</h3>
                        <p className="text-xs text-slate-500 max-w-md mx-auto">
                          This release candidate has been created, but no static analysis scan has been completed yet. Upload code to trigger the analysis pipeline.
                        </p>
                      </div>
                      <Link href={`/releases/${releaseId}/upload`}>
                        <Button size="sm" leftIcon={<Upload className="w-3.5 h-3.5" />}>
                          Upload & Run Scan
                        </Button>
                      </Link>
                    </CardContent>
                  </Card>
                )}

                {/* Section: Partial Analysis Warning Callout */}
                {results.analysisSummary?.completeness === 'PARTIAL' && (
                  <Alert
                    type="warning"
                    title="Partial Analysis Scanned"
                    message="Some analyzers encountered file-level warnings or incomplete source manifests. Metrics and scores represent partial codebase coverage."
                  />
                )}

                {/* Analyzed Results Display */}
                {results.analysisSummary && results.releaseInfo.status !== 'NOT_ANALYZED' && (
                  <>
                    {/* Top Metric Cards: Readiness (Section B) & Risk (Section C) */}
                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                      {/* Readiness Score Card */}
                      <div className="lg:col-span-6 flex">
                        <Card className="w-full p-6 flex flex-col justify-between space-y-4">
                          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                            <div className="flex items-center gap-2">
                              <Sparkles className="w-4 h-4 text-indigo-600" />
                              <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider">
                                Readiness Score
                              </h3>
                            </div>
                            {results.readinessScore?.calculationVersion && (
                              <span className="text-[10px] font-mono text-slate-400">
                                {results.readinessScore.calculationVersion}
                              </span>
                            )}
                          </div>

                          {results.readinessScore ? (
                            <div className="space-y-3">
                              <div className="flex items-baseline gap-3">
                                <span className="text-4xl font-black text-slate-900 tracking-tight">
                                  {results.readinessScore.readinessScore != null
                                    ? Number(results.readinessScore.readinessScore).toFixed(1)
                                    : 'N/A'}
                                </span>
                                <span className="text-xs text-slate-400 font-semibold">/ 100</span>
                                <Badge variant={getReadinessBadgeVariant(results.readinessScore.readinessLevel)} size="md" dot>
                                  {results.readinessScore.readinessLevel || 'UNKNOWN'}
                                </Badge>
                              </div>

                              <div className="flex items-center gap-4 text-xs text-slate-600">
                                <div>
                                  <span className="text-slate-400 font-medium">Confidence: </span>
                                  <span className="font-semibold text-slate-800">
                                    {results.readinessScore.confidence || 'UNKNOWN'}
                                  </span>
                                </div>
                                {results.readinessScore.riskLevel && (
                                  <div>
                                    <span className="text-slate-400 font-medium">Risk Gate: </span>
                                    <span className="font-semibold text-slate-800">
                                      {results.readinessScore.riskLevel}
                                    </span>
                                  </div>
                                )}
                              </div>

                              {results.readinessScore.readinessWarnings && results.readinessScore.readinessWarnings.length > 0 && (
                                <div className="space-y-1 text-xs text-amber-800 bg-amber-50 p-2.5 rounded-lg border border-amber-200">
                                  <p className="font-semibold text-[11px] uppercase tracking-wider">Readiness Warnings:</p>
                                  <ul className="list-disc list-inside space-y-0.5 text-[11px]">
                                    {results.readinessScore.readinessWarnings.map((w, idx) => (
                                      <li key={idx}>{w}</li>
                                    ))}
                                  </ul>
                                </div>
                              )}
                            </div>
                          ) : (
                            <p className="text-xs text-slate-400 py-4">Readiness score not available for this analysis.</p>
                          )}
                        </Card>
                      </div>

                      {/* Risk Summary Card */}
                      <div className="lg:col-span-6 flex">
                        <Card className="w-full p-6 flex flex-col justify-between space-y-4">
                          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
                            <div className="flex items-center gap-2">
                              <ShieldAlert className="w-4 h-4 text-rose-600" />
                              <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider">
                                Risk Assessment
                              </h3>
                            </div>
                            {results.riskSummary?.calculationVersion && (
                              <span className="text-[10px] font-mono text-slate-400">
                                {results.riskSummary.calculationVersion}
                              </span>
                            )}
                          </div>

                          {results.riskSummary ? (
                            <div className="space-y-3">
                              <div className="flex items-center gap-3">
                                <Badge variant={getRiskBadgeVariant(results.riskSummary.overallRiskLevel)} size="md" dot>
                                  {results.riskSummary.overallRiskLevel || 'UNKNOWN'} RISK
                                </Badge>
                                <div className="text-xs text-slate-600">
                                  <span className="font-bold text-slate-900">
                                    {results.riskSummary.weightedRiskPoints != null
                                      ? Number(results.riskSummary.weightedRiskPoints).toFixed(1)
                                      : 0}
                                  </span>
                                  <span className="text-slate-400"> weighted risk points</span>
                                </div>
                              </div>

                              <div className="grid grid-cols-4 gap-2 text-center text-xs pt-1">
                                <div className="p-2 bg-rose-50 rounded-lg border border-rose-100">
                                  <span className="block text-[10px] font-bold text-rose-600 uppercase">High/Crit</span>
                                  <span className="font-bold text-rose-900">{results.riskSummary.highFindings}</span>
                                </div>
                                <div className="p-2 bg-amber-50 rounded-lg border border-amber-100">
                                  <span className="block text-[10px] font-bold text-amber-600 uppercase">Medium</span>
                                  <span className="font-bold text-amber-900">{results.riskSummary.mediumFindings}</span>
                                </div>
                                <div className="p-2 bg-sky-50 rounded-lg border border-sky-100">
                                  <span className="block text-[10px] font-bold text-sky-600 uppercase">Low</span>
                                  <span className="font-bold text-sky-900">{results.riskSummary.lowFindings}</span>
                                </div>
                                <div className="p-2 bg-slate-50 rounded-lg border border-slate-200">
                                  <span className="block text-[10px] font-bold text-slate-500 uppercase">Info</span>
                                  <span className="font-bold text-slate-800">{results.riskSummary.infoFindings}</span>
                                </div>
                              </div>
                            </div>
                          ) : (
                            <p className="text-xs text-slate-400 py-4">Risk summary not available for this analysis.</p>
                          )}
                        </Card>
                      </div>
                    </div>

                    {/* Findings & Category Breakdown (Section D & H) */}
                    <Card>
                      <CardHeader className="flex flex-row items-center justify-between pb-3">
                        <div className="flex items-center gap-2">
                          <BarChart3 className="w-4 h-4 text-indigo-600" />
                          <CardTitle className="text-sm">Findings & Impact Overview</CardTitle>
                        </div>
                        {results.findingSummary && (
                          <span className="text-xs font-semibold text-slate-500">
                            {results.findingSummary.totalFindings} total findings across {results.findingSummary.affectedFilesCount} files
                          </span>
                        )}
                      </CardHeader>

                      <CardContent className="space-y-4">
                        {results.findingSummary && (
                          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                            {/* Category breakdown entries */}
                            {Object.entries(results.findingSummary.categoryBreakdown || {}).map(([cat, count]) => (
                              <div key={cat} className="p-3 bg-slate-50 rounded-xl border border-slate-200 flex items-center justify-between">
                                <span className="text-xs font-semibold text-slate-700">{cat.replace(/_/g, ' ')}</span>
                                <span className="text-xs font-bold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded-full border border-indigo-100">
                                  {count}
                                </span>
                              </div>
                            ))}
                          </div>
                        )}
                      </CardContent>
                    </Card>

                    {/* Analyzer Execution Status (Section E) */}
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center gap-2">
                          <FileCheck className="w-4 h-4 text-indigo-600" />
                          <CardTitle className="text-sm">Static Analyzer Pipeline Execution</CardTitle>
                        </div>
                      </CardHeader>

                      <CardContent className="space-y-4">
                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
                          {/* Completed Analyzers */}
                          <div className="p-3 bg-emerald-50/60 rounded-xl border border-emerald-100 space-y-1.5">
                            <div className="flex items-center gap-1.5 text-emerald-800 font-bold text-xs uppercase tracking-wider">
                              <CheckCircle2 className="w-4 h-4 text-emerald-600 shrink-0" />
                              <span>Completed ({results.analysisSummary.completedAnalyzers?.length || 0})</span>
                            </div>
                            <div className="flex flex-wrap gap-1 pt-1">
                              {results.analysisSummary.completedAnalyzers?.map((name) => (
                                <span key={name} className="text-[11px] font-semibold bg-emerald-100 text-emerald-800 px-2 py-0.5 rounded">
                                  {name}
                                </span>
                              )) || <span className="text-slate-400">None</span>}
                            </div>
                          </div>

                          {/* Failed Analyzers */}
                          <div className="p-3 bg-rose-50/60 rounded-xl border border-rose-100 space-y-1.5">
                            <div className="flex items-center gap-1.5 text-rose-800 font-bold text-xs uppercase tracking-wider">
                              <XCircle className="w-4 h-4 text-rose-600 shrink-0" />
                              <span>Failed ({results.analysisSummary.failedAnalyzers?.length || 0})</span>
                            </div>
                            <div className="flex flex-wrap gap-1 pt-1">
                              {results.analysisSummary.failedAnalyzers?.map((name) => (
                                <span key={name} className="text-[11px] font-semibold bg-rose-100 text-rose-800 px-2 py-0.5 rounded">
                                  {name}
                                </span>
                              )) || <span className="text-slate-400">None</span>}
                            </div>
                          </div>

                          {/* Skipped Analyzers */}
                          <div className="p-3 bg-slate-100/70 rounded-xl border border-slate-200 space-y-1.5">
                            <div className="flex items-center gap-1.5 text-slate-700 font-bold text-xs uppercase tracking-wider">
                              <Info className="w-4 h-4 text-slate-500 shrink-0" />
                              <span>Skipped ({results.analysisSummary.skippedAnalyzers?.length || 0})</span>
                            </div>
                            <div className="flex flex-wrap gap-1 pt-1">
                              {results.analysisSummary.skippedAnalyzers?.map((name) => (
                                <span key={name} className="text-[11px] font-semibold bg-slate-200 text-slate-700 px-2 py-0.5 rounded">
                                  {name}
                                </span>
                              )) || <span className="text-slate-400">None</span>}
                            </div>
                          </div>
                        </div>

                        {/* Analysis Warnings */}
                        {results.analysisSummary.warnings && results.analysisSummary.warnings.length > 0 && (
                          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs text-slate-700 space-y-1">
                            <p className="font-bold text-slate-900">Analysis Logs & Warnings:</p>
                            <ul className="list-disc list-inside space-y-0.5 text-slate-600 text-[11px]">
                              {results.analysisSummary.warnings.map((w, idx) => (
                                <li key={idx}>{w}</li>
                              ))}
                            </ul>
                          </div>
                        )}
                      </CardContent>
                    </Card>

                    {/* AI Review (Section F) & Recommendation Summary (Section G) */}
                    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                      {/* AI Review Summary */}
                      <div className="lg:col-span-6 flex">
                        <Card className="w-full p-5 flex flex-col justify-between space-y-3">
                          <div className="flex items-center justify-between border-b border-slate-100 pb-2.5">
                            <div className="flex items-center gap-2">
                              <Zap className="w-4 h-4 text-indigo-600" />
                              <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider">
                                AI Review Summary
                              </h3>
                            </div>
                            {results.aiReviewSummary && (
                              <Badge
                                variant={results.aiReviewSummary.overallStatus === 'COMPLETED' ? 'ready' : results.aiReviewSummary.overallStatus === 'FAILED' ? 'critical' : 'neutral'}
                                size="sm"
                              >
                                {results.aiReviewSummary.overallStatus}
                              </Badge>
                            )}
                          </div>

                          {results.aiReviewSummary ? (
                            <div className="space-y-2 text-xs">
                              <div className="flex items-center gap-4 text-slate-600">
                                <span>Total Reviews: <strong className="text-slate-900">{results.aiReviewSummary.totalReviews}</strong></span>
                                <span>Completed: <strong className="text-emerald-700">{results.aiReviewSummary.completedReviews}</strong></span>
                                {results.aiReviewSummary.failedReviews > 0 && (
                                  <span>Failed: <strong className="text-rose-700">{results.aiReviewSummary.failedReviews}</strong></span>
                                )}
                              </div>

                              {results.aiReviewSummary.model && (
                                <p className="text-slate-500">
                                  Model: <span className="font-mono font-semibold text-slate-800">{results.aiReviewSummary.model}</span>
                                  {results.aiReviewSummary.confidence && (
                                    <span> • Confidence: <strong className="text-slate-800">{results.aiReviewSummary.confidence}</strong></span>
                                  )}
                                </p>
                              )}

                              {results.aiReviewSummary.summary && (
                                <p className="text-slate-700 bg-slate-50 p-2.5 rounded-lg border border-slate-200 leading-relaxed italic">
                                  "{results.aiReviewSummary.summary}"
                                </p>
                              )}

                              {results.aiReviewSummary.failureCategory && (
                                <Alert
                                  type="error"
                                  title="AI Review Failure"
                                  message={`Review generation status: ${results.aiReviewSummary.failureCategory}`}
                                />
                              )}
                            </div>
                          ) : (
                            <p className="text-xs text-slate-400">No AI review generated for this analysis.</p>
                          )}
                        </Card>
                      </div>

                      {/* Recommendation Summary */}
                      <div className="lg:col-span-6 flex">
                        <Card className="w-full p-5 flex flex-col justify-between space-y-3">
                          <div className="flex items-center justify-between border-b border-slate-100 pb-2.5">
                            <div className="flex items-center gap-2">
                              <ListOrdered className="w-4 h-4 text-indigo-600" />
                              <h3 className="text-sm font-bold text-slate-900 uppercase tracking-wider">
                                Actionable Recommendations
                              </h3>
                            </div>
                            {results.recommendationSummary && (
                              <span className="font-bold text-xs text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded-full border border-indigo-100">
                                {results.recommendationSummary.totalRecommendations}
                              </span>
                            )}
                          </div>

                          {results.recommendationSummary ? (
                            <div className="space-y-3 text-xs">
                              <div>
                                <p className="text-[11px] font-bold text-slate-500 uppercase tracking-wider mb-1.5">Priority Breakdown:</p>
                                <div className="flex flex-wrap gap-2">
                                  {Object.entries(results.recommendationSummary.countsByPriority || {}).map(([prio, count]) => (
                                    <span key={prio} className="px-2 py-1 bg-slate-100 text-slate-800 font-semibold rounded text-[11px] flex items-center gap-1">
                                      <span>{prio}:</span>
                                      <strong className="text-indigo-600">{count}</strong>
                                    </span>
                                  ))}
                                  {Object.keys(results.recommendationSummary.countsByPriority || {}).length === 0 && (
                                    <span className="text-slate-400">No recommendations mapped</span>
                                  )}
                                </div>
                              </div>
                            </div>
                          ) : (
                            <p className="text-xs text-slate-400">No recommendations summary available.</p>
                          )}
                        </Card>
                      </div>
                    </div>
                  </>
                )}
              </>
            )}
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
