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
import { fetchProjectReports, ApiError } from '@/lib/api';
import { ReleaseReport } from '@/types/report';
import { formatDate } from '@/lib/utils';
import {
  FileText,
  FolderGit2,
  ArrowLeft,
  Eye,
  RefreshCw,
  GitBranch,
} from 'lucide-react';

export default function ProjectReportsPage() {
  const params = useParams();
  const projectId = params?.projectId as string;

  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [reports, setReports] = useState<ReleaseReport[]>([]);

  const loadReports = useCallback(async () => {
    if (!projectId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await fetchProjectReports(projectId);
      setReports(data || []);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message || 'Failed to fetch project release reports.');
      } else {
        setError('Network error while retrieving release reports. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    loadReports();
  }, [loadReports]);

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
                  href={`/projects/${projectId}`}
                  className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1"
                >
                  <ArrowLeft className="w-3 h-3" /> Back to Project
                </Link>
              </div>
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
                    <FileText className="w-6 h-6 text-indigo-600" /> Project Reports
                  </h1>
                  <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
                    Structured release readiness reports organized by project release candidates.
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <Button variant="outline" size="sm" onClick={loadReports} leftIcon={<RefreshCw className="w-3.5 h-3.5" />}>
                    Refresh Reports
                  </Button>
                </div>
              </div>
            </div>

            {/* Loading State */}
            {loading && (
              <div className="py-20 text-center bg-white rounded-xl border border-slate-200">
                <Spinner size="lg" label="Loading project release reports..." />
              </div>
            )}

            {/* Error State */}
            {!loading && error && (
              <div className="space-y-4">
                <Alert type="error" title="Reports Unavailable" message={error} />
                <Button size="sm" onClick={loadReports} leftIcon={<RefreshCw className="w-3.5 h-3.5" />}>
                  Retry
                </Button>
              </div>
            )}

            {/* Reports List */}
            {!loading && !error && (
              <Card>
                <CardHeader className="flex flex-row items-center justify-between pb-3 border-b border-slate-100">
                  <CardTitle className="text-sm">Release Reports Directory</CardTitle>
                  <span className="text-xs font-semibold text-slate-500">
                    {reports.length} {reports.length === 1 ? 'Report' : 'Reports'}
                  </span>
                </CardHeader>

                <CardContent className="p-0">
                  {reports.length === 0 ? (
                    <div className="py-12 text-center space-y-3">
                      <FileText className="w-8 h-8 text-slate-400 mx-auto" />
                      <p className="text-xs font-semibold text-slate-600">No release reports generated yet for this project.</p>
                      <Link href={`/projects/${projectId}/releases/new`}>
                        <Button size="sm" leftIcon={<GitBranch className="w-3.5 h-3.5" />}>
                          Create First Release
                        </Button>
                      </Link>
                    </div>
                  ) : (
                    <div className="overflow-x-auto">
                      <table className="w-full text-left text-xs">
                        <thead className="bg-slate-50 border-b border-slate-100 text-slate-500 font-semibold uppercase tracking-wider">
                          <tr>
                            <th className="px-5 py-3">Report ID & Release</th>
                            <th className="px-4 py-3">Version</th>
                            <th className="px-4 py-3">Readiness Score</th>
                            <th className="px-4 py-3">Status</th>
                            <th className="px-4 py-3">Created Date</th>
                            <th className="px-5 py-3 text-right">Actions</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-100 text-slate-700">
                          {reports.map((report) => (
                            <tr key={report.id || report.releaseId} className="hover:bg-slate-50/50">
                              <td className="px-5 py-3.5">
                                <div className="flex items-center gap-2.5">
                                  <div className="p-2 rounded-lg bg-indigo-50 text-indigo-600">
                                    <FileText className="w-4 h-4" />
                                  </div>
                                  <div>
                                    <span className="font-mono font-bold text-slate-900 block">
                                      {report.reportNumber || `REP-${report.releaseId.substring(0, 8)}`}
                                    </span>
                                    <span className="text-[11px] text-slate-500">
                                      {report.releaseName || 'Release Candidate'}
                                    </span>
                                  </div>
                                </div>
                              </td>
                              <td className="px-4 py-3.5">
                                <span className="font-mono font-bold text-slate-800 bg-slate-100 px-2 py-0.5 rounded">
                                  v{report.version}
                                </span>
                              </td>
                              <td className="px-4 py-3.5 font-bold text-slate-900">
                                {report.readinessScore != null
                                  ? `${Number(report.readinessScore).toFixed(1)} / 100`
                                  : 'N/A'}
                              </td>
                              <td className="px-4 py-3.5">
                                <Badge
                                  variant={report.status === 'COMPLETED' ? 'ready' : report.status === 'ANALYZING' ? 'review' : 'neutral'}
                                  dot
                                  size="sm"
                                >
                                  {report.status.replace(/_/g, ' ')}
                                </Badge>
                              </td>
                              <td className="px-4 py-3.5 text-slate-500">
                                {formatDate(report.createdAt)}
                              </td>
                              <td className="px-5 py-3.5 text-right space-x-2">
                                <Link href={`/projects/${projectId}/reports/${report.releaseId}`}>
                                  <Button size="sm" variant="outline" leftIcon={<Eye className="w-3.5 h-3.5" />}>
                                    View Report
                                  </Button>
                                </Link>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </CardContent>
              </Card>
            )}
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
