'use client';

import React, { useState } from 'react';
import { useParams } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { Save, ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export default function ProjectSettingsPage() {
  const params = useParams();
  const projectId = params?.projectId as string;
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const [minPassScore, setMinPassScore] = useState(85);
  const [blockOnCritical, setBlockOnCritical] = useState(true);
  const [saved, setSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  };

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            <div>
              <Link href={`/projects/${projectId}`} className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1 mb-2">
                <ArrowLeft className="w-3 h-3" /> Back to Project
              </Link>
              <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                Project Settings
              </h1>
              <p className="text-xs sm:text-sm text-slate-500 mt-1">
                Configure analysis gating criteria, test coverage thresholds, and notification webhooks.
              </p>
            </div>

            <form onSubmit={handleSave} className="max-w-2xl space-y-6">
              <Card>
                <CardHeader>
                  <CardTitle className="text-sm">Release Readiness Gate Configuration</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="space-y-1.5">
                    <label className="block text-xs font-semibold text-slate-700">
                      Minimum Passing Score (0 - 100)
                    </label>
                    <input
                      type="number"
                      min="50"
                      max="100"
                      value={minPassScore}
                      onChange={(e) => setMinPassScore(Number(e.target.value))}
                      className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                    />
                    <p className="text-[11px] text-slate-400">
                      Releases scoring below this threshold are automatically labeled &quot;NEEDS REVIEW&quot; or &quot;NOT READY&quot;.
                    </p>
                  </div>

                  <div className="flex items-center gap-3 pt-2">
                    <input
                      type="checkbox"
                      id="blockCritical"
                      checked={blockOnCritical}
                      onChange={(e) => setBlockOnCritical(e.target.checked)}
                      className="w-4 h-4 text-indigo-600 rounded"
                    />
                    <label htmlFor="blockCritical" className="text-xs font-medium text-slate-700">
                      Strict Gate: Block release if even 1 Critical security vulnerability is found.
                    </label>
                  </div>
                </CardContent>
                <CardFooter className="justify-between">
                  <span className="text-xs text-emerald-600 font-semibold">{saved ? 'Settings updated successfully!' : ''}</span>
                  <Button type="submit" size="sm" leftIcon={<Save className="w-3.5 h-3.5" />}>
                    Save Preferences
                  </Button>
                </CardFooter>
              </Card>
            </form>
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
