import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import ProgressBar from '@/components/ui/ProgressBar';
import { getScoreColor } from '@/lib/utils';
import { ShieldCheck, AlertTriangle, ArrowRight } from 'lucide-react';
import Link from 'next/link';

export interface RiskOverviewProps {
  overallScore?: number;
  status?: string;
  projectName?: string;
}

export const RiskOverview: React.FC<RiskOverviewProps> = ({
  overallScore = 82,
  status = 'NEEDS REVIEW',
  projectName = 'Student Management System (v1.2)',
}) => {
  const scoreConfig = getScoreColor(overallScore);

  // SVG Radial Gauge calculation
  const radius = 64;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (overallScore / 100) * circumference;

  const categories = [
    { name: 'Testing', score: 92, status: 'Good', color: 'emerald' as const },
    { name: 'Security', score: 74, status: 'Needs Attention', color: 'amber' as const },
    { name: 'Code Quality', score: 86, status: 'Good', color: 'emerald' as const },
    { name: 'Dependencies', score: 70, status: 'Needs Attention', color: 'amber' as const },
    { name: 'Performance', score: 88, status: 'Good', color: 'emerald' as const },
  ];

  return (
    <Card className="overflow-hidden">
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <div>
          <CardTitle>Overall Release Readiness</CardTitle>
          <p className="text-xs text-slate-500 mt-0.5">{projectName}</p>
        </div>
        <Badge variant={scoreConfig.badge} dot>
          {status}
        </Badge>
      </CardHeader>

      <CardContent className="space-y-6 pt-5">
        <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-center">
          {/* Radial Score Gauge */}
          <div className="md:col-span-5 flex flex-col items-center justify-center p-4 bg-slate-50 rounded-xl border border-slate-200/80">
            <div className="relative flex items-center justify-center">
              <svg className="w-36 h-36 transform -rotate-90">
                {/* Background track */}
                <circle
                  cx="72"
                  cy="72"
                  r={radius}
                  className="stroke-slate-200"
                  strokeWidth="10"
                  fill="transparent"
                />
                {/* Progress arc */}
                <circle
                  cx="72"
                  cy="72"
                  r={radius}
                  className={overallScore >= 90 ? 'stroke-emerald-500' : overallScore >= 70 ? 'stroke-amber-500' : 'stroke-rose-500'}
                  strokeWidth="10"
                  strokeDasharray={circumference}
                  strokeDashoffset={strokeDashoffset}
                  strokeLinecap="round"
                  fill="transparent"
                  style={{ transition: 'stroke-dashoffset 0.8s ease' }}
                />
              </svg>

              <div className="absolute flex flex-col items-center justify-center text-center">
                <span className="text-3xl font-extrabold text-slate-900">{overallScore}</span>
                <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-wider">out of 100</span>
              </div>
            </div>

            <p className="text-xs text-slate-600 font-medium mt-3 text-center">
              {overallScore >= 90
                ? 'Ready for production deployment'
                : overallScore >= 70
                ? '1 Critical blocker requires review'
                : 'High risk: release blocked'}
            </p>
          </div>

          {/* Category Progress Bars */}
          <div className="md:col-span-7 space-y-3.5">
            {categories.map((cat, idx) => (
              <div key={idx} className="space-y-1">
                <div className="flex justify-between text-xs font-medium">
                  <span className="text-slate-700">{cat.name}</span>
                  <div className="flex items-center gap-2">
                    <span className="font-semibold text-slate-900">{cat.score}/100</span>
                    <span className={`text-[10px] px-1.5 py-0.2 rounded font-semibold ${cat.color === 'emerald' ? 'text-emerald-700 bg-emerald-50' : 'text-amber-700 bg-amber-50'}`}>
                      {cat.status}
                    </span>
                  </div>
                </div>
                <ProgressBar value={cat.score} variant={cat.color} size="sm" />
              </div>
            ))}
          </div>
        </div>

        <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs">
          <span className="text-slate-500">Full evaluation breakdown with AI recommendations available</span>
          <Link
            href="/releases/rel-102/results"
            className="font-semibold text-indigo-600 hover:text-indigo-700 flex items-center gap-1"
          >
            <span>View Detailed Results</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>
      </CardContent>
    </Card>
  );
};

export default RiskOverview;
