'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { useAnalysis } from '@/hooks/useAnalysis';
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
  Terminal,
  FileText,
  AlertCircle,
  Play,
  RefreshCw,
  Search,
} from 'lucide-react';
import Link from 'next/link';

export default function ReleaseAnalysisPage() {
  const params = useParams();
  const router = useRouter();
  const releaseId = params?.releaseId as string;
  const [sidebarOpen, setSidebarOpen] = useState(false);

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
                      Project Detection & Analysis Foundation
                    </h1>
                    <p className="text-xs sm:text-sm text-slate-500 mt-1">
                      Static project inspection, profile generation, and evaluation planning.
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
                      Re-run Detection
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
                        <CardTitle className="text-sm">Ready for Project Detection</CardTitle>
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
                      <h3 className="text-base font-bold text-slate-900">Initiate Static Technology Detection</h3>
                      <p className="text-xs text-slate-500 leading-relaxed">
                        Analyze the uploaded archive in the isolated workspace. Detection is 100% static and will identify primary languages, frameworks, build tools, databases, and testing setup without executing uploaded code.
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
                        {loading ? 'Analyzing Workspace...' : 'Start Static Detection'}
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
                          <h2 className="text-sm font-bold text-slate-900">Project Detection Completed</h2>
                          <Badge variant="ready" dot size="sm">
                            {analysis.status}
                          </Badge>
                        </div>
                        <p className="text-xs text-slate-500">
                          Run #{analysis.runNumber} • Analyzed workspace statically
                        </p>
                      </div>
                    </div>

                    <div className="text-xs text-slate-400 font-mono">
                      ID: {analysis.id.substring(0, 12)}...
                    </div>
                  </div>

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
                      {profile?.detectionWarnings && profile.detectionWarnings.length > 0 ? (
                        <div className="space-y-2">
                          {profile.detectionWarnings.map((warning, idx) => (
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
                        <p className="text-slate-500 italic">No detection warnings for this upload.</p>
                      )}
                    </CardContent>
                  </Card>

                  {/* Detection Evidences Card */}
                  {profile?.detectionEvidences && profile.detectionEvidences.length > 0 && (
                    <Card>
                      <CardHeader className="pb-3">
                        <div className="flex items-center gap-2">
                          <Sparkles className="w-4 h-4 text-indigo-600" />
                          <CardTitle className="text-sm">Static Detection Evidence</CardTitle>
                        </div>
                      </CardHeader>
                      <CardContent className="space-y-3">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                          {profile.detectionEvidences.map((ev, idx) => (
                            <div key={idx} className="p-3 rounded-xl bg-slate-50 border border-slate-200 space-y-1.5 text-xs">
                              <div className="flex items-center justify-between">
                                <span className="font-bold text-slate-900 font-mono">{ev.technology}</span>
                                <Badge variant="info" size="sm">{ev.confidence} confidence</Badge>
                              </div>
                              <ul className="list-disc list-inside text-slate-500 space-y-0.5 font-mono text-[11px]">
                                {ev.evidence.map((item, eIdx) => (
                                  <li key={eIdx}>{item}</li>
                                ))}
                              </ul>
                            </div>
                          ))}
                        </div>
                      </CardContent>
                    </Card>
                  )}

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
                          Based on detected project profile, the following evaluation engines have been scheduled for subsequent analysis stages:
                        </p>
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
                          {plan.analyzers.map((analyzerKey, idx) => (
                            <div key={idx} className="p-3 rounded-xl bg-indigo-50/50 border border-indigo-100 space-y-1">
                              <span className="font-bold text-indigo-950 font-mono block">{analyzerKey}</span>
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
                          Part 7 Foundation Active • Code is untrusted data & static only
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
