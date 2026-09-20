'use client';

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useParams, useSearchParams, useRouter } from 'next/navigation';
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
import { fetchVersionComparison, ApiError } from '@/lib/api';
import { useReleases } from '@/hooks/useReleases';
import { VersionComparisonResponse, FindingChangeItem } from '@/types/comparison';
import {
  ArrowLeft,
  ArrowRight,
  GitCompare,
  AlertCircle,
  AlertTriangle,
  CheckCircle2,
  XCircle,
  Info,
  ShieldAlert,
  FileText,
  Sparkles,
  Layers,
  ArrowUpRight,
  ArrowDownRight,
  Minus,
  SlidersHorizontal,
} from 'lucide-react';

export default function VersionComparisonPage() {
  const params = useParams();
  const searchParams = useSearchParams();
  const router = useRouter();

  const projectId = params?.projectId as string;
  const initialBaseId = searchParams?.get('baseReleaseId') || '';
  const initialTargetId = searchParams?.get('targetReleaseId') || '';

  const { releases, loading: releasesLoading } = useReleases(projectId);

  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [baseReleaseId, setBaseReleaseId] = useState<string>(initialBaseId);
  const [targetReleaseId, setTargetReleaseId] = useState<string>(initialTargetId);

  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [comparison, setComparison] = useState<VersionComparisonResponse | null>(null);

  const [findingFilter, setFindingFilter] = useState<'ALL' | 'NEW' | 'RESOLVED' | 'UNCHANGED'>('ALL');

  // Auto-select initial releases if not set
  useEffect(() => {
    if (releases && releases.length > 0) {
      if (!baseReleaseId && !targetReleaseId) {
        if (releases.length >= 2) {
          setBaseReleaseId(releases[1].id);
          setTargetReleaseId(releases[0].id);
        } else if (releases.length === 1) {
          setBaseReleaseId(releases[0].id);
          setTargetReleaseId(releases[0].id);
        }
      }
    }
  }, [releases, baseReleaseId, targetReleaseId]);

  const loadComparison = useCallback(async (baseId: string, targetId: string) => {
    if (!projectId || !baseId || !targetId) return;

    if (baseId === targetId) {
      setComparison(null);
      setError('Please select two different releases to perform a version comparison.');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const data = await fetchVersionComparison(projectId, baseId, targetId);
      setComparison(data);
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        setError(err.message || 'Failed to compare release versions.');
      } else {
        setError('Network error while retrieving version comparison. Please try again.');
      }
      setComparison(null);
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    if (baseReleaseId && targetReleaseId && baseReleaseId !== targetReleaseId) {
      loadComparison(baseReleaseId, targetReleaseId);
    }
  }, [baseReleaseId, targetReleaseId, loadComparison]);

  const handleCompareClick = () => {
    if (baseReleaseId && targetReleaseId) {
      router.push(`/projects/${projectId}/releases/compare?baseReleaseId=${baseReleaseId}&targetReleaseId=${targetReleaseId}`);
      loadComparison(baseReleaseId, targetReleaseId);
    }
  };

  const handleSwap = () => {
    const temp = baseReleaseId;
    setBaseReleaseId(targetReleaseId);
    setTargetReleaseId(temp);
  };

  const filteredFindingItems = useMemo(() => {
    if (!comparison?.findingChanges?.items) return [];
    if (findingFilter === 'ALL') return comparison.findingChanges.items;
    return comparison.findingChanges.items.filter(item => item.changeType === findingFilter);
  }, [comparison, findingFilter]);

  const renderDeltaBadge = (delta: number, inverseGood = false) => {
    if (delta === 0) {
      return (
        <span className="inline-flex items-center gap-0.5 text-xs font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">
          <Minus className="w-3 h-3" /> 0
        </span>
      );
    }

    const isPositive = delta > 0;
    const isGood = inverseGood ? !isPositive : isPositive;

    const bgClass = isGood ? 'bg-emerald-50 text-emerald-700 border-emerald-200' : 'bg-rose-50 text-rose-700 border-rose-200';
    const Icon = isPositive ? ArrowUpRight : ArrowDownRight;

    return (
      <span className={`inline-flex items-center gap-0.5 text-xs font-bold border px-2 py-0.5 rounded-full ${bgClass}`}>
        <Icon className="w-3.5 h-3.5" />
        {isPositive ? `+${delta}` : delta}
      </span>
    );
  };

  const renderFindingChangeBadge = (type: string) => {
    switch (type) {
      case 'NEW':
        return <Badge variant="critical">NEW</Badge>;
      case 'RESOLVED':
        return <Badge variant="ready">RESOLVED</Badge>;
      case 'UNCHANGED':
        return <Badge variant="neutral">UNCHANGED</Badge>;
      default:
        return <Badge variant="neutral">{type}</Badge>;
    }
  };

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            {/* Page Header */}
            <div>
              <div className="flex items-center gap-2 mb-1">
                <Link
                  href={`/projects/${projectId}/releases`}
                  className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1"
                >
                  <ArrowLeft className="w-3 h-3" /> Back to Releases
                </Link>
              </div>
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                <div>
                  <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight flex items-center gap-2">
                    <GitCompare className="w-6 h-6 text-indigo-600" /> Version Comparison
                  </h1>
                  <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
                    Compare risk, readiness score, findings delta, and code quality changes between release candidates.
                  </p>
                </div>
              </div>
            </div>

            {/* Selection Controls */}
            <Card>
              <CardHeader className="pb-3 border-b border-slate-100">
                <CardTitle className="text-sm font-bold flex items-center gap-2">
                  <SlidersHorizontal className="w-4 h-4 text-slate-600" /> Select Release Versions to Compare
                </CardTitle>
              </CardHeader>
              <CardContent className="pt-4">
                <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-end">
                  <div className="md:col-span-5">
                    <label className="block text-xs font-bold text-slate-700 mb-1">
                      Base Release (Baseline / Older)
                    </label>
                    <select
                      value={baseReleaseId}
                      onChange={(e) => setBaseReleaseId(e.target.value)}
                      disabled={releasesLoading}
                      className="w-full text-xs font-semibold px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    >
                      <option value="">Select Base Release...</option>
                      {releases.map((rel) => (
                        <option key={rel.id} value={rel.id}>
                          v{rel.version} - {rel.name} ({rel.status})
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="md:col-span-2 flex justify-center">
                    <Button
                      type="button"
                      variant="outline"
                      size="sm"
                      onClick={handleSwap}
                      disabled={!baseReleaseId || !targetReleaseId}
                      className="w-full text-xs"
                      title="Swap Base and Target"
                    >
                      <GitCompare className="w-3.5 h-3.5 mr-1" /> Swap
                    </Button>
                  </div>

                  <div className="md:col-span-5">
                    <label className="block text-xs font-bold text-slate-700 mb-1">
                      Target Release (Comparison / Newer)
                    </label>
                    <select
                      value={targetReleaseId}
                      onChange={(e) => setTargetReleaseId(e.target.value)}
                      disabled={releasesLoading}
                      className="w-full text-xs font-semibold px-3 py-2 rounded-lg border border-slate-300 bg-white text-slate-900 focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                    >
                      <option value="">Select Target Release...</option>
                      {releases.map((rel) => (
                        <option key={rel.id} value={rel.id}>
                          v{rel.version} - {rel.name} ({rel.status})
                        </option>
                      ))}
                    </select>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* Loading / Error States */}
            {loading ? (
              <div className="py-20 flex flex-col items-center justify-center space-y-3 bg-white rounded-xl border border-slate-200">
                <Spinner size="lg" label="Computing release comparison..." />
                <p className="text-xs text-slate-500">Evaluating persisted release artifacts and findings deltas...</p>
              </div>
            ) : error ? (
              <Alert type="error" title="Comparison Notice" message={error} />
            ) : comparison ? (
              <div className="space-y-6">
                {/* Comparison Header Banner */}
                <div className="bg-white border border-slate-200 rounded-xl p-4 sm:p-6 shadow-sm flex flex-col sm:flex-row items-center justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <div className="p-3 bg-indigo-50 rounded-xl border border-indigo-100">
                      <Layers className="w-6 h-6 text-indigo-600" />
                    </div>
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Comparing</span>
                        <span className="text-sm font-black text-slate-900">
                          v{comparison.baseRelease.version} ({comparison.baseRelease.name})
                        </span>
                        <ArrowRight className="w-4 h-4 text-indigo-500" />
                        <span className="text-sm font-black text-slate-900">
                          v{comparison.targetRelease.version} ({comparison.targetRelease.name})
                        </span>
                      </div>
                      <p className="text-xs text-slate-500 mt-1">
                        Project: {comparison.baseRelease.projectName || projectId}
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <Link href={`/releases/${comparison.baseRelease.releaseId}/results`}>
                      <Button variant="outline" size="sm">
                        View Base Results
                      </Button>
                    </Link>
                    <Link href={`/releases/${comparison.targetRelease.releaseId}/results`}>
                      <Button variant="outline" size="sm">
                        View Target Results
                      </Button>
                    </Link>
                  </div>
                </div>

                {/* Top Metrics Cards */}
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                  {/* Readiness Score Delta */}
                  <Card className="hover:shadow-md transition-shadow">
                    <CardContent className="pt-5 pb-4">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Readiness Score</span>
                        {renderDeltaBadge(comparison.readinessComparison?.scoreDelta ?? 0, false)}
                      </div>
                      <div className="mt-3 flex items-baseline justify-between">
                        <div>
                          <span className="text-2xl font-black text-slate-900">
                            {comparison.readinessComparison?.targetScore ?? 'N/A'}
                          </span>
                          <span className="text-xs font-semibold text-slate-400 ml-1">
                            (Base: {comparison.readinessComparison?.baseScore ?? 'N/A'})
                          </span>
                        </div>
                      </div>
                      <div className="mt-2 text-xs font-semibold text-slate-600 flex items-center gap-1">
                        Level: {comparison.readinessComparison?.baseLevel || 'UNKNOWN'} <ArrowRight className="w-3 h-3 text-slate-400" /> {comparison.readinessComparison?.targetLevel || 'UNKNOWN'}
                      </div>
                    </CardContent>
                  </Card>

                  {/* Weighted Risk Points Delta */}
                  <Card className="hover:shadow-md transition-shadow">
                    <CardContent className="pt-5 pb-4">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-slate-500 uppercase tracking-wider font-mono">Risk Points</span>
                        {renderDeltaBadge(comparison.riskComparison?.deltaWeightedRiskPoints ?? 0, true)}
                      </div>
                      <div className="mt-3 flex items-baseline justify-between">
                        <div>
                          <span className="text-2xl font-black text-slate-900">
                            {comparison.riskComparison?.targetWeightedRiskPoints ?? '0.00'}
                          </span>
                          <span className="text-xs font-semibold text-slate-400 ml-1">
                            (Base: {comparison.riskComparison?.baseWeightedRiskPoints ?? '0.00'})
                          </span>
                        </div>
                      </div>
                      <div className="mt-2 text-xs font-semibold text-slate-600 flex items-center gap-1">
                        Risk Level: {comparison.riskComparison?.baseRiskLevel || 'UNKNOWN'} <ArrowRight className="w-3 h-3 text-slate-400" /> {comparison.riskComparison?.targetRiskLevel || 'UNKNOWN'}
                      </div>
                    </CardContent>
                  </Card>

                  {/* Total Findings Delta */}
                  <Card className="hover:shadow-md transition-shadow">
                    <CardContent className="pt-5 pb-4">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Total Findings</span>
                        {renderDeltaBadge(comparison.findingsComparison.delta, true)}
                      </div>
                      <div className="mt-3 flex items-baseline justify-between">
                        <div>
                          <span className="text-2xl font-black text-slate-900">
                            {comparison.findingsComparison.targetTotal}
                          </span>
                          <span className="text-xs font-semibold text-slate-400 ml-1">
                            (Base: {comparison.findingsComparison.baseTotal})
                          </span>
                        </div>
                      </div>
                      <div className="mt-2 text-xs font-semibold text-slate-600">
                        {comparison.findingChanges.newCount} New / {comparison.findingChanges.resolvedCount} Resolved
                      </div>
                    </CardContent>
                  </Card>

                  {/* Affected Files Delta */}
                  <Card className="hover:shadow-md transition-shadow">
                    <CardContent className="pt-5 pb-4">
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">Affected Files</span>
                        {renderDeltaBadge(comparison.affectedFilesComparison.delta, true)}
                      </div>
                      <div className="mt-3 flex items-baseline justify-between">
                        <div>
                          <span className="text-2xl font-black text-slate-900">
                            {comparison.affectedFilesComparison.targetAffectedFiles}
                          </span>
                          <span className="text-xs font-semibold text-slate-400 ml-1">
                            (Base: {comparison.affectedFilesComparison.baseAffectedFiles})
                          </span>
                        </div>
                      </div>
                      <div className="mt-2 text-xs font-semibold text-slate-600">
                        Files with detected issues
                      </div>
                    </CardContent>
                  </Card>
                </div>

                {/* Severity & Category Deltas */}
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  {/* Severity Comparison Table */}
                  <Card>
                    <CardHeader className="pb-3 border-b border-slate-100">
                      <CardTitle className="text-sm font-bold flex items-center gap-2">
                        <ShieldAlert className="w-4 h-4 text-amber-500" /> Severity Breakdown Delta
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="p-0">
                      <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs">
                          <thead className="bg-slate-50 text-slate-500 uppercase font-bold border-b border-slate-100">
                            <tr>
                              <th className="py-2.5 px-4">Severity</th>
                              <th className="py-2.5 px-4 text-center">Base (v{comparison.baseRelease.version})</th>
                              <th className="py-2.5 px-4 text-center">Target (v{comparison.targetRelease.version})</th>
                              <th className="py-2.5 px-4 text-right">Delta</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-100">
                            {['HIGH', 'MEDIUM', 'LOW', 'INFO'].map((sev) => {
                              const baseCount = comparison.severityComparison.baseCounts[sev] || 0;
                              const targetCount = comparison.severityComparison.targetCounts[sev] || 0;
                              const delta = comparison.severityComparison.deltas[sev] || 0;

                              return (
                                <tr key={sev} className="hover:bg-slate-50/50">
                                  <td className="py-3 px-4 font-bold text-slate-800">{sev}</td>
                                  <td className="py-3 px-4 text-center text-slate-600 font-medium">{baseCount}</td>
                                  <td className="py-3 px-4 text-center text-slate-900 font-bold">{targetCount}</td>
                                  <td className="py-3 px-4 text-right">{renderDeltaBadge(delta, true)}</td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Category Comparison Table */}
                  <Card>
                    <CardHeader className="pb-3 border-b border-slate-100">
                      <CardTitle className="text-sm font-bold flex items-center gap-2">
                        <FileText className="w-4 h-4 text-indigo-500" /> Category Breakdown Delta
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="p-0">
                      <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs">
                          <thead className="bg-slate-50 text-slate-500 uppercase font-bold border-b border-slate-100">
                            <tr>
                              <th className="py-2.5 px-4">Category</th>
                              <th className="py-2.5 px-4 text-center">Base (v{comparison.baseRelease.version})</th>
                              <th className="py-2.5 px-4 text-center">Target (v{comparison.targetRelease.version})</th>
                              <th className="py-2.5 px-4 text-right">Delta</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-100">
                            {['SECURITY', 'CODE_QUALITY', 'DEPENDENCY', 'TESTING', 'PERFORMANCE'].map((cat) => {
                              const baseCount = comparison.categoryComparison.baseCounts[cat] || 0;
                              const targetCount = comparison.categoryComparison.targetCounts[cat] || 0;
                              const delta = comparison.categoryComparison.deltas[cat] || 0;

                              return (
                                <tr key={cat} className="hover:bg-slate-50/50">
                                  <td className="py-3 px-4 font-bold text-slate-800">{cat}</td>
                                  <td className="py-3 px-4 text-center text-slate-600 font-medium">{baseCount}</td>
                                  <td className="py-3 px-4 text-center text-slate-900 font-bold">{targetCount}</td>
                                  <td className="py-3 px-4 text-right">{renderDeltaBadge(delta, true)}</td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      </div>
                    </CardContent>
                  </Card>
                </div>

                {/* Finding Changes Section */}
                <Card>
                  <CardHeader className="pb-3 border-b border-slate-100 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                    <div>
                      <CardTitle className="text-sm font-bold flex items-center gap-2">
                        <AlertTriangle className="w-4 h-4 text-rose-500" /> Traceable Finding Changes
                      </CardTitle>
                      <p className="text-xs text-slate-500 mt-0.5">
                        Categorized findings based on rule, file path, line number, and category identity across releases.
                      </p>
                    </div>

                    {/* Filter Tabs */}
                    <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-lg">
                      {(['ALL', 'NEW', 'RESOLVED', 'UNCHANGED'] as const).map((filter) => (
                        <button
                          key={filter}
                          type="button"
                          onClick={() => setFindingFilter(filter)}
                          className={`text-xs font-bold px-2.5 py-1 rounded-md transition-colors ${
                            findingFilter === filter
                              ? 'bg-white text-slate-900 shadow-sm'
                              : 'text-slate-600 hover:text-slate-900'
                          }`}
                        >
                          {filter}
                          {filter === 'NEW' && ` (${comparison.findingChanges.newCount})`}
                          {filter === 'RESOLVED' && ` (${comparison.findingChanges.resolvedCount})`}
                          {filter === 'UNCHANGED' && ` (${comparison.findingChanges.unchangedCount})`}
                        </button>
                      ))}
                    </div>
                  </CardHeader>

                  <CardContent className="p-0">
                    {comparison.findingChanges.limitationNote && (
                      <div className="p-3 bg-amber-50 border-b border-amber-100 text-xs text-amber-800 flex items-center gap-2">
                        <Info className="w-4 h-4 text-amber-600 flex-shrink-0" />
                        <span>{comparison.findingChanges.limitationNote}</span>
                      </div>
                    )}

                    {filteredFindingItems.length === 0 ? (
                      <div className="p-8 text-center text-xs text-slate-500">
                        No finding changes matching filter <span className="font-bold">{findingFilter}</span>.
                      </div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full text-left text-xs">
                          <thead className="bg-slate-50 text-slate-500 uppercase font-bold border-b border-slate-100">
                            <tr>
                              <th className="py-2.5 px-4">Status</th>
                              <th className="py-2.5 px-4">Rule & Category</th>
                              <th className="py-2.5 px-4">Severity</th>
                              <th className="py-2.5 px-4">Title</th>
                              <th className="py-2.5 px-4">File Location</th>
                            </tr>
                          </thead>
                          <tbody className="divide-y divide-slate-100">
                            {filteredFindingItems.map((item, idx) => (
                              <tr key={item.findingId || `${item.ruleId}-${idx}`} className="hover:bg-slate-50/50">
                                <td className="py-3 px-4">{renderFindingChangeBadge(item.changeType)}</td>
                                <td className="py-3 px-4">
                                  <div className="font-bold text-slate-900">{item.ruleId || 'N/A'}</div>
                                  <div className="text-[10px] text-slate-500 font-semibold">{item.category}</div>
                                </td>
                                <td className="py-3 px-4 font-bold text-slate-700">{item.severity}</td>
                                <td className="py-3 px-4 font-medium text-slate-800 max-w-xs truncate">{item.title}</td>
                                <td className="py-3 px-4 text-slate-600 font-mono text-[11px]">
                                  {item.filePath ? (
                                    <span>
                                      {item.filePath}
                                      {item.lineNumber !== null && item.lineNumber !== undefined ? `:${item.lineNumber}` : ''}
                                    </span>
                                  ) : (
                                    <span className="text-slate-400 italic">Project-level</span>
                                  )}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </CardContent>
                </Card>

                {/* Additional Details: AI Review & Coverage Comparison */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* AI Review & Recommendation Comparison */}
                  <Card>
                    <CardHeader className="pb-3 border-b border-slate-100">
                      <CardTitle className="text-sm font-bold flex items-center gap-2">
                        <Sparkles className="w-4 h-4 text-indigo-500" /> AI Review & Recommendation Comparison
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="pt-4 space-y-4 text-xs">
                      <div className="grid grid-cols-2 gap-4 bg-slate-50 p-3 rounded-lg border border-slate-100">
                        <div>
                          <div className="text-[11px] font-bold text-slate-400 uppercase">Base AI Review</div>
                          <div className="mt-1 font-bold text-slate-800">
                            Status: {comparison.aiReviewComparison?.baseOverallStatus || 'NONE'}
                          </div>
                          <div className="text-slate-500">
                            Completed: {comparison.aiReviewComparison?.baseCompletedReviews ?? 0} / {comparison.aiReviewComparison?.baseTotalReviews ?? 0}
                          </div>
                        </div>

                        <div>
                          <div className="text-[11px] font-bold text-slate-400 uppercase">Target AI Review</div>
                          <div className="mt-1 font-bold text-slate-800">
                            Status: {comparison.aiReviewComparison?.targetOverallStatus || 'NONE'}
                          </div>
                          <div className="text-slate-500">
                            Completed: {comparison.aiReviewComparison?.targetCompletedReviews ?? 0} / {comparison.aiReviewComparison?.targetTotalReviews ?? 0}
                          </div>
                        </div>
                      </div>

                      <div className="grid grid-cols-2 gap-4 bg-slate-50 p-3 rounded-lg border border-slate-100">
                        <div>
                          <div className="text-[11px] font-bold text-slate-400 uppercase">Base Recommendations</div>
                          <div className="mt-1 font-bold text-slate-800">
                            Total: {comparison.recommendationComparison?.baseTotal ?? 0}
                          </div>
                        </div>

                        <div>
                          <div className="text-[11px] font-bold text-slate-400 uppercase">Target Recommendations</div>
                          <div className="mt-1 font-bold text-slate-800">
                            Total: {comparison.recommendationComparison?.targetTotal ?? 0}
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>

                  {/* Coverage Comparison */}
                  <Card>
                    <CardHeader className="pb-3 border-b border-slate-100">
                      <CardTitle className="text-sm font-bold flex items-center gap-2">
                        <CheckCircle2 className="w-4 h-4 text-emerald-500" /> Analyzer Coverage Comparison
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="pt-4 space-y-4 text-xs">
                      <div className="grid grid-cols-2 gap-4 bg-slate-50 p-3 rounded-lg border border-slate-100">
                        <div>
                          <div className="text-[11px] font-bold text-slate-400 uppercase">Base Completeness</div>
                          <div className="mt-1 font-bold text-slate-800">
                            {comparison.coverageComparison?.baseCompleteness || 'UNKNOWN'}
                          </div>
                          <div className="text-slate-500 mt-1">
                            Passed: {comparison.coverageComparison?.baseCompletedAnalyzers?.length ?? 0} analyzers
                          </div>
                        </div>

                        <div>
                          <div className="text-[11px] font-bold text-slate-400 uppercase">Target Completeness</div>
                          <div className="mt-1 font-bold text-slate-800">
                            {comparison.coverageComparison?.targetCompleteness || 'UNKNOWN'}
                          </div>
                          <div className="text-slate-500 mt-1">
                            Passed: {comparison.coverageComparison?.targetCompletedAnalyzers?.length ?? 0} analyzers
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </div>
            ) : null}
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
