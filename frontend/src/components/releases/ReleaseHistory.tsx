'use client';

import React from 'react';
import Link from 'next/link';
import { Release } from '@/types/release';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import { formatDate } from '@/lib/utils';
import { GitBranch, Calendar, ArrowRight, Plus } from 'lucide-react';

export interface ReleaseHistoryProps {
  projectId?: string;
  releases: Release[];
}

export const ReleaseHistory: React.FC<ReleaseHistoryProps> = ({ projectId, releases }) => {
  if (releases.length === 0) {
    return (
      <Card>
        <CardHeader className="flex flex-row items-center justify-between">
          <div className="flex items-center gap-2">
            <GitBranch className="w-4 h-4 text-indigo-600" />
            <CardTitle className="text-sm">Release History</CardTitle>
          </div>
          {projectId && (
            <Link href={`/projects/${projectId}/releases/new`}>
              <Button size="sm" leftIcon={<Plus className="w-3.5 h-3.5" />}>
                + Create Release
              </Button>
            </Link>
          )}
        </CardHeader>
        <CardContent className="py-12 text-center space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center mx-auto">
            <GitBranch className="w-6 h-6" />
          </div>
          <div className="space-y-1">
            <h3 className="text-sm font-bold text-slate-900">No releases yet</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto">
              Create your first release candidate to track code versions and evaluation milestones.
            </p>
          </div>
          {projectId && (
            <Link href={`/projects/${projectId}/releases/new`}>
              <Button size="sm" leftIcon={<Plus className="w-3.5 h-3.5" />}>
                Create your first release
              </Button>
            </Link>
          )}
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <div className="flex items-center gap-2">
          <GitBranch className="w-4 h-4 text-indigo-600" />
          <CardTitle className="text-sm">Release History ({releases.length})</CardTitle>
        </div>
        {projectId && (
          <Link href={`/projects/${projectId}/releases/new`}>
            <Button size="sm" leftIcon={<Plus className="w-3.5 h-3.5" />}>
              + Create Release
            </Button>
          </Link>
        )}
      </CardHeader>

      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 border-b border-slate-100 text-slate-500 font-semibold uppercase tracking-wider">
              <tr>
                <th className="px-5 py-3">Version & Name</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Description</th>
                <th className="px-4 py-3">Created Date</th>
                <th className="px-5 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {releases.map((release) => (
                <tr key={release.id} className="hover:bg-slate-50/50">
                  <td className="px-5 py-3.5">
                    <div className="flex items-center gap-2.5">
                      <span className="font-mono font-bold text-xs text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">
                        {release.version}
                      </span>
                      <span className="font-semibold text-slate-900">{release.name}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3.5">
                    <Badge variant="neutral" dot size="sm">
                      {release.status ? release.status.replace('_', ' ') : 'NOT ANALYZED'}
                    </Badge>
                  </td>
                  <td className="px-4 py-3.5 text-slate-500 max-w-xs truncate">
                    {release.description || '—'}
                  </td>
                  <td className="px-4 py-3.5 text-slate-500">
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3 h-3 text-slate-400" />
                      {formatDate(release.createdAt)}
                    </span>
                  </td>
                  <td className="px-5 py-3.5 text-right">
                    <Link href={`/releases/${release.id}`}>
                      <Button size="sm" variant="ghost" rightIcon={<ArrowRight className="w-3.5 h-3.5" />}>
                        View Release
                      </Button>
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  );
};

export default ReleaseHistory;
