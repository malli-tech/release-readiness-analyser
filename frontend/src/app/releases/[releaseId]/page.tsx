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
import ReleaseForm from '@/components/releases/ReleaseForm';
import { Release, UpdateReleaseRequest } from '@/types/release';
import { useReleases } from '@/hooks/useReleases';
import { formatDate } from '@/lib/utils';
import {
  GitBranch,
  Edit,
  Trash2,
  Calendar,
  AlertTriangle,
  UploadCloud,
  ArrowLeft,
  Clock,
  FileCode2,
} from 'lucide-react';
import Link from 'next/link';

export default function ReleaseDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const releaseId = params?.releaseId as string;
  const { getRelease, updateRelease, deleteRelease } = useReleases();

  const [release, setRelease] = useState<Release | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const loadRelease = useCallback(async () => {
    if (!releaseId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await getRelease(releaseId);
      setRelease(data);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Failed to load release specifications.');
      }
    } finally {
      setLoading(false);
    }
  }, [releaseId]);

  useEffect(() => {
    loadRelease();
  }, [loadRelease]);

  const handleUpdate = async (data: UpdateReleaseRequest) => {
    if (!releaseId) return;
    const updated = await updateRelease(releaseId, data);
    setRelease(updated);
    setIsEditing(false);
  };

  const handleDelete = async () => {
    if (!releaseId || !release) return;
    setIsDeleting(true);
    try {
      await deleteRelease(releaseId);
      router.push(`/projects/${release.projectId}/releases`);
    } catch (err: unknown) {
      setIsDeleting(false);
      setShowDeleteModal(false);
      setError(err instanceof Error ? err.message : 'Failed to delete release.');
    }
  };

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-8 overflow-y-auto">
            {loading ? (
              <div className="py-20 flex flex-col items-center justify-center space-y-3 bg-white rounded-2xl border border-slate-200">
                <Spinner size="lg" label="Loading release specifications..." />
              </div>
            ) : error || !release ? (
              <div className="p-8 text-center bg-rose-50 rounded-2xl border border-rose-200 space-y-3 max-w-md mx-auto">
                <p className="text-xs font-semibold text-rose-800">{error || 'Release not found'}</p>
                <Link href="/projects">
                  <Button size="sm" variant="outline" leftIcon={<ArrowLeft className="w-3.5 h-3.5" />}>
                    Back to Projects
                  </Button>
                </Link>
              </div>
            ) : isEditing ? (
              <div>
                <ReleaseForm
                  projectId={release.projectId}
                  initialData={release}
                  onSubmit={handleUpdate}
                  isEdit={true}
                />
              </div>
            ) : (
              <div className="space-y-6">
                {/* Header Card */}
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-6 bg-white rounded-2xl border border-slate-200 shadow-xs">
                  <div className="flex items-start gap-3.5">
                    <div className="p-3 rounded-xl bg-indigo-50 text-indigo-600 border border-indigo-100">
                      <GitBranch className="w-6 h-6" />
                    </div>
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <Link
                          href={`/projects/${release.projectId}/releases`}
                          className="text-xs text-indigo-600 font-semibold hover:underline"
                        >
                          Releases
                        </Link>
                        <span className="text-slate-400">•</span>
                        <h1 className="text-xl font-bold text-slate-900 font-mono">{release.version}</h1>
                        <Badge variant="neutral" dot size="sm">
                          {release.status ? release.status.replace('_', ' ') : 'NOT ANALYZED'}
                        </Badge>
                      </div>
                      <p className="text-sm font-semibold text-slate-800">{release.name}</p>
                      <p className="text-xs text-slate-500 max-w-2xl">
                        {release.description || 'No description or release notes provided.'}
                      </p>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-2 self-start sm:self-auto">
                    <Link href={`/releases/${releaseId}/upload`}>
                      <Button size="sm" leftIcon={<UploadCloud className="w-3.5 h-3.5" />}>
                        Upload Project
                      </Button>
                    </Link>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setIsEditing(true)}
                      leftIcon={<Edit className="w-3.5 h-3.5" />}
                    >
                      Edit Release
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      onClick={() => setShowDeleteModal(true)}
                      leftIcon={<Trash2 className="w-3.5 h-3.5" />}
                    >
                      Delete
                    </Button>
                  </div>
                </div>

                {/* Status & Details Section */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <Card className="md:col-span-2">
                    <CardHeader>
                      <CardTitle className="text-sm">Release Readiness Status</CardTitle>
                    </CardHeader>
                    <CardContent className="py-12 text-center space-y-3">
                      <div className="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto border border-amber-200">
                        <Clock className="w-6 h-6" />
                      </div>
                      <div className="space-y-1">
                        <h3 className="text-sm font-bold text-slate-900">NOT ANALYZED</h3>
                        <p className="text-xs text-slate-500 max-w-sm mx-auto">
                          This release has not been analyzed yet. Upload a project source archive (.ZIP) to initiate the automated multi-engine evaluation pipeline.
                        </p>
                      </div>
                      <Link href={`/releases/${releaseId}/upload`}>
                        <Button size="sm" leftIcon={<UploadCloud className="w-3.5 h-3.5" />}>
                          Upload Project (.ZIP)
                        </Button>
                      </Link>
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="text-sm">Release Metadata</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3 text-xs">
                      <div className="flex justify-between py-1.5 border-b border-slate-100">
                        <span className="text-slate-500">Release ID</span>
                        <span className="font-mono text-[11px] text-slate-800">{release.id}</span>
                      </div>
                      <div className="flex justify-between py-1.5 border-b border-slate-100">
                        <span className="text-slate-500">Project ID</span>
                        <span className="font-mono text-[11px] text-slate-800">{release.projectId}</span>
                      </div>
                      <div className="flex justify-between py-1.5 border-b border-slate-100">
                        <span className="text-slate-500">Version</span>
                        <span className="font-mono font-bold text-indigo-600">{release.version}</span>
                      </div>
                      <div className="flex justify-between py-1.5 border-b border-slate-100">
                        <span className="text-slate-500">Created Date</span>
                        <span className="font-medium text-slate-800 flex items-center gap-1">
                          <Calendar className="w-3 h-3 text-slate-400" />
                          {formatDate(release.createdAt)}
                        </span>
                      </div>
                      <div className="flex justify-between py-1.5">
                        <span className="text-slate-500">Last Modified</span>
                        <span className="font-medium text-slate-800 flex items-center gap-1">
                          <Calendar className="w-3 h-3 text-slate-400" />
                          {formatDate(release.updatedAt || release.createdAt)}
                        </span>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </div>
            )}

            {/* Delete Confirmation Modal */}
            {showDeleteModal && (
              <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
                <Card className="max-w-md w-full border-rose-200 shadow-2xl animate-in fade-in zoom-in-95 duration-150">
                  <CardHeader className="pb-3">
                    <div className="flex items-center gap-2.5 text-rose-600">
                      <AlertTriangle className="w-5 h-5" />
                      <CardTitle className="text-base text-rose-900">Delete Release</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    <p className="text-xs text-slate-700">
                      Are you sure you want to delete release <strong className="text-slate-900 font-mono">{release?.version}</strong> ({release?.name})?
                    </p>
                    <p className="text-[11px] text-slate-500">
                      This action is permanent and will remove this release version from the project.
                    </p>
                  </CardContent>
                  <CardFooter className="justify-end gap-2.5 bg-slate-50 border-t border-slate-100">
                    <Button
                      variant="outline"
                      size="sm"
                      disabled={isDeleting}
                      onClick={() => setShowDeleteModal(false)}
                    >
                      Cancel
                    </Button>
                    <Button
                      variant="danger"
                      size="sm"
                      loading={isDeleting}
                      onClick={handleDelete}
                    >
                      Yes, Delete Release
                    </Button>
                  </CardFooter>
                </Card>
              </div>
            )}
          </main>
        </div>

        <Footer />
      </div>
    </AuthGuard>
  );
}
