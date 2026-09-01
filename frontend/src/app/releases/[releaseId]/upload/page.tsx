'use client';

import React, { useState } from 'react';
import { useParams } from 'next/navigation';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import UploadZone from '@/components/releases/UploadZone';
import Link from 'next/link';
import { ArrowLeft } from 'lucide-react';

export default function ReleaseUploadPage() {
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
            <div className="max-w-2xl mx-auto">
              <Link
                href={`/releases/${releaseId}`}
                className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1 mb-2"
              >
                <ArrowLeft className="w-3 h-3" /> Back to Release Details
              </Link>
            </div>


            <UploadZone releaseId={releaseId} />
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
