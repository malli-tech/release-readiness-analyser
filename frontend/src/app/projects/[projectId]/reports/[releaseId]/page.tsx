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
import { fetchReleaseReport, ApiError } from '@/lib/api';
import { ReleaseReport } from '@/types/report';
import { formatDate } from '@/lib/utils';
import {
  FileText,
  ArrowLeft,
  RefreshCw,
  GitBranch,
  BarChart3,
  Calendar,
  Layers,
  Info,
  Clock,
  UploadCloud,
} from 'lucide-react';

export default function SingleReleaseReportPage() {
  const params = useParams();
  const projectId = params?.projectId as string;
  const releaseId = params?.releaseId as string;

  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [report, setReport] = useState<ReleaseReport | null>(null);

  const loadReport = useCallback(async () => {
    if (!projectId || !releaseId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await fetchReleaseReport(projectId, releaseId);
      setReport(data);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message || 'Failed to fetch release report.');
      } else {
        setError('Network error while retrieving release report details. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [projectId, releaseId]);

  useEffect(() => {
    loadReport();
  }, [loadReport]);

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            {/* Header & Breadcrumb */}
            <div>
              <div className="flex items-center gap-2 mb-1">
                <Link
                  href={`/projects/${projectId}/reports`}
                  className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1"
                >
                  <ArrowLeft className="w-3 h-3" /> Back to Project Reports
                </Link>
              </div>
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
                    <FileText className="w-6 h-6 text-indigo-600" />
                    {report ? report.reportNumber : 'Release Report'}
                  </h1>
                  <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
                    Release report identity, candidate status, and hierarchy context.
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Link href={`/releases/${releaseId}/results`}>
                    <Button variant="outline" size="sm" leftIcon={<BarChart3 className="w-3.5 h-3.5" />}>
                      View Results Dashboard
                    </Button>
                  </Link>
                </div>
              </div>
            </div>

            {/* Loading State */}
            {loading && (
              <div className="py-20 text-center bg-white rounded-xl border border-slate-200">
                <Spinner size="lg" label="Loading release report..." />
              </div>
            )}

            {/* Error State */}
            {!loading && error && (
              <div className="space-y-4">
                <Alert type="error" title="Report Unavailable" message={error} />
                <Button size="sm" onClick={loadReport} leftIcon={<RefreshCw className="w-3.5 h-3.5" />}>
                  Retry
                </Button>
              </div>
            )}

            {/* Report Content */}
            {!loading && !error && report && (
              <div className="space-y-6">
                {/* Identity Card */}
                <Card>
                  <CardHeader className="pb-3 border-b border-slate-100">
                    <div className="flex items-center justify-between">
                      <CardTitle className="text-sm font-bold flex items-center gap-2">
                        <Layers className="w-4 h-4 text-indigo-600" /> Report Identity & Hierarchy
                      </CardTitle>
                      <Badge
                        variant={report.status === 'COMPLETED' ? 'ready' : report.status === 'ANALYZING' ? 'review' : 'neutral'}
                        size="md"
                        dot
                      >
                        {report.status ? report.status.replace(/_/g, ' ') : 'NOT ANALYZED'}
                      </Badge>
                    </div>
                  </CardHeader>

                  <CardContent className="pt-4">
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 text-xs">
                      <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-1">
                        <span className="text-[10px] font-bold text-slate-400 uppercase">Report Number</span>
                        <span className="block font-mono font-bold text-indigo-600 text-sm">{report.reportNumber}</span>
                      </div>

                      <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-1">
                        <span className="text-[10px] font-bold text-slate-400 uppercase">Release Version</span>
                        <span className="block font-mono font-bold text-slate-900 text-sm">v{report.version}</span>
                      </div>

                      <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-1">
                        <span className="text-[10px] font-bold text-slate-400 uppercase">Release ID</span>
                        <span className="block font-mono text-slate-700 text-xs truncate">{report.releaseId}</span>
                      </div>

                      <div className="p-3 bg-slate-50 rounded-xl border border-slate-200 space-y-1">
                        <span className="text-[10px] font-bold text-slate-400 uppercase">Project ID</span>
                        <span className="block font-mono text-slate-700 text-xs truncate">{report.projectId}</span>
                      </div>
                    </div>

                    <div className="mt-4 pt-4 border-t border-slate-100 flex flex-wrap items-center justify-between gap-3 text-xs text-slate-500">
                      <div className="flex items-center gap-2">
                        <Calendar className="w-3.5 h-3.5 text-slate-400" />
                        <span>Generated: {formatDate(report.createdAt)}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        <Info className="w-3.5 h-3.5 text-slate-400" />
                        <span>Hierarchy: Project ({projectId}) → Release ({releaseId}) → Report ({report.id})</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Unanalyzed Release State Card */}
                {(!report.latestAnalysisId || report.status === 'NOT_ANALYZED') ? (
                  <Card>
                    <CardHeader className="pb-3 border-b border-slate-100">
                      <CardTitle className="text-sm font-bold flex items-center gap-2">
                        <Clock className="w-4 h-4 text-amber-600" /> Report Not Generated Yet
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="py-10 text-center space-y-4">
                      <div className="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto border border-amber-200">
                        <Clock className="w-6 h-6" />
                      </div>
                      <div className="space-y-1 max-w-md mx-auto">
                        <h3 className="text-sm font-bold text-slate-900">This release candidate has not been analyzed yet.</h3>
                        <p className="text-xs text-slate-500">
                          Run static analysis to generate the release readiness report, findings, risk assessment, and readiness score.
                        </p>
                      </div>
                      <div className="flex items-center justify-center gap-2.5 pt-2">
                        <Link href={`/releases/${releaseId}/upload`}>
                          <Button size="sm" variant="outline" leftIcon={<UploadCloud className="w-3.5 h-3.5" />}>
                            Upload Source Archive
                          </Button>
                        </Link>
                        <Link href={`/releases/${releaseId}/analysis`}>
                          <Button size="sm" leftIcon={<BarChart3 className="w-3.5 h-3.5" />}>
                            Start / View Analysis
                          </Button>
                        </Link>
                      </div>
                    </CardContent>
                  </Card>
                ) : (
                  /* Analyzed Release Report Summary Card */
                  <Card>
                    <CardHeader className="pb-3 border-b border-slate-100">
                      <CardTitle className="text-sm font-bold flex items-center gap-2">
                        <BarChart3 className="w-4 h-4 text-indigo-600" /> Release Evaluation Metrics
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="pt-4 space-y-4">
                      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 text-xs">
                        <div className="p-4 bg-indigo-50/50 rounded-xl border border-indigo-100 space-y-1">
                          <span className="text-[10px] font-bold text-slate-500 uppercase">Readiness Score</span>
                          <span className="block font-bold text-indigo-600 text-xl">
                            {report.readinessScore != null ? `${Number(report.readinessScore).toFixed(1)} / 100` : '—'}
                          </span>
                        </div>
                        <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-1">
                          <span className="text-[10px] font-bold text-slate-500 uppercase">Readiness Level</span>
                          <span className="block font-bold text-slate-800 text-base">
                            {report.readinessLevel ? report.readinessLevel.replace(/_/g, ' ') : '—'}
                          </span>
                        </div>
                        <div className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-1">
                          <span className="text-[10px] font-bold text-slate-500 uppercase">Overall Risk Level</span>
                          <span className="block font-bold text-slate-800 text-base">
                            {report.riskLevel ? report.riskLevel.replace(/_/g, ' ') : '—'}
                          </span>
                        </div>
                      </div>
                      <div className="flex justify-end pt-2">
                        <Link href={`/releases/${releaseId}/results`}>
                          <Button size="sm" leftIcon={<BarChart3 className="w-3.5 h-3.5" />}>
                            View Full Results Dashboard
                          </Button>
                        </Link>
                      </div>
                    </CardContent>
                  </Card>
                )}
              </div>
            )}
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
