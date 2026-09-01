'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { Project, CreateProjectRequest, UpdateProjectRequest } from '@/types/project';
import { PROJECT_TYPES, TECH_LANGUAGES } from '@/lib/constants';
import { FolderGit2, AlertCircle, Save, ArrowLeft } from 'lucide-react';
import Link from 'next/link';


export interface ProjectFormProps {
  initialData?: Project;
  onSubmit: (data: CreateProjectRequest | UpdateProjectRequest) => Promise<Project | void>;
  isEdit?: boolean;
}

export const ProjectForm: React.FC<ProjectFormProps> = ({
  initialData,
  onSubmit,
  isEdit = false,
}) => {
  const router = useRouter();
  const [name, setName] = useState(initialData?.name || '');
  const [description, setDescription] = useState(initialData?.description || '');
  const [projectType, setProjectType] = useState(initialData?.projectType || 'WEB_APPLICATION');
  const [primaryLanguage, setPrimaryLanguage] = useState(initialData?.primaryLanguage || 'Java');
  const [framework, setFramework] = useState(initialData?.framework || 'Spring Boot');
  const [repositoryUrl, setRepositoryUrl] = useState(initialData?.repositoryUrl || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('Project name is required.');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      const payload: CreateProjectRequest = {
        name: name.trim(),
        description: description.trim(),
        projectType,
        primaryLanguage,
        framework: framework.trim(),
        repositoryUrl: repositoryUrl.trim() || undefined,
      };

      const result = await onSubmit(payload);
      if (!isEdit && result && typeof result === 'object' && 'id' in result) {
        router.push(`/projects/${result.id}`);
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Failed to save project. Please check your inputs.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-2xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <Link
            href={isEdit && initialData ? `/projects/${initialData.id}` : '/projects'}
            className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1 mb-2"
          >
            <ArrowLeft className="w-3 h-3" /> Back
          </Link>
          <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            {isEdit ? 'Edit Project Details' : 'Register New Project'}
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            {isEdit
              ? 'Update repository configuration, technology stack, and classification.'
              : 'Create a tracked repository to begin automated release readiness evaluations.'}
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <FolderGit2 className="w-4 h-4 text-indigo-600" />
            <CardTitle className="text-sm">Project Specifications</CardTitle>
          </div>
        </CardHeader>

        <CardContent className="space-y-4">
          {error && (
            <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-700">
              Project Name <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              maxLength={100}
              placeholder="e.g. Student Management System"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-700">Description</label>
            <textarea
              rows={3}
              maxLength={1000}
              placeholder="Provide a short summary of this application and its key responsibilities..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-700">
                Project Type <span className="text-rose-500">*</span>
              </label>
              <select
                value={projectType}
                onChange={(e) => setProjectType(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500 bg-white"
              >
                {PROJECT_TYPES.map((t) => (
                  <option key={t} value={t}>
                    {t}
                  </option>
                ))}
              </select>
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-700">
                Primary Language <span className="text-rose-500">*</span>
              </label>
              <select
                value={primaryLanguage}
                onChange={(e) => setPrimaryLanguage(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500 bg-white"
              >
                {TECH_LANGUAGES.map((l) => (
                  <option key={l} value={l}>
                    {l}
                  </option>
                ))}
              </select>
            </div>
          </div>


          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-700">Framework / Tech Stack</label>
              <input
                type="text"
                placeholder="e.g. Spring Boot 3.3, Next.js 15, FastAPI"
                value={framework}
                onChange={(e) => setFramework(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-700">
                Repository URL (optional)
              </label>
              <input
                type="url"
                placeholder="https://github.com/org/repo"
                value={repositoryUrl}
                onChange={(e) => setRepositoryUrl(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>
          </div>
        </CardContent>

        <CardFooter className="justify-end gap-3 border-t border-slate-100">
          <Link href={isEdit && initialData ? `/projects/${initialData.id}` : '/projects'}>
            <Button type="button" variant="outline" size="sm">
              Cancel
            </Button>
          </Link>
          <Button
            type="submit"
            loading={loading}
            size="sm"
            leftIcon={<Save className="w-3.5 h-3.5" />}
          >
            {isEdit ? 'Update Project' : 'Create Project'}
          </Button>
        </CardFooter>
      </Card>
    </form>
  );
};

export default ProjectForm;
