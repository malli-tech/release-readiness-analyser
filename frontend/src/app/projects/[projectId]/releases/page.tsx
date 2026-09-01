'use client';

import React, { useState } from 'react';
import { useParams } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import ReleaseHistory from '@/components/releases/ReleaseHistory';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { useReleases } from '@/hooks/useReleases';
import { useProjects } from '@/hooks/useProjects';
import { Plus, ArrowLeft, AlertCircle } from 'lucide-react';
import Link from 'next/link';

export default function ProjectReleasesPage() {
  const params = useParams();
  const projectId = params?.projectId as string;
  const { releases, loading, error } = useReleases(projectId);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <Link
                    href={`/projects/${projectId}`}
                    className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1"
                  >
                    <ArrowLeft className="w-3 h-3" /> Back to Project
                  </Link>
                </div>
                <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                  Release History
                </h1>
                <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
                  Track candidate versions, evaluation records, and release progression.
                </p>
              </div>

              <Link href={`/projects/${projectId}/releases/new`}>
                <Button size="sm" leftIcon={<Plus className="w-3.5 h-3.5" />}>
                  New Release
                </Button>
              </Link>
            </div>

            {loading ? (
              <div className="py-16 flex flex-col items-center justify-center space-y-3 bg-white rounded-xl border border-slate-200">
                <Spinner size="lg" label="Loading releases..." />
              </div>
            ) : error ? (
              <div className="p-8 text-center bg-rose-50 rounded-xl border border-rose-200 space-y-3">
                <AlertCircle className="w-5 h-5 text-rose-600 mx-auto" />
                <p className="text-xs font-semibold text-rose-800">{error}</p>
              </div>
            ) : (
              <ReleaseHistory projectId={projectId} releases={releases} />
            )}
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
