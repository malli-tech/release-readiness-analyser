import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { TrendingUp, Award, ArrowUpRight } from 'lucide-react';

export const ScoreTrend: React.FC = () => {
  const trendData = [
    { version: 'v1.0', score: 62, date: 'Jul 25', change: 'Baseline' },
    { version: 'v1.1', score: 75, date: 'Aug 10', change: '+13 pts' },
    { version: 'v1.2', score: 82, date: 'Aug 20', change: '+7 pts' },
    { version: 'v1.3', score: 91, date: 'Aug 29', change: '+9 pts' },
  ];

  const maxScore = 100;

  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <div>
          <CardTitle>Release Score Trend</CardTitle>
          <p className="text-xs text-slate-500 mt-0.5">Historical readiness progression</p>
        </div>
        <div className="flex items-center gap-1.5 text-xs font-semibold text-emerald-600 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-100">
          <TrendingUp className="w-3.5 h-3.5" />
          <span>+29 pts overall</span>
        </div>
      </CardHeader>

      <CardContent className="space-y-6">
        {/* Visual Bar Graph */}
        <div className="pt-4 flex items-end justify-between gap-4 h-44 border-b border-slate-100 pb-2">
          {trendData.map((item, idx) => {
            const heightPercent = (item.score / maxScore) * 100;
            const isLatest = idx === trendData.length - 1;

            return (
              <div key={idx} className="flex-1 flex flex-col items-center gap-2 group">
                <span className={`text-xs font-bold ${isLatest ? 'text-indigo-600' : 'text-slate-700'}`}>
                  {item.score}
                </span>

                <div className="w-full max-w-[48px] bg-slate-100 rounded-t-lg h-32 flex items-end p-1">
                  <div
                    className={`w-full rounded-t-md transition-all duration-500 ${
                      isLatest
                        ? 'bg-gradient-to-t from-indigo-600 to-indigo-500 shadow-sm'
                        : 'bg-slate-400 group-hover:bg-slate-500'
                    }`}
                    style={{ height: `${heightPercent}%` }}
                  />
                </div>

                <div className="text-center">
                  <span className="font-mono text-xs font-bold text-slate-900 block">{item.version}</span>
                  <span className="text-[10px] text-slate-400">{item.date}</span>
                </div>
              </div>
            );
          })}
        </div>

        {/* Milestone Callout */}
        <div className="p-3 bg-indigo-50/60 rounded-lg border border-indigo-100 flex items-center justify-between text-xs">
          <div className="flex items-center gap-2 text-indigo-900">
            <Award className="w-4 h-4 text-indigo-600 shrink-0" />
            <span>Latest candidate <strong>v1.3</strong> passed release readiness threshold (91/100).</span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};

export default ScoreTrend;
