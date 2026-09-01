import React from 'react';
import { Card } from '@/components/ui/Card';
import { AlertCircle, AlertTriangle, Info, CheckCircle2 } from 'lucide-react';

export interface RiskSummaryProps {
  summary: {
    critical: number;
    high: number;
    medium: number;
    low: number;
    total: number;
  };
}

export const RiskSummary: React.FC<RiskSummaryProps> = ({ summary }) => {
  const cards = [
    {
      label: 'Critical',
      count: summary.critical,
      desc: 'Immediate release blockers',
      color: 'text-rose-600 bg-rose-50 border-rose-200',
      icon: AlertCircle,
    },
    {
      label: 'High',
      count: summary.high,
      desc: 'Significant vulnerabilities',
      color: 'text-orange-600 bg-orange-50 border-orange-200',
      icon: AlertTriangle,
    },
    {
      label: 'Medium',
      count: summary.medium,
      desc: 'Quality & coverage issues',
      color: 'text-amber-600 bg-amber-50 border-amber-200',
      icon: AlertTriangle,
    },
    {
      label: 'Low',
      count: summary.low,
      desc: 'Minor code smell findings',
      color: 'text-sky-600 bg-sky-50 border-sky-200',
      icon: Info,
    },
  ];

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
      {cards.map((item, idx) => {
        const Icon = item.icon;
        return (
          <Card key={idx} className={`p-4 border ${item.color} space-y-1`}>
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider">{item.label}</span>
              <Icon className="w-4 h-4 opacity-80" />
            </div>
            <div className="text-2xl font-black">{item.count}</div>
            <p className="text-[10px] opacity-75">{item.desc}</p>
          </Card>
        );
      })}
    </div>
  );
};

export default RiskSummary;
