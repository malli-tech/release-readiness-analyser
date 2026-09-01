import React from 'react';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/Card';
import ProgressBar from '@/components/ui/ProgressBar';
import { CategoryScore } from '@/types/analysis';
import { Shield, TestTube2, Code2, Box, Gauge } from 'lucide-react';

export interface RiskBreakdownProps {
  categories: CategoryScore[];
}

export const RiskBreakdown: React.FC<RiskBreakdownProps> = ({ categories }) => {
  const getIcon = (cat: string) => {
    switch (cat) {
      case 'Security':
        return Shield;
      case 'Testing':
        return TestTube2;
      case 'Code Quality':
        return Code2;
      case 'Dependencies':
        return Box;
      case 'Performance':
      default:
        return Gauge;
    }
  };

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      {categories.map((cat, idx) => {
        const Icon = getIcon(cat.category);
        const isGood = cat.score >= 85;
        const isReview = cat.score >= 70 && cat.score < 85;

        return (
          <Card key={idx} className="p-4 flex flex-col justify-between space-y-3">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-2.5">
                <div className={`p-2 rounded-lg ${
                  isGood ? 'bg-emerald-50 text-emerald-600' : isReview ? 'bg-amber-50 text-amber-600' : 'bg-rose-50 text-rose-600'
                }`}>
                  <Icon className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="text-xs font-bold text-slate-900">{cat.category}</h4>
                  <span className="text-[10px] text-slate-400 font-medium">Weight: {cat.weight}%</span>
                </div>
              </div>
              <span className={`text-base font-extrabold ${
                isGood ? 'text-emerald-600' : isReview ? 'text-amber-600' : 'text-rose-600'
              }`}>
                {cat.score}/100
              </span>
            </div>

            <ProgressBar
              value={cat.score}
              variant={isGood ? 'emerald' : isReview ? 'amber' : 'rose'}
              size="sm"
            />

            <p className="text-[11px] text-slate-500 leading-relaxed pt-1 border-t border-slate-100">
              {cat.description}
            </p>
          </Card>
        );
      })}
    </div>
  );
};

export default RiskBreakdown;
