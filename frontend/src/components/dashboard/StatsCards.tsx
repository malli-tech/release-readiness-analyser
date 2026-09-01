import React from 'react';
import { Card } from '@/components/ui/Card';
import { FolderGit2, GitBranch, CheckCircle2, AlertTriangle, ArrowUpRight, ArrowDownRight } from 'lucide-react';

export interface StatsCardsProps {
  totalProjects?: number;
  totalReleases?: number;
  projectsReady?: number;
  issuesFound?: number;
}

export const StatsCards: React.FC<StatsCardsProps> = ({
  totalProjects = 12,
  totalReleases = 27,
  projectsReady = 8,
  issuesFound = 43,
}) => {
  const stats = [
    {
      title: 'Total Projects',
      value: totalProjects,
      subtitle: 'Active repositories',
      icon: FolderGit2,
      trend: '+2 this month',
      isPositive: true,
      color: 'text-indigo-600 bg-indigo-50 border-indigo-100',
    },
    {
      title: 'Total Releases',
      value: totalReleases,
      subtitle: 'Evaluated versions',
      icon: GitBranch,
      trend: '+5 this week',
      isPositive: true,
      color: 'text-sky-600 bg-sky-50 border-sky-100',
    },
    {
      title: 'Projects Ready',
      value: projectsReady,
      subtitle: 'Passed release gates (≥90)',
      icon: CheckCircle2,
      trend: '66% success rate',
      isPositive: true,
      color: 'text-emerald-600 bg-emerald-50 border-emerald-100',
    },
    {
      title: 'Issues Found',
      value: issuesFound,
      subtitle: '4 critical blockers',
      icon: AlertTriangle,
      trend: '-12% vs last cycle',
      isPositive: true,
      color: 'text-amber-600 bg-amber-50 border-amber-100',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.map((stat, idx) => {
        const Icon = stat.icon;
        return (
          <Card key={idx} hover className="p-5 space-y-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                {stat.title}
              </span>
              <div className={`p-2 rounded-lg border ${stat.color}`}>
                <Icon className="w-4 h-4" />
              </div>
            </div>
            <div className="flex items-baseline justify-between">
              <span className="text-2xl font-bold text-slate-900 tracking-tight">
                {stat.value}
              </span>
              <span className="text-[11px] font-medium text-emerald-600 flex items-center gap-0.5">
                <ArrowUpRight className="w-3 h-3" />
                {stat.trend}
              </span>
            </div>
            <p className="text-xs text-slate-500">{stat.subtitle}</p>
          </Card>
        );
      })}
    </div>
  );
};

export default StatsCards;
