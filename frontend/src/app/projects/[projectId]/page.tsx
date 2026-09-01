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
import ProjectForm from '@/components/projects/ProjectForm';
import ReleaseHistory from '@/components/releases/ReleaseHistory';
import { Project, UpdateProjectRequest } from '@/types/project';
import { useProjects } from '@/hooks/useProjects';
import { useReleases } from '@/hooks/useReleases';
import { formatDate } from '@/lib/utils';
import {
  FolderGit2,
  Edit,
  Trash2,
  ExternalLink,
  Code2,
  Layers,
  Calendar,
  AlertTriangle,
  GitBranch,
  Plus,
  ArrowLeft,
} from 'lucide-react';
import Link from 'next/link';

export default function ProjectDetailsPage() {
  const params = useParams();
  const router = useRouter();
  const projectId = params?.projectId as string;
  const { getProject, updateProject, deleteProject } = useProjects();
  const { releases, loading: releasesLoading } = useReleases(projectId);

  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const loadProject = useCallback(async () => {
    if (!projectId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await getProject(projectId);
      setProject(data);
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Failed to load project details.');
      }
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    loadProject();
  }, [loadProject]);

  const handleUpdate = async (data: UpdateProjectRequest) => {
    if (!projectId) return;
    const updated = await updateProject(projectId, data);
    setProject(updated);
    setIsEditing(false);
  };

  const handleDelete = async () => {
    if (!projectId) return;
    setIsDeleting(true);
    try {
      await deleteProject(projectId);
      router.push('/projects');
    } catch (err: unknown) {
      setIsDeleting(false);
      setShowDeleteModal(false);
      setError(err instanceof Error ? err.message : 'Failed to delete project.');
    }
  };

  const latestRelease = releases.length > 0 ? releases[0] : null;

  return (
    <AuthGuard>
      <div className="min-h-screen flex flex-col bg-slate-50 text-slate-900">
        <Navbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} isSidebarOpen={sidebarOpen} />

        <div className="flex-1 flex max-w-7xl w-full mx-auto">
          <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

          <main className="flex-1 p-4 sm:p-6 lg:p-8 space-y-8 overflow-y-auto">
            {loading ? (
              <div className="py-20 flex flex-col items-center justify-center space-y-3 bg-white rounded-2xl border border-slate-200">
                <Spinner size="lg" label="Loading project specifications..." />
              </div>
            ) : error || !project ? (
              <div className="p-8 text-center bg-rose-50 rounded-2xl border border-rose-200 space-y-3 max-w-md mx-auto">
                <p className="text-xs font-semibold text-rose-800">{error || 'Project not found'}</p>
                <Link href="/projects">
                  <Button size="sm" variant="outline" leftIcon={<ArrowLeft className="w-3.5 h-3.5" />}>
                    Back to Projects
                  </Button>
                </Link>
              </div>
            ) : isEditing ? (
              <div>
                <ProjectForm
                  initialData={project}
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
                      <FolderGit2 className="w-6 h-6" />
                    </div>
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <h1 className="text-xl font-bold text-slate-900">{project.name}</h1>
                        <Badge variant="neutral" size="sm">
                          {(project.projectType || project.type || 'WEB_APPLICATION').replace('_', ' ')}
                        </Badge>
                      </div>
                      <p className="text-xs text-slate-500 max-w-2xl">
                        {project.description || 'No description provided.'}
                      </p>
                      <div className="flex flex-wrap items-center gap-2 pt-1">
                        <span className="inline-flex items-center gap-1 text-[11px] font-mono px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                          <Code2 className="w-3 h-3 text-slate-400" />
                          {project.primaryLanguage || project.language || 'Code'}
                        </span>
                        {project.framework && (
                          <span className="inline-flex items-center gap-1 text-[11px] font-mono px-2 py-0.5 rounded bg-indigo-50 text-indigo-700">
                            <Layers className="w-3 h-3 text-indigo-400" />
                            {project.framework}
                          </span>
                        )}
                        {project.repositoryUrl && (
                          <a
                            href={project.repositoryUrl}
                            target="_blank"
                            rel="noreferrer"
                            className="inline-flex items-center gap-1 text-[11px] font-mono px-2 py-0.5 rounded bg-slate-50 border border-slate-200 text-slate-600 hover:text-indigo-600 hover:border-indigo-300 transition"
                          >
                            <ExternalLink className="w-3 h-3" />
                            Repository
                          </a>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="flex flex-wrap items-center gap-2 self-start sm:self-auto">
                    <Link href={`/projects/${projectId}/releases/new`}>
                      <Button size="sm" leftIcon={<Plus className="w-3.5 h-3.5" />}>
                        Create Release
                      </Button>
                    </Link>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setIsEditing(true)}
                      leftIcon={<Edit className="w-3.5 h-3.5" />}
                    >
                      Edit Project
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

                {/* Summary Row */}
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  <Card className="md:col-span-2">
                    <CardHeader className="flex flex-row items-center justify-between">
                      <CardTitle className="text-sm">Latest Release & Status</CardTitle>
                      <Link href={`/projects/${projectId}/releases`} className="text-xs text-indigo-600 font-semibold hover:underline">
                        View all releases ({releases.length})
                      </Link>
                    </CardHeader>
                    <CardContent className="py-8">
                      {latestRelease ? (
                        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-xl bg-slate-50 border border-slate-200">
                          <div className="space-y-1">
                            <div className="flex items-center gap-2">
                              <span className="font-mono font-bold text-sm text-indigo-600 bg-white px-2 py-0.5 rounded border border-indigo-100">
                                {latestRelease.version}
                              </span>
                              <span className="font-semibold text-slate-900 text-xs">{latestRelease.name}</span>
                              <Badge variant="neutral" dot size="sm">
                                {latestRelease.status ? latestRelease.status.replace('_', ' ') : 'NOT ANALYZED'}
                              </Badge>
                            </div>
                            <p className="text-xs text-slate-500">
                              Created on {formatDate(latestRelease.createdAt)}
                            </p>
                          </div>

                          <div className="flex items-center gap-2">
                            <Link href={`/releases/${latestRelease.id}`}>
                              <Button size="sm" variant="outline">
                                Release Details
                              </Button>
                            </Link>
                            <Link href={`/releases/${latestRelease.id}/upload`}>
                              <Button size="sm">
                                Upload Project
                              </Button>
                            </Link>
                          </div>
                        </div>
                      ) : (
                        <div className="text-center py-4 space-y-2">
                          <p className="text-xs font-bold text-slate-700">No releases yet</p>
                          <p className="text-[11px] text-slate-400 max-w-sm mx-auto">
                            Create your first candidate version to begin tracking evaluation milestones.
                          </p>
                          <Link href={`/projects/${projectId}/releases/new`}>
                            <Button size="sm" leftIcon={<Plus className="w-3.5 h-3.5" />}>
                              + Create Release
                            </Button>
                          </Link>
                        </div>
                      )}
                    </CardContent>
                  </Card>

                  <Card>
                    <CardHeader>
                      <CardTitle className="text-sm">Project Metadata</CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-3 text-xs">
                      <div className="flex justify-between py-1.5 border-b border-slate-100">
                        <span className="text-slate-500">Total Releases</span>
                        <span className="font-bold text-slate-900">{releases.length}</span>
                      </div>
                      <div className="flex justify-between py-1.5 border-b border-slate-100">
                        <span className="text-slate-500">Created Date</span>
                        <span className="font-medium text-slate-800 flex items-center gap-1">
                          <Calendar className="w-3 h-3 text-slate-400" />
                          {formatDate(project.createdAt)}
                        </span>
                      </div>
                      <div className="flex justify-between py-1.5 border-b border-slate-100">
                        <span className="text-slate-500">Last Modified</span>
                        <span className="font-medium text-slate-800 flex items-center gap-1">
                          <Calendar className="w-3 h-3 text-slate-400" />
                          {formatDate(project.updatedAt)}
                        </span>
                      </div>
                      <div className="flex justify-between py-1.5">
                        <span className="text-slate-500">Access Scope</span>
                        <span className="font-semibold text-emerald-600">Owner Only</span>
                      </div>
                    </CardContent>
                  </Card>
                </div>

                {/* Release History Table */}
                <ReleaseHistory projectId={projectId} releases={releases} />
              </div>
            )}

            {/* Delete Confirmation Modal */}
            {showDeleteModal && (
              <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
                <Card className="max-w-md w-full border-rose-200 shadow-2xl animate-in fade-in zoom-in-95 duration-150">
                  <CardHeader className="pb-3">
                    <div className="flex items-center gap-2.5 text-rose-600">
                      <AlertTriangle className="w-5 h-5" />
                      <CardTitle className="text-base text-rose-900">Delete Project</CardTitle>
                    </div>
                  </CardHeader>
                  <CardContent className="space-y-2">
                    <p className="text-xs text-slate-700">
                      Are you sure you want to delete <strong className="text-slate-900">{project?.name}</strong>?
                    </p>
                    <p className="text-[11px] text-slate-500">
                      This action is permanent and cannot be undone. All associated project releases will be removed.
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
                      Yes, Delete Project
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
