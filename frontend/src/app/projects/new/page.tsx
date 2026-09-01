'use client';

import React, { useState } from 'react';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import ProjectForm from '@/components/projects/ProjectForm';
import { useProjects } from '@/hooks/useProjects';

export default function NewProjectPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { createProject } = useProjects();

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            <ProjectForm onSubmit={createProject} />
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
