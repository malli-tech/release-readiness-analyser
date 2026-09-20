'use client';

import React, { useState, useEffect, useMemo, useCallback } from 'react';
import Link from 'next/link';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import { useAuth } from '@/hooks/useAuth';
import { useReleases } from '@/hooks/useReleases';
import { Release } from '@/types/release';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import Alert from '@/components/ui/Alert';
import { formatDate } from '@/lib/utils';
import {
  GitBranch,
  Calendar,
  Search,
  Filter,
  RefreshCw,
  FolderGit2,
  Upload,
  BarChart3,
  Layers,
  CheckCircle2,
  Clock,
} from 'lucide-react';

export default function GlobalReleasesPage() {
  const { isAuthenticated } = useAuth();
  const { getAllUserReleases, error } = useReleases();
  const [releases, setReleases] = useState<Release[]>([]);
  const [fetching, setFetching] = useState<boolean>(true);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const loadReleases = useCallback(async () => {
    if (!isAuthenticated) return;
    setFetching(true);
    setFetchError(null);
    try {
      const data = await getAllUserReleases();
      setReleases(data);
    } catch (err: unknown) {
      setFetchError(error || 'Failed to load releases. Please try again.');
    } finally {
      setFetching(false);
    }
  }, [isAuthenticated, getAllUserReleases, error]);

  useEffect(() => {
    loadReleases();
  }, [loadReleases]);

  // Compute metrics
  const totalCount = releases.length;
  const analyzedCount = useMemo(() => {
    return releases.filter(
      (r) => r.status === 'ANALYZED' || r.status === 'READY' || r.status === 'NEEDS_REVIEW' || r.status === 'NOT_READY'
    ).length;
  }, [releases]);
  const pendingCount = useMemo(() => {
    return releases.filter(
      (r) => r.status === 'NOT_ANALYZED' || r.status === 'ANALYZING' || r.status === 'QUEUED' || !r.status
    ).length;
  }, [releases]);

  // Filtered releases
  const filteredReleases = useMemo(() => {
    return releases.filter((release) => {
      const matchesSearch =
        searchQuery.trim() === '' ||
        release.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        release.version.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (release.projectName && release.projectName.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (release.description && release.description.toLowerCase().includes(searchQuery.toLowerCase()));

      const matchesStatus =
        statusFilter === 'ALL' ||
        release.status === statusFilter ||
        (statusFilter === 'ANALYZED' && ['ANALYZED', 'READY', 'NEEDS_REVIEW', 'NOT_READY'].includes(release.status)) ||
        (statusFilter === 'NOT_ANALYZED' && (!release.status || release.status === 'NOT_ANALYZED'));

      return matchesSearch && matchesStatus;
    });
  }, [releases, searchQuery, statusFilter]);

  const getBadgeVariant = (status?: string): 'ready' | 'review' | 'critical' | 'warning' | 'info' | 'neutral' => {
    switch (status) {
      case 'READY':
        return 'ready';
      case 'ANALYZED':
        return 'info';
      case 'NEEDS_REVIEW':
        return 'review';
      case 'NOT_READY':
        return 'critical';
      case 'ANALYZING':
      case 'QUEUED':
        return 'warning';
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
            {/* Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2.5">
                  <GitBranch className="w-6 h-6 text-indigo-600" />
                  All Project Releases
                </h1>
                <p className="text-xs sm:text-sm text-slate-500 mt-1">
                  Track, inspect, and evaluate release candidates across all your projects.
                </p>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={loadReleases}
                disabled={fetching}
                leftIcon={<RefreshCw className={`w-3.5 h-3.5 ${fetching ? 'animate-spin' : ''}`} />}
              >
                Refresh
              </Button>
            </div>

            {/* Error Alert */}
            {fetchError && (
              <Alert
                type="error"
                title="Error Loading Releases"
                message={fetchError}
                onDismiss={() => setFetchError(null)}
              />
            )}

            {/* Summary Metrics */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <Card className="p-4 flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center shrink-0">
                  <Layers className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-xs font-medium text-slate-500">Total Releases</p>
                  <p className="text-xl font-bold text-slate-900">{fetching ? '—' : totalCount}</p>
                </div>
              </Card>

              <Card className="p-4 flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
                  <CheckCircle2 className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-xs font-medium text-slate-500">Evaluated / Analyzed</p>
                  <p className="text-xl font-bold text-slate-900">{fetching ? '—' : analyzedCount}</p>
                </div>
              </Card>

              <Card className="p-4 flex items-center gap-4">
                <div className="w-10 h-10 rounded-xl bg-amber-50 text-amber-600 flex items-center justify-center shrink-0">
                  <Clock className="w-5 h-5" />
                </div>
                <div>
                  <p className="text-xs font-medium text-slate-500">Pending Scans</p>
                  <p className="text-xl font-bold text-slate-900">{fetching ? '—' : pendingCount}</p>
                </div>
              </Card>
            </div>

            {/* Search and Filters */}
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-white p-3.5 rounded-xl border border-slate-200 shadow-sm">
              <div className="relative flex-1">
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  placeholder="Search by version, release name, or project..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-9 pr-3 py-1.5 text-xs rounded-lg border border-slate-200 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500"
                />
              </div>

              <div className="flex items-center gap-2">
                <Filter className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                <select
                  value={statusFilter}
                  onChange={(e) => setStatusFilter(e.target.value)}
                  className="py-1.5 px-3 text-xs rounded-lg border border-slate-200 bg-white text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500"
                >
                  <option value="ALL">All Statuses</option>
                  <option value="ANALYZED">Analyzed / Evaluated</option>
                  <option value="NOT_ANALYZED">Not Analyzed</option>
                  <option value="ANALYZING">Analyzing</option>
                  <option value="READY">Ready</option>
                  <option value="NEEDS_REVIEW">Needs Review</option>
                  <option value="NOT_READY">Not Ready</option>
                </select>
              </div>
            </div>

            {/* Content Area: Loading / Empty / List */}
            {fetching ? (
              <div className="py-16 text-center">
                <Spinner size="lg" label="Loading project releases..." />
              </div>
            ) : filteredReleases.length === 0 ? (
              <Card>
                <CardContent className="py-12 text-center space-y-3">
                  <div className="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto">
                    <GitBranch className="w-6 h-6" />
                  </div>
                  <div className="space-y-1">
                    <h3 className="text-sm font-bold text-slate-900">
                      {releases.length === 0 ? 'No releases found' : 'No matching releases'}
                    </h3>
                    <p className="text-xs text-slate-500 max-w-sm mx-auto">
                      {releases.length === 0
                        ? 'Select a project to create your first release candidate.'
                        : 'No releases matched your current search or filter criteria.'}
                    </p>
                  </div>
                  {releases.length === 0 ? (
                    <Link href="/projects">
                      <Button size="sm" leftIcon={<FolderGit2 className="w-3.5 h-3.5" />}>
                        Go to My Projects
                      </Button>
                    </Link>
                  ) : (
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => {
                        setSearchQuery('');
                        setStatusFilter('ALL');
                      }}
                    >
                      Clear Filters
                    </Button>
                  )}
                </CardContent>
              </Card>
            ) : (
              <Card>
                <CardHeader className="flex flex-row items-center justify-between pb-3">
                  <div className="flex items-center gap-2">
                    <GitBranch className="w-4 h-4 text-indigo-600" />
                    <CardTitle className="text-sm">Releases ({filteredReleases.length})</CardTitle>
                  </div>
                </CardHeader>

                <CardContent className="p-0">
                  <div className="overflow-x-auto">
                    <table className="w-full text-left text-xs">
                      <thead className="bg-slate-50 border-b border-slate-100 text-slate-500 font-semibold uppercase tracking-wider">
                        <tr>
                          <th className="px-5 py-3">Version & Name</th>
                          <th className="px-4 py-3">Project</th>
                          <th className="px-4 py-3">Status</th>
                          <th className="px-4 py-3">Created Date</th>
                          <th className="px-5 py-3 text-right">Actions</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100 text-slate-700">
                        {filteredReleases.map((release) => (
                          <tr key={release.id} className="hover:bg-slate-50/50 transition-colors">
                            <td className="px-5 py-3.5">
                              <div className="flex items-center gap-2.5">
                                <span className="font-mono font-bold text-xs text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">
                                  {release.version}
                                </span>
                                <div>
                                  <Link
                                    href={`/releases/${release.id}`}
                                    className="font-semibold text-slate-900 hover:text-indigo-600 transition-colors"
                                  >
                                    {release.name}
                                  </Link>
                                  {release.description && (
                                    <p className="text-[11px] text-slate-400 max-w-xs truncate">
                                      {release.description}
                                    </p>
                                  )}
                                </div>
                              </div>
                            </td>

                            <td className="px-4 py-3.5">
                              {release.projectId ? (
                                <Link
                                  href={`/projects/${release.projectId}`}
                                  className="inline-flex items-center gap-1 font-medium text-slate-700 hover:text-indigo-600 transition-colors"
                                >
                                  <FolderGit2 className="w-3.5 h-3.5 text-slate-400" />
                                  <span>{release.projectName || 'Project'}</span>
                                </Link>
                              ) : (
                                <span className="text-slate-400">—</span>
                              )}
                            </td>

                            <td className="px-4 py-3.5">
                              <Badge variant={getBadgeVariant(release.status)} dot size="sm">
                                {release.status ? release.status.replace(/_/g, ' ') : 'NOT ANALYZED'}
                              </Badge>
                            </td>

                            <td className="px-4 py-3.5 text-slate-500">
                              <span className="flex items-center gap-1">
                                <Calendar className="w-3 h-3 text-slate-400" />
                                {formatDate(release.createdAt)}
                              </span>
                            </td>

                            <td className="px-5 py-3.5 text-right">
                              <div className="flex items-center justify-end gap-1.5">
                                <Link href={`/releases/${release.id}`}>
                                  <Button size="sm" variant="ghost">
                                    Details
                                  </Button>
                                </Link>
                                <Link href={`/releases/${release.id}/upload`}>
                                  <Button size="sm" variant="ghost" leftIcon={<Upload className="w-3 h-3" />}>
                                    Upload
                                  </Button>
                                </Link>
                                <Link href={`/releases/${release.id}/analysis`}>
                                  <Button size="sm" variant="outline" leftIcon={<BarChart3 className="w-3 h-3" />}>
                                    Analysis
                                  </Button>
                                </Link>
                              </div>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
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
