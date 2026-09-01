'use client';

import React, { useState } from 'react';
import Navbar from '@/components/layout/Navbar';
import Sidebar from '@/components/layout/Sidebar';
import Footer from '@/components/layout/Footer';
import AuthGuard from '@/components/auth/AuthGuard';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import { mockReports } from '@/lib/mock-data';
import { getScoreColor } from '@/lib/utils';
import { FileText, Download, Eye } from 'lucide-react';
import Link from 'next/link';

export default function ReportsPage() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [downloadingId, setDownloadingId] = useState<string | null>(null);

  const handleDownload = (id: string) => {
    setDownloadingId(id);
    setTimeout(() => {
      setDownloadingId(null);
    }, 1000);
  };

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-6 overflow-y-auto">
            <div>
              <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
                Evaluation Reports
              </h1>
              <p className="text-xs sm:text-sm text-slate-500 mt-1">
                Exportable audit certificates and comprehensive readiness summaries.
              </p>
            </div>

            <Card>
              <CardHeader>
                <CardTitle className="text-sm">Generated Release Reports</CardTitle>
              </CardHeader>
              <CardContent className="p-0">
                <div className="overflow-x-auto">
                  <table className="w-full text-left text-xs">
                    <thead className="bg-slate-50 border-b border-slate-100 text-slate-500 font-semibold uppercase tracking-wider">
                      <tr>
                        <th className="px-5 py-3">Project & Version</th>
                        <th className="px-4 py-3">Readiness Score</th>
                        <th className="px-4 py-3">Status</th>
                        <th className="px-4 py-3">Date Generated</th>
                        <th className="px-4 py-3">File Size</th>
                        <th className="px-5 py-3 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 text-slate-700">
                      {mockReports.map((report) => {
                        const scoreInfo = getScoreColor(report.score);
                        return (
                          <tr key={report.id} className="hover:bg-slate-50/50">
                            <td className="px-5 py-3.5">
                              <div className="flex items-center gap-2">
                                <FileText className="w-4 h-4 text-indigo-600" />
                                <div>
                                  <span className="font-semibold text-slate-900">{report.projectName}</span>
                                  <span className="ml-2 font-mono text-[11px] text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded">
                                    {report.releaseVersion}
                                  </span>
                                </div>
                              </div>
                            </td>
                            <td className="px-4 py-3.5 font-bold text-slate-900">
                              {report.score}/100
                            </td>
                            <td className="px-4 py-3.5">
                              <Badge variant={scoreInfo.badge} dot size="sm">
                                {report.status}
                              </Badge>
                            </td>
                            <td className="px-4 py-3.5 text-slate-500">{report.generatedDate}</td>
                            <td className="px-4 py-3.5 text-slate-500">{report.size}</td>
                            <td className="px-5 py-3.5 text-right space-x-2">
                              <Link href="/releases/rel-102/results">
                                <Button size="sm" variant="ghost" leftIcon={<Eye className="w-3.5 h-3.5" />}>
                                  View
                                </Button>
                              </Link>
                              <Button
                                size="sm"
                                variant="outline"
                                loading={downloadingId === report.id}
                                onClick={() => handleDownload(report.id)}
                                leftIcon={<Download className="w-3.5 h-3.5" />}
                              >
                                Download PDF
                              </Button>
                            </td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </CardContent>
            </Card>
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
