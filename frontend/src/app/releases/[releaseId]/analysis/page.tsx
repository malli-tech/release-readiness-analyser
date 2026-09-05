'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useParams } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { useAnalysis } from '@/hooks/useAnalysis';
import { Finding } from '@/types/finding';
import {
  CheckCircle2,
  AlertTriangle,
  ArrowLeft,
  FileCode2,
  ShieldCheck,
  Sparkles,
  Layers,
  Cpu,
  Database,
  FileText,
  AlertCircle,
  Play,
  RefreshCw,
  Search,
  Code2,
  Filter,
} from 'lucide-react';
import Link from 'next/link';

export default function ReleaseAnalysisPage() {
  const params = useParams();
  const releaseId = params?.releaseId as string;
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [severityFilter, setSeverityFilter] = useState<string>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<string>('ALL');

  const { loading, error, analysis, startAnalysis, getLatestAnalysis } = useAnalysis();
  const [initializing, setInitializing] = useState(true);

  const loadAnalysis = useCallback(async () => {
    if (!releaseId) return;
    setInitializing(true);
    try {
      await getLatestAnalysis(releaseId);
    } catch {
      // Ignored if no analysis exists yet
    } finally {
      setInitializing(false);
    }
  }, [releaseId, getLatestAnalysis]);

  useEffect(() => {
    loadAnalysis();
  }, [loadAnalysis]);

  const handleStartAnalysis = async () => {
    if (!releaseId) return;
    try {
      await startAnalysis(releaseId);
    } catch {
      // Error handled in hook state
    }
  };

  const profile = analysis?.projectProfile;
  const plan = analysis?.analysisPlan;
  const structure = profile?.projectStructure;
  const testingSummary = analysis?.testingSummary;
  const dependencySummary = analysis?.dependencySummary;
  const securitySummary = analysis?.securitySummary;
  const performanceSummary = analysis?.performanceSummary;
  const findings: Finding[] = (analysis?.findings as Finding[]) || [];

  const highFindings = findings.filter((f) => f.severity === 'HIGH');
  const mediumFindings = findings.filter((f) => f.severity === 'MEDIUM');
  const lowFindings = findings.filter((f) => f.severity === 'LOW');

  const filteredFindings = findings.filter((f) => {
    const matchesSev = severityFilter === 'ALL' || f.severity === severityFilter;
    const matchesCat =
      categoryFilter === 'ALL' ||
      f.category === categoryFilter ||
      (categoryFilter === 'DEPENDENCIES' && (f.category === 'DEPENDENCY' || f.category === 'DEPENDENCIES')) ||
      (categoryFilter === 'PERFORMANCE' && f.category === 'PERFORMANCE');
    return matchesSev && matchesCat;
  });

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            <div className="max-w-4xl mx-auto space-y-6">
              <div>
                <Link
                  href={`/releases/${releaseId}`}
                  className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1 mb-2"
                >
                  <ArrowLeft className="w-3 h-3" /> Back to Release Details
                </Link>
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                  <div>
                    <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                      Code Quality, Testing, Dependency, Security & Performance Analyzer
                    </h1>
                    <p className="text-xs sm:text-sm text-slate-500 mt-1">
                      Static project inspection, code quality pattern evaluation, testing structure analysis, dependency management, security scanning, and static performance analysis.
                    </p>
                  </div>
                  {analysis && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={handleStartAnalysis}
                      disabled={loading}
                      leftIcon={<RefreshCw className="w-3.5 h-3.5" />}
                    >
                      Re-run Analysis
                    </Button>
                  )}
                </div>
              </div>

              {initializing ? (
                <div className="py-20 flex flex-col items-center justify-center space-y-3 bg-white rounded-2xl border border-slate-200">
                  <Spinner size="lg" label="Loading analysis specifications..." />
                </div>
              ) : !analysis ? (
                /* Initial State: Trigger Analysis */
                <Card>
                  <CardHeader>
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <ShieldCheck className="w-5 h-5 text-indigo-600" />
                        <CardTitle className="text-sm">Ready for Code Quality Analysis</CardTitle>
                      </div>
                      <Badge variant="neutral" dot size="sm">
                        NOT ANALYZED
                      </Badge>
                    </div>
                  </CardHeader>

                  <CardContent className="py-10 text-center space-y-4">
                    <div className="w-16 h-16 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto border border-indigo-200 shadow-xs">
                      <Search className="w-8 h-8" />
                    </div>

                    <div className="space-y-1.5 max-w-md mx-auto">
                      <h3 className="text-base font-bold text-slate-900">Initiate Static Code Quality Evaluation</h3>
                      <p className="text-xs text-slate-500 leading-relaxed">
                        Analyze the uploaded archive in the isolated workspace. Inspection is 100% static and evaluates maintainability, long methods, large classes, empty exception handlers, and code smells without executing uploaded code.
                      </p>
                    </div>

                    {error && (
                      <div className="p-3 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center justify-center gap-2 max-w-md mx-auto">
                        <AlertCircle className="w-4 h-4 shrink-0" />
                        <span>{error}</span>
                      </div>
                    )}

                    <div className="pt-2">
                      <Button
                        size="md"
                        loading={loading}
                        onClick={handleStartAnalysis}
                        leftIcon={<Play className="w-4 h-4 fill-current" />}
                      >
                        {loading ? 'Analyzing Workspace...' : 'Start Code Quality Analysis'}
                      </Button>
                    </div>
                  </CardContent>
                </Card>
              ) : (
                /* Analysis Result State */
                <div className="space-y-6">
                  {/* Status Banner */}
                  <div className="p-4 rounded-2xl bg-white border border-slate-200 shadow-xs flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                    <div className="flex items-center gap-3">
                      <div className="p-2.5 rounded-xl bg-emerald-50 text-emerald-600 border border-emerald-100">
                        <CheckCircle2 className="w-6 h-6" />
                      </div>
                      <div>
                        <div className="flex items-center gap-2">
                          <h2 className="text-sm font-bold text-slate-900">Analysis Completed</h2>
                          <Badge variant="ready" dot size="sm">
                            {analysis.status}
                          </Badge>
                        </div>
                        <p className="text-xs text-slate-500">
                          Run #{analysis.runNumber} • Analyzed source files statically
                        </p>
                      </div>
                    </div>

                    <div className="text-xs text-slate-400 font-mono">
                      ID: {analysis.id.substring(0, 12)}...
                    </div>
                  </div>

                  {/* Code Quality & Testing Overview */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Findings Summary Card */}
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center gap-2">
                          <Code2 className="w-4 h-4 text-indigo-600" />
                          <CardTitle className="text-sm">Static Findings Overview</CardTitle>
                        </div>
                      </CardHeader>
                      <CardContent>
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
                          <div className="p-3 bg-slate-50 rounded-xl border border-slate-200">
                            <span className="text-[11px] text-slate-400 font-medium block">Total Findings</span>
                            <span className="text-lg font-bold text-slate-900">{findings.length}</span>
                          </div>
                          <div className="p-3 bg-rose-50/50 rounded-xl border border-rose-100">
                            <span className="text-[11px] text-rose-600 font-medium block">High Severity</span>
                            <span className="text-lg font-bold text-rose-700">{highFindings.length}</span>
                          </div>
                          <div className="p-3 bg-amber-50/50 rounded-xl border border-amber-100">
                            <span className="text-[11px] text-amber-600 font-medium block">Medium Severity</span>
                            <span className="text-lg font-bold text-amber-700">{mediumFindings.length}</span>
                          </div>
                          <div className="p-3 bg-blue-50/50 rounded-xl border border-blue-100">
                            <span className="text-[11px] text-blue-600 font-medium block">Low Severity</span>
                            <span className="text-lg font-bold text-blue-700">{lowFindings.length}</span>
                          </div>
                        </div>
                      </CardContent>
                    </Card>

                    {/* Static Testing Summary Card */}
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <FileCode2 className="w-4 h-4 text-emerald-600" />
                            <CardTitle className="text-sm">Static Testing Summary</CardTitle>
                          </div>
                          {testingSummary?.testingCompleteness && (
                            <Badge
                              variant={
                                testingSummary.testingCompleteness === 'STRONG'
                                  ? 'ready'
                                  : testingSummary.testingCompleteness === 'MODERATE' || testingSummary.testingCompleteness === 'PARTIAL'
                                  ? 'warning'
                                  : 'neutral'
                              }
                              size="sm"
                            >
                              {testingSummary.testingCompleteness}
                            </Badge>
                          )}
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                          <div className="p-2.5 bg-slate-50 rounded-xl border border-slate-200">
                            <span className="text-[10px] text-slate-400 font-medium block">Test Files</span>
                            <span className="text-base font-bold text-slate-900">{testingSummary?.testFiles ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-slate-50 rounded-xl border border-slate-200">
                            <span className="text-[10px] text-slate-400 font-medium block">Source Files</span>
                            <span className="text-base font-bold text-slate-900">{testingSummary?.sourceFiles ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-emerald-50/50 rounded-xl border border-emerald-100">
                            <span className="text-[10px] text-emerald-700 font-medium block">Presence Ratio</span>
                            <span className="text-base font-bold text-emerald-700">
                              {((testingSummary?.testPresenceRatio ?? 0) * 100).toFixed(0)}%
                            </span>
                          </div>
                          <div className="p-2.5 bg-indigo-50/50 rounded-xl border border-indigo-100">
                            <span className="text-[10px] text-indigo-700 font-medium block">Tests Counted</span>
                            <span className="text-base font-bold text-indigo-700">{testingSummary?.testsDetected ?? 0}</span>
                          </div>
                        </div>

                        <div className="grid grid-cols-3 gap-2 text-[11px] p-2.5 bg-slate-50 rounded-xl border border-slate-200 font-mono">
                          <div>
                            <span className="text-slate-400 block text-[10px]">Assertions</span>
                            <span className="font-bold text-slate-800">{testingSummary?.assertionsDetected ?? 0}</span>
                          </div>
                          <div>
                            <span className="text-slate-400 block text-[10px]">Skipped</span>
                            <span className="font-bold text-amber-700">{testingSummary?.skippedTestsDetected ?? 0}</span>
                          </div>
                          <div>
                            <span className="text-slate-400 block text-[10px]">Empty</span>
                            <span className="font-bold text-rose-700">{testingSummary?.emptyTestsDetected ?? 0}</span>
                          </div>
                        </div>

                        {testingSummary?.detectedFrameworks && testingSummary.detectedFrameworks.length > 0 && (
                          <div className="flex items-center gap-1.5 text-xs">
                            <span className="text-slate-400 text-[11px]">Frameworks:</span>
                            {testingSummary.detectedFrameworks.map((fw, idx) => (
                              <Badge key={idx} variant="info" size="sm">
                                {fw}
                              </Badge>
                            ))}
                          </div>
                        )}

                        <p className="text-[11px] text-slate-400 italic">
                          {testingSummary?.disclaimer || 'Static test presence is not runtime code coverage.'}
                        </p>
                      </CardContent>
                    </Card>

                    {/* Static Dependency Summary Card */}
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <Layers className="w-4 h-4 text-purple-600" />
                            <CardTitle className="text-sm">Dependency Analysis Summary</CardTitle>
                          </div>
                          {dependencySummary?.dependencyCompleteness && (
                            <Badge
                              variant={
                                dependencySummary.dependencyCompleteness === 'COMPLETE'
                                  ? 'ready'
                                  : dependencySummary.dependencyCompleteness === 'PARTIAL'
                                  ? 'warning'
                                  : 'neutral'
                              }
                              size="sm"
                            >
                              {dependencySummary.dependencyCompleteness}
                            </Badge>
                          )}
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                          <div className="p-2.5 bg-slate-50 rounded-xl border border-slate-200">
                            <span className="text-[10px] text-slate-400 font-medium block">Total Deps</span>
                            <span className="text-base font-bold text-slate-900">{dependencySummary?.dependencyCount ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-slate-50 rounded-xl border border-slate-200">
                            <span className="text-[10px] text-slate-400 font-medium block">Direct Deps</span>
                            <span className="text-base font-bold text-slate-900">{dependencySummary?.directDependencyCount ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-purple-50/50 rounded-xl border border-purple-100">
                            <span className="text-[10px] text-purple-700 font-medium block">Dev Deps</span>
                            <span className="text-base font-bold text-purple-700">{dependencySummary?.devDependencyCount ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-amber-50/50 rounded-xl border border-amber-100">
                            <span className="text-[10px] text-amber-700 font-medium block">Unpinned</span>
                            <span className="text-base font-bold text-amber-700">{dependencySummary?.unpinnedDependencyCount ?? 0}</span>
                          </div>
                        </div>

                        <div className="grid grid-cols-3 gap-2 text-[11px] p-2.5 bg-slate-50 rounded-xl border border-slate-200 font-mono">
                          <div>
                            <span className="text-slate-400 block text-[10px]">Broad Range</span>
                            <span className="font-bold text-amber-700">{dependencySummary?.broadVersionDependencyCount ?? 0}</span>
                          </div>
                          <div>
                            <span className="text-slate-400 block text-[10px]">Duplicates</span>
                            <span className="font-bold text-rose-700">{dependencySummary?.duplicateDependencyCount ?? 0}</span>
                          </div>
                          <div>
                            <span className="text-slate-400 block text-[10px]">Manifests</span>
                            <span className="font-bold text-slate-800">{dependencySummary?.manifestFiles?.length ?? 0}</span>
                          </div>
                        </div>

                        {dependencySummary?.detectedPackageManagers && dependencySummary.detectedPackageManagers.length > 0 && (
                          <div className="flex items-center gap-1.5 text-xs">
                            <span className="text-slate-400 text-[11px]">Ecosystems:</span>
                            {dependencySummary.detectedPackageManagers.map((pm, idx) => (
                              <Badge key={idx} variant="info" size="sm">
                                {pm}
                              </Badge>
                            ))}
                          </div>
                        )}

                        <p className="text-[11px] text-slate-400 italic">
                          {dependencySummary?.disclaimer || 'Static dependency analysis does not evaluate runtime vulnerabilities or download remote packages.'}
                        </p>
                      </CardContent>
                    </Card>

                    {/* Static Security Summary Card */}
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <ShieldCheck className="w-4 h-4 text-rose-600" />
                            <CardTitle className="text-sm">Static Security Analysis Summary</CardTitle>
                          </div>
                          {securitySummary?.securityCompleteness && (
                            <Badge
                              variant={
                                securitySummary.securityCompleteness === 'COMPLETE'
                                  ? 'ready'
                                  : securitySummary.securityCompleteness === 'PARTIAL'
                                  ? 'warning'
                                  : 'neutral'
                              }
                              size="sm"
                            >
                              {securitySummary.securityCompleteness}
                            </Badge>
                          )}
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                          <div className="p-2.5 bg-slate-50 rounded-xl border border-slate-200">
                            <span className="text-[10px] text-slate-400 font-medium block">Total Security</span>
                            <span className="text-base font-bold text-slate-900">{securitySummary?.totalSecurityFindings ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-rose-50/50 rounded-xl border border-rose-100">
                            <span className="text-[10px] text-rose-700 font-medium block">Hardcoded Secrets</span>
                            <span className="text-base font-bold text-rose-700">{securitySummary?.hardcodedSecretsDetected ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-amber-50/50 rounded-xl border border-amber-100">
                            <span className="text-[10px] text-amber-700 font-medium block">Dangerous Exec</span>
                            <span className="text-base font-bold text-amber-700">{securitySummary?.dangerousExecutionFindings ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-indigo-50/50 rounded-xl border border-indigo-100">
                            <span className="text-[10px] text-indigo-700 font-medium block">Injection Risks</span>
                            <span className="text-base font-bold text-indigo-700">{securitySummary?.injectionRiskFindings ?? 0}</span>
                          </div>
                        </div>

                        <div className="grid grid-cols-3 gap-2 text-[11px] p-2.5 bg-slate-50 rounded-xl border border-slate-200 font-mono">
                          <div>
                            <span className="text-slate-400 block text-[10px]">Insecure HTTP</span>
                            <span className="font-bold text-slate-800">{securitySummary?.insecureTransportFindings ?? 0}</span>
                          </div>
                          <div>
                            <span className="text-slate-400 block text-[10px]">Weak Crypto</span>
                            <span className="font-bold text-amber-700">{securitySummary?.weakCryptographyFindings ?? 0}</span>
                          </div>
                          <div>
                            <span className="text-slate-400 block text-[10px]">Sensitive Files</span>
                            <span className="font-bold text-rose-700">{securitySummary?.sensitiveFilesDetected ?? 0}</span>
                          </div>
                        </div>

                        <p className="text-[11px] text-slate-400 italic">
                          {securitySummary?.disclaimer || 'Static heuristic security analysis. Does not prove exploitability or replace penetration testing.'}
                        </p>
                      </CardContent>
                    </Card>

                    {/* Static Performance Analysis Summary Card */}
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <Cpu className="w-4 h-4 text-amber-600" />
                            <CardTitle className="text-sm">Static Performance Analysis Summary</CardTitle>
                          </div>
                          {performanceSummary?.performanceCompleteness && (
                            <Badge
                              variant={
                                performanceSummary.performanceCompleteness === 'COMPLETE'
                                  ? 'ready'
                                  : performanceSummary.performanceCompleteness === 'PARTIAL'
                                  ? 'warning'
                                  : 'neutral'
                              }
                              size="sm"
                            >
                              {performanceSummary.performanceCompleteness}
                            </Badge>
                          )}
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                          <div className="p-2.5 bg-slate-50 rounded-xl border border-slate-200">
                            <span className="text-[10px] text-slate-400 font-medium block">Total Findings</span>
                            <span className="text-base font-bold text-slate-900">{performanceSummary?.totalPerformanceFindings ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-rose-50/50 rounded-xl border border-rose-100">
                            <span className="text-[10px] text-rose-700 font-medium block">High Severity</span>
                            <span className="text-base font-bold text-rose-700">{performanceSummary?.highSeverityFindings ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-amber-50/50 rounded-xl border border-amber-100">
                            <span className="text-[10px] text-amber-700 font-medium block">Medium Severity</span>
                            <span className="text-base font-bold text-amber-700">{performanceSummary?.mediumSeverityFindings ?? 0}</span>
                          </div>
                          <div className="p-2.5 bg-blue-50/50 rounded-xl border border-blue-100">
                            <span className="text-[10px] text-blue-700 font-medium block">Low Severity</span>
                            <span className="text-base font-bold text-blue-700">{performanceSummary?.lowSeverityFindings ?? 0}</span>
                          </div>
                        </div>

                        <div className="grid grid-cols-2 gap-2 text-[11px] p-2.5 bg-slate-50 rounded-xl border border-slate-200 font-mono">
                          <div>
                            <span className="text-slate-400 block text-[10px]">Affected Files</span>
                            <span className="font-bold text-slate-800">{performanceSummary?.affectedFiles ?? 0}</span>
                          </div>
                          <div>
                            <span className="text-slate-400 block text-[10px]">Source Files Analyzed</span>
                            <span className="font-bold text-indigo-700">{performanceSummary?.analyzedSourceFiles ?? 0}</span>
                          </div>
                        </div>

                        <p className="text-[11px] text-slate-400 italic">
                          {performanceSummary?.disclaimer || 'Performance analysis is static and heuristic. It does not measure runtime CPU, memory, latency, throughput, or actual production performance.'}
                        </p>
                      </CardContent>
                    </Card>
                  </div>

                  {/* Findings List Section */}
                  <Card>
                    <CardHeader className="pb-3 border-b border-slate-100">
                      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                        <div className="flex items-center gap-2">
                          <Filter className="w-4 h-4 text-indigo-600" />
                          <CardTitle className="text-sm">Static Findings List ({filteredFindings.length})</CardTitle>
                        </div>
                        <div className="flex flex-wrap items-center gap-2 text-xs">
                          {/* Category Filters */}
                          <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-lg">
                            {['ALL', 'CODE_QUALITY', 'TESTING', 'DEPENDENCIES', 'SECURITY', 'PERFORMANCE'].map((cat) => (
                              <button
                                key={cat}
                                onClick={() => setCategoryFilter(cat)}
                                className={`px-2 py-0.5 rounded-md font-semibold text-[11px] transition ${
                                  categoryFilter === cat
                                    ? 'bg-white text-slate-900 shadow-xs'
                                    : 'text-slate-500 hover:text-slate-800'
                                }`}
                              >
                                {cat === 'ALL' ? 'All Categories' : cat}
                              </button>
                            ))}
                          </div>

                          {/* Severity Filters */}
                          <div className="flex items-center gap-1">
                            {['ALL', 'HIGH', 'MEDIUM', 'LOW'].map((sev) => (
                              <button
                                key={sev}
                                onClick={() => setSeverityFilter(sev)}
                                className={`px-2 py-1 rounded-lg font-semibold text-[11px] transition ${
                                  severityFilter === sev
                                    ? 'bg-indigo-600 text-white shadow-xs'
                                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                                }`}
                              >
                                {sev}
                              </button>
                            ))}
                          </div>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent className="pt-4 space-y-3">
                      {filteredFindings.length === 0 ? (
                        <div className="py-8 text-center space-y-2">
                          <CheckCircle2 className="w-8 h-8 text-emerald-500 mx-auto" />
                          <p className="text-xs font-semibold text-slate-700">No findings for this filter</p>
                          <p className="text-[11px] text-slate-400 max-w-sm mx-auto">
                            No static findings were detected matching the selected criteria.
                          </p>
                        </div>
                      ) : (
                        filteredFindings.map((finding, idx) => (
                          <div
                            key={idx}
                            className="p-4 rounded-xl border border-slate-200 bg-white hover:border-slate-300 transition space-y-2.5"
                          >
                            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                              <div className="flex flex-wrap items-center gap-2">
                                <Badge
                                  variant={
                                    finding.severity === 'HIGH'
                                      ? 'critical'
                                      : finding.severity === 'MEDIUM'
                                      ? 'warning'
                                      : 'info'
                                  }
                                  size="sm"
                                >
                                  {finding.severity}
                                </Badge>
                                <Badge variant="neutral" size="sm">
                                  {finding.category || 'ANALYSIS'}
                                </Badge>
                                <h4 className="text-xs font-bold text-slate-900">{finding.title}</h4>
                              </div>
                              <span className="font-mono text-[11px] text-indigo-600 font-semibold bg-indigo-50 px-2 py-0.5 rounded border border-indigo-100 self-start sm:self-auto">
                                {finding.ruleId}
                              </span>
                            </div>

                            <p className="text-xs text-slate-600 leading-relaxed">{finding.description}</p>

                            <div className="flex flex-wrap items-center gap-3 text-[11px] text-slate-500 font-mono pt-1 border-t border-slate-100">
                              <span className="text-slate-800 font-bold">
                                {finding.filePath}
                                {finding.lineNumber ? `:${finding.lineNumber}` : ''}
                              </span>
                              {finding.confidence && (
                                <span className="text-slate-400">• Confidence: {finding.confidence}</span>
                              )}
                            </div>

                            {finding.evidence && (
                              <div className="p-2.5 rounded-lg bg-slate-900 text-slate-100 text-[11px] font-mono overflow-x-auto leading-relaxed border border-slate-800">
                                {finding.evidence}
                              </div>
                            )}
                          </div>
                        ))
                      )}
                    </CardContent>
                  </Card>

                  {/* Project Profile Cards */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Technology Stack Card */}
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center gap-2">
                          <Cpu className="w-4 h-4 text-indigo-600" />
                          <CardTitle className="text-sm">Detected Technology Profile</CardTitle>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3 text-xs">
                        <div className="flex justify-between py-2 border-b border-slate-100">
                          <span className="text-slate-500">Primary Language</span>
                          <span className="font-bold text-indigo-600 font-mono">
                            {profile?.primaryLanguage || 'UNKNOWN'}
                          </span>
                        </div>
                        <div className="flex justify-between py-2 border-b border-slate-100">
                          <span className="text-slate-500">Framework</span>
                          <span className="font-semibold text-slate-800 font-mono">
                            {profile?.framework || 'UNKNOWN'}
                          </span>
                        </div>
                        <div className="flex justify-between py-2 border-b border-slate-100">
                          <span className="text-slate-500">Build System</span>
                          <span className="font-semibold text-slate-800 font-mono">
                            {profile?.buildSystem || 'UNKNOWN'}
                          </span>
                        </div>
                        <div className="flex justify-between py-2 border-b border-slate-100">
                          <span className="text-slate-500">Project Type</span>
                          <Badge variant="info" size="sm">
                            {profile?.projectType || 'UNKNOWN'}
                          </Badge>
                        </div>
                        <div className="flex justify-between py-2 border-b border-slate-100">
                          <span className="text-slate-500">Testing Setup</span>
                          <span className="font-medium text-slate-800">
                            {profile?.testFrameworks && profile.testFrameworks.length > 0
                              ? profile.testFrameworks.join(', ')
                              : 'None Detected'}
                          </span>
                        </div>
                        <div className="flex justify-between py-2 border-b border-slate-100">
                          <span className="text-slate-500">Database</span>
                          <span className="font-medium text-slate-800 flex items-center gap-1">
                            <Database className="w-3 h-3 text-slate-400" />
                            {profile?.database || 'NONE'}
                          </span>
                        </div>
                        <div className="flex justify-between py-2">
                          <span className="text-slate-500">Package Manager</span>
                          <span className="font-mono text-slate-700">{profile?.packageManager || 'N/A'}</span>
                        </div>
                      </CardContent>
                    </Card>

                    {/* Structure & Manifests Card */}
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center gap-2">
                          <FileText className="w-4 h-4 text-indigo-600" />
                          <CardTitle className="text-sm">Structure & Manifests</CardTitle>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3 text-xs">
                        <div className="grid grid-cols-2 gap-2 p-3 bg-slate-50 rounded-xl border border-slate-200">
                          <div>
                            <span className="text-[11px] text-slate-400 block">Total Files</span>
                            <span className="text-sm font-bold text-slate-900">{structure?.totalFiles || 0}</span>
                          </div>
                          <div>
                            <span className="text-[11px] text-slate-400 block">Source Files</span>
                            <span className="text-sm font-bold text-indigo-600">{structure?.sourceFileCount || 0}</span>
                          </div>
                          <div>
                            <span className="text-[11px] text-slate-400 block">Test Files</span>
                            <span className="text-sm font-bold text-emerald-600">{structure?.testFileCount || 0}</span>
                          </div>
                          <div>
                            <span className="text-[11px] text-slate-400 block">Config Files</span>
                            <span className="text-sm font-bold text-slate-700">{structure?.configFileCount || 0}</span>
                          </div>
                        </div>

                        <div className="pt-2">
                          <span className="text-slate-500 block mb-1.5 font-semibold">Detected Manifests</span>
                          <div className="flex flex-wrap gap-1.5">
                            {profile?.detectedManifests && profile.detectedManifests.length > 0 ? (
                              profile.detectedManifests.map((m, idx) => (
                                <Badge key={idx} variant="neutral" size="sm">
                                  {m}
                                </Badge>
                              ))
                            ) : (
                              <span className="text-slate-400 italic">No manifests detected</span>
                            )}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  </div>

                  {/* Completeness & Warnings Card */}
                  <Card className={profile?.analysisCompleteness === 'PARTIAL' ? 'border-amber-200 bg-amber-50/10' : ''}>
                    <CardHeader className="pb-3">
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          <AlertTriangle className={`w-4 h-4 ${profile?.analysisCompleteness === 'PARTIAL' ? 'text-amber-600' : 'text-slate-600'}`} />
                          <CardTitle className="text-sm">Analysis Completeness & Warnings</CardTitle>
                        </div>
                        <Badge
                          variant={profile?.analysisCompleteness === 'COMPLETE' ? 'ready' : 'neutral'}
                          size="sm"
                        >
                          {profile?.analysisCompleteness || 'UNKNOWN'}
                        </Badge>
                      </div>
                    </CardHeader>
                    <CardContent className="space-y-3 text-xs">
                      {analysis.warnings && analysis.warnings.length > 0 ? (
                        <div className="space-y-2">
                          {analysis.warnings.map((warning, idx) => (
                            <div
                              key={idx}
                              className="p-3 rounded-xl bg-amber-50 border border-amber-200 text-amber-900 flex items-start gap-2.5 leading-relaxed"
                            >
                              <AlertCircle className="w-4 h-4 text-amber-600 shrink-0 mt-0.5" />
                              <span>{warning}</span>
                            </div>
                          ))}
                        </div>
                      ) : (
                        <p className="text-slate-500 italic">No detection or analysis warnings for this upload.</p>
                      )}
                    </CardContent>
                  </Card>

                  {/* Target Analysis Plan Card */}
                  {plan && (
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center gap-2">
                          <Layers className="w-4 h-4 text-indigo-600" />
                          <CardTitle className="text-sm">Future Analyzer Evaluation Plan</CardTitle>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3 text-xs">
                        <p className="text-slate-500">
                          Based on detected project profile, the following evaluation engines are scheduled for subsequent analysis stages:
                        </p>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
                          {plan.analyzers.map((analyzerKey, idx) => (
                            <div key={idx} className="p-3 rounded-xl bg-indigo-50/50 border border-indigo-100 space-y-1">
                              <div className="flex items-center justify-between">
                                <span className="font-bold text-indigo-950 font-mono block">{analyzerKey}</span>
                                {(analyzerKey === 'CODE_QUALITY' || analyzerKey === 'TESTING' || analyzerKey === 'DEPENDENCIES' || analyzerKey === 'SECURITY' || analyzerKey === 'PERFORMANCE') && (
                                  <Badge variant="ready" size="sm">Completed</Badge>
                                )}
                              </div>
                              <p className="text-[11px] text-slate-600 leading-normal">
                                {plan.rationale[analyzerKey] || 'Engine scheduled.'}
                              </p>
                            </div>
                          ))}
                        </div>
                      </CardContent>
                      <CardFooter className="justify-between border-t border-slate-100 pt-3">
                        <Link href={`/releases/${releaseId}`}>
                          <Button variant="outline" size="sm">
                            Return to Release Specs
                          </Button>
                        </Link>
                        <span className="text-[11px] text-slate-400">
                          Part 8 Code Quality Active • Code is untrusted data & static only
                        </span>
                      </CardFooter>
                    </Card>
                  )}
                </div>
              )}
            </div>
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
