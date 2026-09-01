import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import { MetricComparison } from '@/types/analysis';
import { ArrowUpRight, ArrowDownRight, GitCompare } from 'lucide-react';

export interface VersionComparisonProps {
  previousVersion?: string;
  currentVersion?: string;
  metrics: MetricComparison[];
}

export const VersionComparison: React.FC<VersionComparisonProps> = ({
  previousVersion = 'v1.1',
  currentVersion = 'v1.2',
  metrics,
}) => {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between pb-3">
        <div className="flex items-center gap-2">
          <GitCompare className="w-4 h-4 text-indigo-600" />
          <CardTitle className="text-sm">Version Comparison</CardTitle>
        </div>
        <span className="text-xs font-mono font-semibold text-slate-600 bg-slate-100 px-2 py-0.5 rounded">
          {previousVersion} → {currentVersion}
        </span>
      </CardHeader>

      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 border-b border-slate-100 text-slate-500 font-semibold uppercase tracking-wider">
              <tr>
                <th className="px-5 py-3">Metric</th>
                <th className="px-4 py-3 text-center">{previousVersion}</th>
                <th className="px-4 py-3 text-center">{currentVersion}</th>
                <th className="px-5 py-3 text-right">Delta</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700">
              {metrics.map((m, idx) => {
                const isPositive = m.isPositiveChange;
                return (
                  <tr key={idx} className="hover:bg-slate-50/50">
                    <td className="px-5 py-3 font-semibold text-slate-800">{m.name}</td>
                    <td className="px-4 py-3 text-center font-mono text-slate-500">
                      {m.previousValue}{m.unit || ''}
                    </td>
                    <td className="px-4 py-3 text-center font-mono font-bold text-slate-900">
                      {m.currentValue}{m.unit || ''}
                    </td>
                    <td className="px-5 py-3 text-right">
                      <span className={`inline-flex items-center gap-0.5 font-bold ${
                        isPositive ? 'text-emerald-600' : 'text-rose-600'
                      }`}>
                        {isPositive ? <ArrowUpRight className="w-3.5 h-3.5" /> : <ArrowDownRight className="w-3.5 h-3.5" />}
                        {m.change > 0 ? `+${m.change}` : m.change}{m.unit || ''}
                      </span>
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

export default VersionComparison;
