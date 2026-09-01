'use client';

import React, { useState } from 'react';
import { useParams } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import { Card } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import ReadinessScore from '@/components/analysis/ReadinessScore';
import RiskSummary from '@/components/analysis/RiskSummary';
import RiskBreakdown from '@/components/analysis/RiskBreakdown';
import FindingsList from '@/components/analysis/FindingsList';
import AIExplanation from '@/components/analysis/AIExplanation';
import Recommendations from '@/components/analysis/Recommendations';
import VersionComparison from '@/components/analysis/VersionComparison';
import { mockAnalysisResult, mockFindings } from '@/lib/mock-data';
import { FileText } from 'lucide-react';
import Link from 'next/link';

export default function ReleaseResultsPage() {
  const params = useParams();
  const releaseId = params?.releaseId as string;
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-8 overflow-y-auto">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 bg-white rounded-2xl border border-slate-200 shadow-xs">
              <div className="space-y-1">
                <div className="flex items-center gap-2">
                  <Link href="/projects" className="text-xs text-indigo-600 font-semibold hover:underline">
                    Projects
                  </Link>
                  <span className="text-slate-400">•</span>
                  <span className="font-mono text-xs font-bold text-slate-700 bg-slate-100 px-2 py-0.5 rounded">
                    Release Evaluation
                  </span>
                  <Badge variant="review" dot size="sm">
                    NEEDS REVIEW
                  </Badge>
                </div>
                <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                  Release Readiness Evaluation Report
                </h1>
                <p className="text-xs text-slate-500">
                  Analysis completed on August 29, 2026 at 14:32 UTC • 184 files scanned
                </p>
              </div>

              <div className="flex items-center gap-2">
                <Link href="/reports">
                  <Button variant="outline" size="sm" leftIcon={<FileText className="w-3.5 h-3.5" />}>
                    Export PDF Report
                  </Button>
                </Link>
                <Link href={`/releases/${releaseId}/upload`}>
                  <Button size="sm">
                    Re-evaluate Release
                  </Button>
                </Link>
              </div>

            </div>

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
              <div className="lg:col-span-4 flex">
                <Card className="w-full flex flex-col items-center justify-center p-6 text-center space-y-4">
                  <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                    Overall Readiness Score
                  </span>
                  <ReadinessScore score={82} status="NEEDS REVIEW" size="lg" />
                  <p className="text-xs text-slate-500 max-w-xs">
                    Evaluation failed immediate pass gate due to 1 critical plaintext credential in source configuration.
                  </p>
                </Card>
              </div>

              <div className="lg:col-span-8 space-y-6">
                <RiskSummary summary={mockAnalysisResult.findingSummary} />
                <RiskBreakdown categories={mockAnalysisResult.categoryScores} />
              </div>
            </div>

            <AIExplanation aiReview={mockAnalysisResult.aiReview} />

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
              <div className="lg:col-span-6">
                <Recommendations recommendations={mockAnalysisResult.recommendations} />
              </div>
              <div className="lg:col-span-6">
                <VersionComparison
                  previousVersion="v1.1"
                  currentVersion="v1.2"
                  metrics={mockAnalysisResult.comparison?.metrics || []}
                />
              </div>
            </div>

            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-base font-bold text-slate-900">Detected Issues & Code Locations</h3>
                  <p className="text-xs text-slate-500">Inspect offending code snippets with AI explanations</p>
                </div>
              </div>

              <FindingsList findings={mockFindings} />
            </div>
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
