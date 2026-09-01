'use client';

import React, { useState } from 'react';
import { useParams } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import {
  CheckCircle2,
  Clock,
  ArrowLeft,
  FileCode2,
  ShieldCheck,
  Sparkles,
} from 'lucide-react';
import Link from 'next/link';

export default function ReleaseAnalysisPage() {
  const params = useParams();
  const releaseId = params?.releaseId as string;
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            <div className="max-w-2xl mx-auto space-y-6">
              <div>
                <Link
                  href={`/releases/${releaseId}`}
                  className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1 mb-2"
                >
                  <ArrowLeft className="w-3 h-3" /> Back to Release Details
                </Link>
                <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                  Analysis Pipeline Staging
                </h1>
                <p className="text-xs sm:text-sm text-slate-500 mt-1">
                  Project submission status and evaluation roadmap.
                </p>
              </div>

              <Card>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <ShieldCheck className="w-5 h-5 text-emerald-600" />
                      <CardTitle className="text-sm">Project Uploaded Successfully</CardTitle>
                    </div>
                    <Badge variant="neutral" dot size="sm">
                      NOT ANALYZED
                    </Badge>
                  </div>
                </CardHeader>

                <CardContent className="py-8 text-center space-y-4">
                  <div className="w-14 h-14 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center mx-auto border border-emerald-200 shadow-xs">
                    <CheckCircle2 className="w-7 h-7" />
                  </div>

                  <div className="space-y-1.5">
                    <h3 className="text-base font-bold text-slate-900">Project Uploaded Successfully</h3>
                    <p className="text-xs text-slate-500 max-w-md mx-auto leading-relaxed">
                      Your project source content has been safely received, verified, and extracted into the isolated sandbox workspace.
                    </p>
                  </div>

                  <div className="p-4 rounded-xl bg-slate-50 border border-slate-200 text-left space-y-2 text-xs">
                    <div className="flex items-center gap-2 font-semibold text-slate-800">
                      <Sparkles className="w-4 h-4 text-indigo-600" />
                      <span>Next Step: Multi-Engine Code & Quality Analyzer</span>
                    </div>
                    <p className="text-slate-500 text-[11px] leading-relaxed">
                      Analysis will be available in the next stage. The analyzer service will perform static code analysis, security vulnerability scanning, test coverage verification, and calculate the comprehensive AI Release Readiness score.
                    </p>
                  </div>
                </CardContent>

                <CardFooter className="justify-between border-t border-slate-100 pt-3">
                  <Link href={`/releases/${releaseId}`}>
                    <Button variant="outline" size="sm">
                      Return to Release
                    </Button>
                  </Link>
                  <Link href={`/releases/${releaseId}/upload`}>
                    <Button size="sm">
                      Re-upload Content
                    </Button>
                  </Link>
                </CardFooter>
              </Card>
            </div>
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
