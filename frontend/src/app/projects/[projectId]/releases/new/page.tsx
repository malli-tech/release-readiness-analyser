'use client';

import React, { useState } from 'react';
import { useParams } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import ReleaseForm from '@/components/releases/ReleaseForm';
import { useReleases } from '@/hooks/useReleases';

export default function NewReleasePage() {
  const params = useParams();
  const projectId = params?.projectId as string;
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { createRelease } = useReleases(projectId);

  const handleCreate = async (data: any) => {
    return await createRelease(projectId, data);
  };

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            <ReleaseForm projectId={projectId} onSubmit={handleCreate} />
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
