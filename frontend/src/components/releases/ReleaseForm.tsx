'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Button from '@/components/ui/Button';
import { Release, CreateReleaseRequest, UpdateReleaseRequest } from '@/types/release';
import { GitBranch, AlertCircle, Save, ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export interface ReleaseFormProps {
  projectId: string;
  initialData?: Release;
  onSubmit: (data: CreateReleaseRequest | UpdateReleaseRequest) => Promise<Release | void>;
  isEdit?: boolean;
}

export const ReleaseForm: React.FC<ReleaseFormProps> = ({
  projectId,
  initialData,
  onSubmit,
  isEdit = false,
}) => {
  const router = useRouter();
  const [version, setVersion] = useState(initialData?.version || '');
  const [name, setName] = useState(initialData?.name || '');
  const [description, setDescription] = useState(initialData?.description || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!version.trim() || !name.trim()) {
      setError('Version and Release Name are required.');
      return;
    }

    setError(null);
    setLoading(true);

    try {
      const payload: CreateReleaseRequest = {
        version: version.trim(),
        name: name.trim(),
        description: description.trim() || undefined,
      };

      const result = await onSubmit(payload);
      if (!isEdit && result && typeof result === 'object' && 'id' in result) {
        router.push(`/releases/${result.id}`);
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Failed to save release. Please check your inputs.');
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
            href={isEdit && initialData ? `/releases/${initialData.id}` : `/projects/${projectId}/releases`}
            className="text-xs text-indigo-600 font-semibold hover:underline flex items-center gap-1 mb-2"
          >
            <ArrowLeft className="w-3 h-3" /> Back
          </Link>
          <h1 className="text-xl sm:text-2xl font-black text-slate-900 tracking-tight">
            {isEdit ? 'Edit Release Version' : 'Create New Project Release'}
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            {isEdit
              ? 'Update candidate version identifier and release notes.'
              : 'Register a candidate release version for future automated code audits.'}
          </p>
        </div>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center gap-2">
            <GitBranch className="w-4 h-4 text-indigo-600" />
            <CardTitle className="text-sm">Release Information</CardTitle>
          </div>
        </CardHeader>

        <CardContent className="space-y-4">
          {error && (
            <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2">
              <AlertCircle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-700">
                Version Tag <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                maxLength={50}
                placeholder="e.g. v1.0, v1.1-rc1, v2.0"
                value={version}
                onChange={(e) => setVersion(e.target.value)}
                className="w-full px-3 py-2 text-xs font-mono rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-semibold text-slate-700">
                Release Name <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                maxLength={100}
                placeholder="e.g. Initial Release, Beta Submission"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500"
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="block text-xs font-semibold text-slate-700">Release Notes / Description</label>
            <textarea
              rows={4}
              maxLength={1000}
              placeholder="Summary of changes, new features, or architectural adjustments in this release..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-3 py-2 text-xs rounded-lg border border-slate-300 focus:outline-none focus:ring-1 focus:ring-indigo-500"
            />
          </div>
        </CardContent>

        <CardFooter className="justify-end gap-3 border-t border-slate-100">
          <Link href={isEdit && initialData ? `/releases/${initialData.id}` : `/projects/${projectId}/releases`}>
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
            {isEdit ? 'Update Release' : 'Create Release'}
          </Button>
        </CardFooter>
      </Card>
    </form>
  );
};

export default ReleaseForm;
