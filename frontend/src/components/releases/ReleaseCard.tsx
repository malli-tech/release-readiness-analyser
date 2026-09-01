'use client';

import React from 'react';
import Link from 'next/link';
import { Release } from '@/types/release';
import { Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import Button from '@/components/ui/Button';
import { formatDate } from '@/lib/utils';
import { GitBranch, Calendar, ArrowRight, UploadCloud } from 'lucide-react';

export interface ReleaseCardProps {
  release: Release;
}

export const ReleaseCard: React.FC<ReleaseCardProps> = ({ release }) => {
  const isNotAnalyzed = release.status === 'NOT_ANALYZED' || !release.score;

  return (
    <Card className="hover:border-indigo-200 transition group flex flex-col justify-between">
      <div>
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between gap-2">
            <div className="flex items-center gap-2.5">
              <div className="p-2 rounded-lg bg-indigo-50 text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white transition-colors">
                <GitBranch className="w-5 h-5" />
              </div>
              <div>
                <CardTitle className="text-sm font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">
                  <Link href={`/releases/${release.id}`}>{release.version}</Link>
                </CardTitle>
                <p className="text-xs font-semibold text-slate-700 mt-0.5">{release.name}</p>
              </div>
            </div>

            <Badge variant="neutral" size="sm" dot>
              {release.status ? release.status.replace('_', ' ') : 'NOT ANALYZED'}
            </Badge>
          </div>
        </CardHeader>

        <CardContent className="space-y-3 pt-0">
          <p className="text-xs text-slate-500 line-clamp-2 min-h-[32px]">
            {release.description || 'No release notes or description provided.'}
          </p>

          {isNotAnalyzed && (
            <div className="p-2.5 rounded-lg bg-slate-50 border border-slate-200 text-slate-600 text-[11px] flex items-center justify-between">
              <span>This release has not been analyzed yet.</span>
              <Link href={`/releases/${release.id}/upload`}>
                <span className="font-semibold text-indigo-600 hover:underline flex items-center gap-1">
                  <UploadCloud className="w-3 h-3" /> Upload
                </span>
              </Link>
            </div>
          )}
        </CardContent>
      </div>

      <CardFooter className="justify-between border-t border-slate-100 pt-3 text-xs bg-slate-50/50">
        <div className="flex items-center gap-1 text-[11px] text-slate-400">
          <Calendar className="w-3.5 h-3.5" />
          <span>{formatDate(release.createdAt)}</span>
        </div>

        <Link href={`/releases/${release.id}`}>
          <Button variant="ghost" size="sm" rightIcon={<ArrowRight className="w-3.5 h-3.5" />}>
            View Release
          </Button>
        </Link>
      </CardFooter>
    </Card>
  );
};

export default ReleaseCard;
