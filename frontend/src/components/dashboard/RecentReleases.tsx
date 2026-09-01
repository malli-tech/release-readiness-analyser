import React from 'react';
import Link from 'next/link';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import { mockReleases } from '@/lib/mock-data';
import { ArrowRight, ChevronRight, GitBranch } from 'lucide-react';
import { getScoreColor } from '@/lib/utils';

export const RecentReleases: React.FC = () => {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <div>
          <CardTitle>Recent Releases</CardTitle>
          <p className="text-xs text-slate-500 mt-0.5">Latest evaluated project versions</p>
        </div>
        <Link
          href="/projects"
          className="text-xs font-semibold text-indigo-600 hover:text-indigo-700 flex items-center gap-1"
        >
          <span>View All</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </Link>

      </CardHeader>

      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 border-b border-slate-100 text-slate-500 font-semibold uppercase tracking-wider">
              <tr>
                <th className="px-5 py-3">Project & Version</th>
                <th className="px-4 py-3">Score</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {mockReleases.slice(0, 5).map((release) => {
                const score = release.readinessScore || release.score || 0;
                const scoreInfo = getScoreColor(score);
                return (
                  <tr key={release.id} className="hover:bg-slate-50/80 transition-colors">
                    <td className="px-5 py-3.5 font-medium">
                      <div className="flex items-center gap-2">
                        <GitBranch className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                        <div>
                          <span className="font-semibold text-slate-900">{release.projectName}</span>
                          <span className="ml-2 font-mono text-[11px] text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded">
                            {release.version}
                          </span>
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3.5 font-bold text-slate-900">
                      {score > 0 ? `${score}/100` : 'Evaluating...'}
                    </td>
                    <td className="px-4 py-3.5">
                      <Badge variant={scoreInfo.badge} dot size="sm">
                        {release.status}
                      </Badge>
                    </td>

                    <td className="px-4 py-3.5 text-slate-500">{release.createdAt}</td>
                    <td className="px-4 py-3.5 text-right">
                      <Link
                        href={`/releases/${release.id}/results`}
                        className="inline-flex items-center gap-1 font-semibold text-indigo-600 hover:text-indigo-800"
                      >
                        <span>Details</span>
                        <ChevronRight className="w-3.5 h-3.5" />
                      </Link>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  );
};

export default RecentReleases;
