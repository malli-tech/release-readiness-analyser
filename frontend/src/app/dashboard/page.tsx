'use client';

import React, { useState } from 'react';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import StatsCards from '@/components/dashboard/StatsCards';
import RiskOverview from '@/components/dashboard/RiskOverview';
import RecentReleases from '@/components/dashboard/RecentReleases';
import ScoreTrend from '@/components/dashboard/ScoreTrend';
import { useAuth } from '@/hooks/useAuth';
import { useProjects } from '@/hooks/useProjects';
import { Plus } from 'lucide-react';
import Link from 'next/link';
import Button from '@/components/ui/Button';

export default function DashboardPage() {
  const { user } = useAuth();
  const { projects } = useProjects();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-8 overflow-y-auto">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                  Good morning, {user?.name || 'Developer'}
                </h1>
                <p className="text-xs sm:text-sm text-slate-500 mt-1">
                  Here is an overview of your project release readiness and security posture.
                </p>
              </div>

              <div className="flex items-center gap-2.5">
                <Link href="/projects/new">
                  <Button size="sm" leftIcon={<Plus className="w-4 h-4" />}>
                    New Project
                  </Button>
                </Link>
              </div>
            </div>

            <StatsCards
              totalProjects={projects.length}
              totalReleases={0}
              projectsReady={0}
              issuesFound={0}
            />

            <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
              <div className="lg:col-span-7">
                <RiskOverview overallScore={82} status="NEEDS REVIEW" projectName="Evaluation System" />
              </div>
              <div className="lg:col-span-5">
                <ScoreTrend />
              </div>
            </div>

            <RecentReleases />
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
